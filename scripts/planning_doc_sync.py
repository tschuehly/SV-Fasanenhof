#!/usr/bin/env python3

from __future__ import annotations

import argparse
import difflib
import re
import sys
import zipfile
from dataclasses import dataclass, field
from pathlib import Path
from typing import Iterable
from xml.etree import ElementTree as ET


W_NS = {"w": "http://schemas.openxmlformats.org/wordprocessingml/2006/main"}
DEFAULT_MARKDOWN_PATH = Path("/Users/tschuehly/IdeaProjects/SV-Fasanenhof/site-structure.md")
DEFAULT_DOCX_PATH = Path(
    "/Users/tschuehly/Library/CloudStorage/OneDrive-Personal/Dokumente/Hobby/SV-Fasanenhof-Homepage.docx"
)
DEFAULT_REPORT_PATH = Path("/Users/tschuehly/IdeaProjects/SV-Fasanenhof/build/planning-doc-sync/report.md")
PRESENTATION_ONLY_DOCX_LINES = {
    "Webseitenstruktur und Inhaltsplan",
    "Arbeitsstruktur für die Erstellung und Erweiterung der SV-Fasanenhof-Webseite",
}


@dataclass
class Unit:
    key: tuple[str, ...]
    level: int
    lines: list[str] = field(default_factory=list)


def normalize_line(text: str) -> str:
    text = text.replace("\u00a0", " ")
    text = re.sub(r"\s+", " ", text.strip())
    return text


def slugify(text: str) -> str:
    mapped = (
        text.replace("ä", "ae")
        .replace("ö", "oe")
        .replace("ü", "ue")
        .replace("Ä", "Ae")
        .replace("Ö", "Oe")
        .replace("Ü", "Ue")
        .replace("ß", "ss")
    )
    mapped = mapped.lower()
    mapped = re.sub(r"[^a-z0-9]+", "-", mapped)
    return mapped.strip("-") or "workpackage"


def parse_markdown(path: Path) -> dict[tuple[str, ...], Unit]:
    units: dict[tuple[str, ...], Unit] = {}
    headings: dict[int, str] = {}

    def current_key() -> tuple[str, ...] | None:
        if 3 in headings:
            return (headings[1], headings[2], headings[3])
        if 2 in headings:
            return (headings[1], headings[2])
        if 1 in headings:
            return (headings[1],)
        return None

    for raw in path.read_text(encoding="utf-8").splitlines():
        line = raw.rstrip()
        stripped = line.strip()
        if not stripped:
            continue

        heading_match = re.match(r"^(#{1,3})\s+(.*)$", stripped)
        if heading_match:
            level = len(heading_match.group(1))
            title = normalize_line(heading_match.group(2))
            headings[level] = title
            for child_level in (level + 1, level + 2):
                headings.pop(child_level, None)
            key = tuple(headings[i] for i in range(1, level + 1))
            units[key] = Unit(key=key, level=level)
            continue

        key = current_key()
        if key is None:
            continue

        if stripped.startswith("- "):
            normalized = "• " + normalize_line(stripped[2:])
        else:
            normalized = normalize_line(stripped)
        units[key].lines.append(normalized)

    return units


def parse_docx_paragraphs(path: Path) -> list[tuple[str | None, str]]:
    paragraphs: list[tuple[str | None, str]] = []
    with zipfile.ZipFile(path) as archive:
        document_xml = archive.read("word/document.xml")
    root = ET.fromstring(document_xml)

    for paragraph in root.findall(".//w:body/w:p", W_NS):
        text = "".join(node.text or "" for node in paragraph.findall(".//w:t", W_NS))
        text = normalize_line(text)
        if not text:
            continue
        style = paragraph.find("./w:pPr/w:pStyle", W_NS)
        num_pr = paragraph.find("./w:pPr/w:numPr", W_NS)
        style_id = style.attrib.get(f"{{{W_NS['w']}}}val") if style is not None else None
        is_list = num_pr is not None or style_id in {"ListParagraph", "ListBullet", "ListBullet2", "ListBullet3"}
        if is_list and not text.startswith("• "):
            text = f"• {text}"
        paragraphs.append((style_id, text))

    return paragraphs


