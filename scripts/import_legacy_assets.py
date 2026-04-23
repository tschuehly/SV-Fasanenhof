#!/usr/bin/env python3

from __future__ import annotations

import hashlib
import json
import mimetypes
import re
import ssl
import sys
import time
from dataclasses import dataclass, field
from html.parser import HTMLParser
from pathlib import Path
from typing import Dict, Iterable, List, Set, Tuple
from urllib.error import HTTPError, URLError
from urllib.parse import quote, unquote, urljoin, urlsplit, urlunsplit
from urllib.request import Request, urlopen


PROJECT_ROOT = Path(__file__).resolve().parent.parent
MIGRATIONS_ROOT = PROJECT_ROOT / "migrations"
CRAWL_ROOT = MIGRATIONS_ROOT / "crawl-output"
OUTPUT_ROOT = MIGRATIONS_ROOT / "downloads" / "legacy-assets"
FILES_ROOT = OUTPUT_ROOT / "files"
MANIFEST_PATH = OUTPUT_ROOT / "manifest.json"
REPORT_PATH = OUTPUT_ROOT / "report.md"

USER_AGENT = "Mozilla/5.0 (compatible; SV-Fasanenhof-LegacyImporter/1.0)"
REQUEST_TIMEOUT = 20
PAGE_DELAY_SECONDS = 0.2

PAGE_HOSTS = {
    "sv-fasanenhof.de",
    "www.sv-fasanenhof.de",
    "bsg-fasanenhof.jimdofree.com",
}

IMAGE_HOSTS = PAGE_HOSTS | {
    "image.jimcdn.com",
    "assets.jimstatic.com",
}

IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp", ".svg"}
NON_CONTENT_PREFIXES = (
    "/about",
    "/j/",
    "/login",
    "/protected",
    "/sitemap",
)
MARKDOWN_URL_PATTERN = re.compile(r"https?://[^\s)\]>\"']+")
WORDPRESS_SIZE_SUFFIX_PATTERN = re.compile(r"-\d+x\d+$")
JIMDO_TRANSFORM_PATTERN = re.compile(
    r"^/app/cms/image/transf/(?P<transform>[^/]+)/path/(?P<path>.+)$"
)


def log(message: str) -> None:
    print(message, file=sys.stderr)


@dataclass(order=True)
class Candidate:
    priority: int
    url: str = field(compare=False)
    source_pages: Set[str] = field(default_factory=set, compare=False)
    discovered_from: Set[str] = field(default_factory=set, compare=False)


class LegacyHtmlParser(HTMLParser):
    def __init__(self, page_url: str) -> None:
        super().__init__(convert_charrefs=True)
        self.page_url = page_url
        self.image_urls: Set[str] = set()
        self.page_urls: Set[str] = set()

    def handle_starttag(self, tag: str, attrs: List[Tuple[str, str | None]]) -> None:
        attributes = dict(attrs)
        for attribute_name in ("src", "href", "content", "data-image", "data-src", "srcset"):
            raw_value = attributes.get(attribute_name)
            if not raw_value:
                continue
            self._collect_attribute_value(attribute_name, raw_value)

    def handle_data(self, data: str) -> None:
        for match in MARKDOWN_URL_PATTERN.findall(data):
            self._collect_url(match)

    def _collect_attribute_value(self, attribute_name: str, value: str) -> None:
        if attribute_name == "srcset":
            for item in value.split(","):
                candidate = item.strip().split(" ", 1)[0]
                self._collect_url(candidate)
            return

        for match in re.findall(r"url\(([^)]+)\)", value):
            self._collect_url(match.strip("\"' "))

        for match in MARKDOWN_URL_PATTERN.findall(value):
            self._collect_url(match)

        self._collect_url(value)

    def _collect_url(self, raw_url: str) -> None:
        normalized = normalize_url(raw_url, self.page_url)
        if not normalized:
            return

        if is_image_url(normalized):
            self.image_urls.add(normalized)
        elif is_page_url(normalized):
            self.page_urls.add(normalized)


def normalize_url(raw_url: str, base_url: str | None = None) -> str | None:
    value = raw_url.strip().strip("\"'")
    if not value or value.startswith("data:") or value.startswith("javascript:"):
        return None

    if value.startswith("//"):
        value = "https:" + value

    if base_url:
        value = urljoin(base_url, value)

    parts = urlsplit(value)
    if parts.scheme not in {"http", "https"}:
        return None

    cleaned = parts._replace(
        path=quote(unquote(parts.path), safe="/%:@()+,;=-._~"),
        query=quote(unquote(parts.query), safe="=&%:@()+,;/-._~"),
        fragment="",
    )
    return urlunsplit(cleaned)


