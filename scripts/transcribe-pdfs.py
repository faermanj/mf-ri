#!/usr/bin/env python3
"""Transcribe every .pdf under ConteudoSite to a sibling <name>.pdf.txt.

Extracts the PDF's embedded text layer with PyMuPDF (pip install pymupdf),
which reconstructs words from glyph positions - the archive's old OCR
layers come out readable, where pypdf produced letter-by-letter output.
Where the layer is missing or garbled, real OCR (e.g. ocrmypdf with
tesseract-ocr-por) is needed instead - this script only extracts, it
does not OCR.

Usage: transcribe-pdfs.py [--force] [--jobs N] [root]
  root is a directory to scan recursively or a single .pdf file;
  defaults to <MF>/ConteudoSite (the repo lives at <MF>/Codigo/mf-ri).
  Files whose .pdf.txt is newer than the .pdf are skipped unless
  --force is given.
"""

import argparse
import sys
from concurrent.futures import ProcessPoolExecutor
from pathlib import Path

import pymupdf

PAGE_SEPARATOR = "\n\f\n"  # form feed between pages, as pdftotext does


def transcribe(pdf: Path, force: bool) -> tuple[Path, str, int]:
    """Returns (pdf, status, chars) where status is ok/skipped/error."""
    txt = pdf.with_name(pdf.name + ".txt")
    if not force and txt.exists() and txt.stat().st_mtime >= pdf.stat().st_mtime:
        return pdf, "skipped", 0
    try:
        with pymupdf.open(pdf) as doc:
            pages = [page.get_text("text").strip() for page in doc]
        text = PAGE_SEPARATOR.join(pages).strip() + "\n"
        txt.write_text(text, encoding="utf-8")
        return pdf, "ok", len(text)
    except Exception as e:  # noqa: BLE001 - keep going over a large archive
        return pdf, f"error: {e}", 0


def main() -> int:
    # scripts/ -> mf-ri -> Codigo -> MF root, where ConteudoSite lives
    default_root = Path(__file__).resolve().parents[3] / "ConteudoSite"
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("root", nargs="?", type=Path, default=default_root)
    ap.add_argument("--force", action="store_true",
                    help="re-transcribe even if the .pdf.txt is up to date")
    ap.add_argument("--jobs", type=int, default=None,
                    help="parallel workers (default: cpu count)")
    args = ap.parse_args()

    if args.root.is_file():
        pdfs = [args.root]
    else:
        pdfs = sorted(p for p in args.root.rglob("*.pdf") if p.is_file())
    if not pdfs:
        print(f"No .pdf files found under {args.root}", file=sys.stderr)
        return 1

    ok = skipped = errors = 0
    with ProcessPoolExecutor(max_workers=args.jobs) as pool:
        futures = [pool.submit(transcribe, p, args.force) for p in pdfs]
        for i, fut in enumerate(futures, 1):
            pdf, status, chars = fut.result()
            rel = pdf.name if args.root.is_file() else pdf.relative_to(args.root)
            if status == "ok":
                ok += 1
                print(f"[{i}/{len(pdfs)}] {rel} ({chars} chars)")
            elif status == "skipped":
                skipped += 1
            else:
                errors += 1
                print(f"[{i}/{len(pdfs)}] {rel} {status}", file=sys.stderr)

    print(f"Done: {ok} transcribed, {skipped} up to date, {errors} errors.")
    return 1 if errors else 0


if __name__ == "__main__":
    sys.exit(main())