def parse_docx(path: Path) -> dict[tuple[str, ...], Unit]:
    units: dict[tuple[str, ...], Unit] = {}
    headings: dict[int, str] = {}

    def current_key() -> tuple[str, ...] | None:
        if 3 in headings:
            return (headings[1], headings[2], headings[3])
        if 2 in headings:
            return (headings[1], headings[2])
        if 1 in headings:
            return (headings[1],)
        return None

    for style_id, text in parse_docx_paragraphs(path):
        if text in PRESENTATION_ONLY_DOCX_LINES:
            continue

        if style_id == "Heading1":
            headings[1] = text
            headings.pop(2, None)
            headings.pop(3, None)
            key = (text,)
            units[key] = Unit(key=key, level=1)
            continue
        if style_id == "Heading2":
            if 1 not in headings:
                continue
            headings[2] = text
            headings.pop(3, None)
            key = (headings[1], text)
            units[key] = Unit(key=key, level=2)
            continue
        if style_id == "Heading3":
            if 1 not in headings or 2 not in headings:
                continue
            headings[3] = text
            key = (headings[1], headings[2], text)
            units[key] = Unit(key=key, level=3)
            continue

        key = current_key()
        if key is None:
            continue
        units[key].lines.append(text)

    return units


def leaf_keys(units: dict[tuple[str, ...], Unit]) -> list[tuple[str, ...]]:
    keys = sorted(units.keys())
    leafs: list[tuple[str, ...]] = []
    for key in keys:
        is_parent = any(other[: len(key)] == key and len(other) > len(key) for other in keys)
        if not is_parent:
            leafs.append(key)
    return leafs


def compare_units(
    md_units: dict[tuple[str, ...], Unit], docx_units: dict[tuple[str, ...], Unit]
) -> list[tuple[tuple[str, ...], list[str], list[str]]]:
    changed: list[tuple[tuple[str, ...], list[str], list[str]]] = []
    for key in sorted(set(leaf_keys(md_units)) | set(leaf_keys(docx_units))):
        md_lines = md_units.get(key, Unit(key=key, level=len(key))).lines
        docx_lines = docx_units.get(key, Unit(key=key, level=len(key))).lines
        if md_lines != docx_lines:
            changed.append((key, md_lines, docx_lines))
    return changed


def summarize_line_changes(md_lines: list[str], docx_lines: list[str]) -> tuple[list[str], list[str]]:
    removed = [line for line in md_lines if line not in docx_lines]
    added = [line for line in docx_lines if line not in md_lines]
    return removed, added


def unified_diff_lines(key: tuple[str, ...], md_lines: list[str], docx_lines: list[str]) -> list[str]:
    title = " / ".join(key)
    return list(
        difflib.unified_diff(
            md_lines,
            docx_lines,
            fromfile=f"markdown:{title}",
            tofile=f"docx:{title}",
            lineterm="",
        )
    )


def render_report(
    changed: list[tuple[tuple[str, ...], list[str], list[str]]],
    markdown_path: Path,
    docx_path: Path,
) -> str:
    lines = [
        "# Vergleich Markdown vs. Word",
        "",
        f"- Markdown: `{markdown_path}`",
        f"- Word: `{docx_path}`",
        f"- Geänderte Arbeitseinheiten: `{len(changed)}`",
        "",
    ]

    if not changed:
        lines.extend(
            [
                "## Ergebnis",
                "",
                "Markdown und Word sind inhaltlich synchron.",
                "",
            ]
        )
        return "\n".join(lines)

    lines.extend(["## Ergebnis", "", "Die folgenden Arbeitseinheiten unterscheiden sich:", ""])
    for key, md_lines, docx_lines in changed:
        removed, added = summarize_line_changes(md_lines, docx_lines)
        lines.append(f"### {' / '.join(key)}")
        lines.append("")
        if removed:
            lines.append("Entfernt oder geändert gegenüber Markdown:")
            lines.extend(f"- {line}" for line in removed)
            lines.append("")
        if added:
            lines.append("Neu oder abweichend in Word:")
            lines.extend(f"- {line}" for line in added)
            lines.append("")
        diff_lines = unified_diff_lines(key, md_lines, docx_lines)
        if diff_lines:
            lines.append("Diff:")
            lines.append("")
            lines.append("```diff")
            lines.extend(diff_lines)
            lines.append("```")
            lines.append("")

    return "\n".join(lines)