def is_page_url(url: str) -> bool:
    parts = urlsplit(url)
    if parts.netloc not in PAGE_HOSTS:
        return False

    if any(parts.path.startswith(prefix) for prefix in NON_CONTENT_PREFIXES):
        return False

    if parts.query:
        return False

    if has_image_extension(parts.path):
        return False

    if parts.path.lower().endswith(".pdf"):
        return False

    return True


def is_image_url(url: str) -> bool:
    parts = urlsplit(url)
    if parts.netloc not in IMAGE_HOSTS:
        return False

    return has_image_extension(parts.path)


def has_image_extension(path: str) -> bool:
    suffix = Path(path).suffix.lower()
    return suffix in IMAGE_EXTENSIONS


def gather_seed_urls() -> Tuple[Set[str], Set[str]]:
    page_urls: Set[str] = set()
    image_urls: Set[str] = set()

    for markdown_path in sorted(CRAWL_ROOT.rglob("*.md")):
        text = markdown_path.read_text(encoding="utf-8")
        for raw_url in MARKDOWN_URL_PATTERN.findall(text):
            normalized = normalize_url(raw_url)
            if not normalized:
                continue
            if is_page_url(normalized):
                page_urls.add(normalized)
            elif is_image_url(normalized):
                image_urls.add(normalized)

    return page_urls, image_urls


def fetch_url(url: str) -> bytes:
    request = Request(url, headers={"User-Agent": USER_AGENT})
    context = ssl._create_unverified_context()
    with urlopen(request, timeout=REQUEST_TIMEOUT, context=context) as response:
        return response.read()


def build_candidate_index(seed_image_urls: Iterable[str]) -> Tuple[Dict[str, Candidate], List[dict]]:
    candidates: Dict[str, Candidate] = {}
    page_reports: List[dict] = []
    seed_page_urls, markdown_image_urls = gather_seed_urls()

    for image_url in seed_image_urls:
        register_image_candidate(candidates, image_url, "markdown", set())

    total_pages = len(seed_page_urls)
    for index, page_url in enumerate(sorted(seed_page_urls), start=1):
        log(f"[{index}/{total_pages}] scan {page_url}")
        report = {
            "url": page_url,
            "status": "ok",
            "discoveredImages": 0,
            "error": None,
        }
        try:
            html = fetch_url(page_url).decode("utf-8", errors="replace")
            parser = LegacyHtmlParser(page_url)
            parser.feed(html)
            report["discoveredImages"] = len(parser.image_urls)

            for image_url in parser.image_urls:
                register_image_candidate(candidates, image_url, "page", {page_url})
        except (HTTPError, URLError, TimeoutError) as error:
            report["status"] = "error"
            report["error"] = str(error)
        page_reports.append(report)
        time.sleep(PAGE_DELAY_SECONDS)

    return candidates, page_reports


def register_image_candidate(
    candidates: Dict[str, Candidate],
    image_url: str,
    discovered_from: str,
    source_pages: Set[str],
) -> None:
    key, priority = canonical_image_key(image_url)
    existing = candidates.get(key)
    if existing is None or priority < existing.priority:
        replacement = Candidate(priority=priority, url=image_url)
        if existing:
            replacement.source_pages = set(existing.source_pages)
            replacement.discovered_from = set(existing.discovered_from)
        candidates[key] = replacement
        existing = replacement

    existing.source_pages.update(source_pages)
    existing.discovered_from.add(discovered_from)


def canonical_image_key(url: str) -> Tuple[str, int]:
    parts = urlsplit(url)
    host = parts.netloc
    path = parts.path

    if "sv-fasanenhof.de" in host and "/wp-content/uploads/" in path:
        suffix = Path(path).suffix
        base = WORDPRESS_SIZE_SUFFIX_PATTERN.sub("", Path(path).stem)
        canonical_path = str(Path(path).with_name(base + suffix))
        sized = canonical_path != path
        priority = 0 if not sized else 100
        return f"{host}{canonical_path}", priority

    if host == "image.jimcdn.com":
        match = JIMDO_TRANSFORM_PATTERN.match(path)
        if match:
            transform = match.group("transform")
            image_path = match.group("path")
            priority = jimdo_transform_priority(transform)
            return f"{host}/{image_path}", priority

    return f"{host}{path}", 50


def jimdo_transform_priority(transform: str) -> int:
    if transform == "none":
        return 0

    dimensions = [
        int(value)
        for value in re.findall(r"dimension=(\d+)x(\d+)", transform)
        for value in value
    ]
    if dimensions:
        return 10000 - max(dimensions)

    return 5000


def choose_output_path(url: str, content_type: str | None) -> Path:
    parts = urlsplit(url)
    path = Path(parts.path.lstrip("/"))
    relative = Path(parts.netloc) / path
    if not relative.suffix:
        guessed_extension = mimetypes.guess_extension((content_type or "").split(";", 1)[0]) or ".bin"
        relative = relative.with_suffix(guessed_extension)

    if parts.query:
        digest = hashlib.sha1(parts.query.encode("utf-8")).hexdigest()[:10]
        relative = relative.with_name(f"{relative.stem}-{digest}{relative.suffix}")

    return FILES_ROOT / relative


