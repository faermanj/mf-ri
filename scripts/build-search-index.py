#!/usr/bin/env python3
"""Build the client-side search index for the site.

Scans every document descriptor (*.mf) under <MF>/ConteudoSite, pairs it
with its PDF transcript (<name>.pdf.txt, produced by transcribe-pdfs.py)
and writes __template/search-index.json, which the site generator copies
into the output. The search box (template/search.html) loads this file
with MiniSearch to navigate straight to the document's page.

Usage: build-search-index.py [root]
  root defaults to <MF>/ConteudoSite.
"""

import json
import re
import sys
from pathlib import Path

EXCERPT_CHARS = 1000  # per document, keeps the whole index around ~1 MB


def parse_mf(path: Path) -> dict:
    doc = {}
    for line in path.read_text(encoding="utf-8", errors="replace").splitlines():
        if "=" in line:
            key, _, value = line.partition("=")
            doc[key.strip()] = value.strip()
    return doc


def excerpt_for(mf: Path) -> str:
    base = mf.with_suffix("")
    for candidate in (f"{base.name}_TX.pdf.txt", f"{base.name}.pdf.txt"):
        txt = mf.with_name(candidate)
        if txt.exists():
            text = txt.read_text(encoding="utf-8", errors="replace")
            text = re.sub(r"\s+", " ", text).strip()
            return text[:EXCERPT_CHARS]
    return ""


def main() -> int:
    default_root = Path(__file__).resolve().parents[3] / "ConteudoSite"
    root = Path(sys.argv[1]) if len(sys.argv) > 1 else default_root

    docs = []
    seen = set()
    for mf in sorted(root.rglob("*.mf")):
        if "__template" in mf.parts or mf.name.startswith("_"):
            continue
        # The same document can live in two categories (e.g. Livros and
        # Textos_nao_Ineditos); output pages are flat, so index it once.
        if mf.stem in seen:
            continue
        seen.add(mf.stem)
        meta = parse_mf(mf)
        title = meta.get("title", "")
        if not title:
            continue
        docs.append({
            "id": mf.stem,  # output page is <stem>.html, flat in the site root
            "title": title,
            "publication": meta.get("publication", ""),
            "year": meta.get("year", ""),
            "keywords": meta.get("keywords", ""),
            "text": excerpt_for(mf),
        })

    out = root / "__template" / "search-index.json"
    out.write_text(json.dumps(docs, ensure_ascii=False, separators=(",", ":")),
                   encoding="utf-8")
    size_kb = out.stat().st_size // 1024
    print(f"{len(docs)} documents indexed -> {out} ({size_kb} KB)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