def write_workpackages(
    changed: list[tuple[tuple[str, ...], list[str], list[str]]], output_dir: Path
) -> list[Path]:
    output_dir.mkdir(parents=True, exist_ok=True)
    written: list[Path] = []

    for key, md_lines, docx_lines in changed:
        removed, added = summarize_line_changes(md_lines, docx_lines)
        h1 = key[0] if len(key) > 0 else ""
        h2 = key[1] if len(key) > 1 else ""
        h3 = key[2] if len(key) > 2 else ""
        title = key[-1]

        slug_parts = [slugify(part) for part in key]
        target = output_dir / f"{'__'.join(slug_parts)}.md"

        content = [
            f"# Workpackage: {title}",
            "",
            "## Kontext",
            "",
            f"- Bereich: {h1}",
            f"- Sektion: {h2 or '—'}",
            f"- Page: {h3 or '—'}",
            "- Quelle: Abgleich zwischen Markdown und geteilter Word-Datei",
            "",
            "## Ziel",
            "",
            "- Word-Änderungen prüfen und in eine belastbare Umsetzungsaufgabe übersetzen.",
            "- Entscheiden, ob Markdown, Website-Inhalt oder beide angepasst werden müssen.",
            "",
            "## Änderungen aus Word",
            "",
        ]

        if added:
            content.extend(f"- {line}" for line in added)
        else:
            content.append("- Keine zusätzlichen Inhalte in Word erkannt.")
        content.extend(["", "## Bestehender Markdown-Stand", ""])
        if removed:
            content.extend(f"- {line}" for line in removed)
        else:
            content.append("- Kein abweichender Markdown-Inhalt erkannt.")

        content.extend(
            [
                "",
                "## Akzeptanzkriterien",
                "",
                "- Die fachliche Änderung ist mit den zuständigen Personen abgestimmt.",
                "- Markdown und Word sind nach der Einarbeitung wieder synchron.",
                "- Offene Fragen sind dokumentiert oder geklärt.",
                "",
                "## Offene Fragen",
                "",
                "- Wer gibt die Änderung fachlich frei?",
                "- Betrifft die Änderung nur die Planung oder auch bestehenden Website-Inhalt?",
                "- Gibt es Abhängigkeiten zu anderen Pages oder Abteilungen?",
                "",
                "## Notizen",
                "",
                "- Hier konkrete Umsetzungsschritte, Quellen und Verantwortliche ergänzen.",
                "",
            ]
        )

        target.write_text("\n".join(content), encoding="utf-8")
        written.append(target)

    return written


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description="Vergleicht die geteilte Word-Datei mit dem führenden Markdown und erzeugt bei Bedarf Workpackage-Specs."
    )
    parser.add_argument(
        "--markdown",
        default=DEFAULT_MARKDOWN_PATH,
        type=Path,
        help=f"Pfad zur führenden Markdown-Datei. Standard: {DEFAULT_MARKDOWN_PATH}",
    )
    parser.add_argument(
        "--docx",
        default=DEFAULT_DOCX_PATH,
        type=Path,
        help=f"Pfad zur geteilten Word-Datei. Standard: {DEFAULT_DOCX_PATH}",
    )
    parser.add_argument(
        "--report",
        default=DEFAULT_REPORT_PATH,
        type=Path,
        help=f"Pfad für einen Markdown-Report. Standard: {DEFAULT_REPORT_PATH}",
    )
    parser.add_argument(
        "--workpackages-dir",
        type=Path,
        help="Optionales Ausgabeverzeichnis für Workpackage-Specs der geänderten Arbeitseinheiten.",
    )
    parser.add_argument(
        "--fail-on-diff",
        action="store_true",
        help="Liefert Exit-Code 1, wenn Unterschiede gefunden wurden.",
    )
    return parser


def main(argv: Iterable[str] | None = None) -> int:
    args = build_parser().parse_args(argv)
    if not args.markdown.exists():
        print(f"Markdown-Datei nicht gefunden: {args.markdown}", file=sys.stderr)
        return 2
    if not args.docx.exists():
        print(f"Word-Datei nicht gefunden: {args.docx}", file=sys.stderr)
        return 2

    md_units = parse_markdown(args.markdown)
    docx_units = parse_docx(args.docx)
    changed = compare_units(md_units, docx_units)

    report_text = render_report(changed, args.markdown, args.docx)
    args.report.parent.mkdir(parents=True, exist_ok=True)
    args.report.write_text(report_text, encoding="utf-8")
    print(f"Report geschrieben nach {args.report}", file=sys.stderr)

    if args.workpackages_dir:
        written = write_workpackages(changed, args.workpackages_dir)
        summary = f"{len(written)} Workpackage-Dateien geschrieben nach {args.workpackages_dir}"
        print(summary, file=sys.stderr)

    if changed and args.fail_on_diff:
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