def download_images(candidates: Dict[str, Candidate]) -> List[dict]:
    downloads: List[dict] = []

    for index, candidate in enumerate(sorted(candidates.values(), key=lambda item: item.url), start=1):
        log(f"[image {index}/{len(candidates)}] fetch {candidate.url}")
        record = {
            "url": candidate.url,
            "priority": candidate.priority,
            "sourcePages": sorted(candidate.source_pages),
            "discoveredFrom": sorted(candidate.discovered_from),
            "status": "ok",
            "savedTo": None,
            "bytes": None,
            "error": None,
        }
        try:
            request = Request(candidate.url, headers={"User-Agent": USER_AGENT})
            context = ssl._create_unverified_context()
            with urlopen(request, timeout=REQUEST_TIMEOUT, context=context) as response:
                content = response.read()
                content_type = response.headers.get("Content-Type")
            target_path = choose_output_path(candidate.url, content_type)
            target_path.parent.mkdir(parents=True, exist_ok=True)
            target_path.write_bytes(content)
            record["savedTo"] = str(target_path.relative_to(PROJECT_ROOT))
            record["bytes"] = len(content)
        except Exception as error:
            record["status"] = "error"
            record["error"] = str(error)
        downloads.append(record)

    return downloads


def write_outputs(page_reports: List[dict], downloads: List[dict]) -> None:
    OUTPUT_ROOT.mkdir(parents=True, exist_ok=True)

    successful_downloads = [entry for entry in downloads if entry["status"] == "ok"]
    total_bytes = sum(entry["bytes"] or 0 for entry in successful_downloads)
    manifest = {
        "generatedAt": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
        "pageCount": len(page_reports),
        "pageErrors": len([entry for entry in page_reports if entry["status"] != "ok"]),
        "imageCount": len(downloads),
        "downloadErrors": len([entry for entry in downloads if entry["status"] != "ok"]),
        "downloadedBytes": total_bytes,
        "pages": page_reports,
        "images": downloads,
    }
    MANIFEST_PATH.write_text(json.dumps(manifest, ensure_ascii=False, indent=2), encoding="utf-8")

    image_lines = [
        "# Legacy-Asset-Import",
        "",
        f"- Seiten gescannt: **{manifest['pageCount']}**",
        f"- Bilddateien ausgewählt: **{manifest['imageCount']}**",
        f"- erfolgreich geladen: **{len(successful_downloads)}**",
        f"- Ladefehler: **{manifest['downloadErrors']}**",
        f"- Datenmenge: **{format_bytes(total_bytes)}**",
        "",
        "## Wichtige Pfade",
        "",
        f"- Manifest: `{MANIFEST_PATH.relative_to(PROJECT_ROOT)}`",
        f"- Dateien: `{FILES_ROOT.relative_to(PROJECT_ROOT)}`",
        "",
        "## Seiten mit den meisten entdeckten Bildern",
        "",
    ]

    busiest_pages = sorted(page_reports, key=lambda item: item["discoveredImages"], reverse=True)[:15]
    for entry in busiest_pages:
        image_lines.append(
            f"- `{entry['url']}`: {entry['discoveredImages']} Bilder"
            + (f" ({entry['error']})" if entry["error"] else "")
        )

    image_lines.extend(["", "## Fehlgeschlagene Downloads", ""])
    failed_downloads = [entry for entry in downloads if entry["status"] != "ok"][:30]
    if not failed_downloads:
        image_lines.append("- keine")
    else:
        for entry in failed_downloads:
            image_lines.append(f"- `{entry['url']}`: {entry['error']}")

    REPORT_PATH.write_text("\n".join(image_lines) + "\n", encoding="utf-8")


def format_bytes(size: int) -> str:
    units = ["B", "KB", "MB", "GB"]
    value = float(size)
    for unit in units:
        if value < 1024 or unit == units[-1]:
            return f"{value:.1f} {unit}"
        value /= 1024
    return f"{size} B"


def main() -> int:
    seed_page_urls, seed_image_urls = gather_seed_urls()
    log(f"Seed pages: {len(seed_page_urls)}")
    log(f"Seed images from markdown: {len(seed_image_urls)}")

    candidates, page_reports = build_candidate_index(seed_image_urls)
    log(f"Selected image candidates: {len(candidates)}")
    downloads = download_images(candidates)
    write_outputs(page_reports, downloads)

    successful_downloads = len([entry for entry in downloads if entry["status"] == "ok"])
    log(f"Downloaded {successful_downloads}/{len(downloads)} image files")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
