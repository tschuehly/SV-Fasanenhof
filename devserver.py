#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import os
import time
from http import HTTPStatus
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import urlparse


class LiveReloadHandler(SimpleHTTPRequestHandler):
    server_version = "SVFDevServer/1.0"

    def __init__(self, *args, directory: str, **kwargs):
        self._directory = directory
        super().__init__(*args, directory=directory, **kwargs)

    def do_GET(self) -> None:
        parsed = urlparse(self.path)
        if parsed.path == "/__livereload":
            self._serve_livereload()
            return
        if parsed.path == "/__livereload/ping":
            self._serve_ping()
            return
        super().do_GET()

    def log_message(self, format: str, *args) -> None:
        return

    def _serve_ping(self) -> None:
        payload = json.dumps({"version": self.server.reload_version}).encode("utf-8")
        self.send_response(HTTPStatus.OK)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Cache-Control", "no-cache, no-store, must-revalidate")
        self.send_header("Content-Length", str(len(payload)))
        self.end_headers()
        self.wfile.write(payload)

    def _serve_livereload(self) -> None:
        self.send_response(HTTPStatus.OK)
        self.send_header("Content-Type", "text/event-stream")
        self.send_header("Cache-Control", "no-cache, no-store, must-revalidate")
        self.send_header("Connection", "keep-alive")
        self.end_headers()

        last_version = -1
        while True:
            version = self.server.reload_version
            if version != last_version:
                payload = f"event: reload\ndata: {version}\n\n".encode("utf-8")
                self.wfile.write(payload)
                self.wfile.flush()
                last_version = version
            time.sleep(0.5)


class LiveReloadServer(ThreadingHTTPServer):
    def __init__(self, server_address: tuple[str, int], handler_cls, directory: str):
        super().__init__(server_address, handler_cls)
        self.directory = directory
        self.reload_version = self._compute_reload_version()

    def _compute_reload_version(self) -> int:
        newest = 0.0
        for root, _, files in os.walk(self.directory):
            for name in files:
                path = Path(root, name)
                newest = max(newest, path.stat().st_mtime)
        return int(newest * 1000)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--directory", required=True)
    parser.add_argument("--port", type=int, default=8080)
    args = parser.parse_args()

    directory = os.path.abspath(args.directory)
    handler = lambda *a, **kw: LiveReloadHandler(*a, directory=directory, **kw)
    server = LiveReloadServer(("127.0.0.1", args.port), handler, directory)
    print(f"Serving on http://localhost:{args.port}", flush=True)
    server.serve_forever()


if __name__ == "__main__":
    main()
