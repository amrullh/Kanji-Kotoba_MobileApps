#!/usr/bin/env python3
"""Parse the PyMuPDF text dump of JFT_Kotoba_Kanji.pdf into app datasets.

Emits app/src/main/assets/kotoba.json (themed words + example sentences) and
app/src/main/assets/kanji.json (N5/N4/extra kanji with readings and examples).

Usage (from the repo root):
    python tools/parse_jft.py <pdf.txt>
"""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path

KANA = "ぁ-ゖァ-ヺー"
KANJI = "一-龯々〆ヵヶ"

RE_ON_KUN = re.compile(r"^(On|Kun):\s*(.*)$")
RE_HEADER = re.compile(
    r"^(Bagian|Urutan kolom|Cara|Catatan|Struktur|Dasar|Saran|Isi dokumen|Sumber rujukan)"
)
RE_SECTION = re.compile(r"^([1-9])\.\s+(.+)$")
# An example word line looks like: "家族 (かぞく) keluarga".
RE_WORD_LINE = re.compile(r"^(.+?)\s*\((.{1,20})\)\s*(.*)$")


KATAKANA_TO_HIRAGANA = {
    ord(k): ord(k) - 0x60
    for k in "".join(chr(c) for c in range(0x30A1, 0x30FF) if chr(c) not in "ヶ")
}

def is_kana(text: str) -> bool:
    return bool(re.fullmatch(rf"[{KANA}]+", text))


def has_jp(text: str) -> bool:
    return bool(re.search(rf"[{KANA}{KANJI}]", text))


def to_hiragana(text: str) -> str:
    return text.translate(KATAKANA_TO_HIRAGANA)


def load_tagger():
    """Morphological analyser used to derive readings the PDF omits."""
    try:
        import unidic
        import fugashi

        return fugashi.Tagger(f"-d {unidic.DICDIR.replace(chr(92), '/')}/")
    except Exception as exc:  # pragma: no cover - optional dependency
        print(f"[warn] furigana generator unavailable: {exc}", file=sys.stderr)
        return None


def reading_of(tagger, sentence: str) -> str:
    """Return the hiragana reading of a Japanese sentence."""
    if tagger is None:
        return ""
    parts: list[str] = []
    for word in tagger(sentence):
        pron = getattr(word.feature, "pron", None)
        parts.append(pron if pron and pron != "*" else word.surface)
    return to_hiragana("".join(parts))


def parse_words(lines: list[str], tagger=None) -> list[dict]:
    """Bagian A: theme > word {surface, reading, meaning, sentence, sentence_reading}."""
    themes: list[dict] = []
    current: dict | None = None
    i = 0
    n = len(lines)
    while i < n:
        line = lines[i].strip()
        # A theme heading is a numbered line with no Japanese text.
        if RE_SECTION.match(line) and not has_jp(line):
            current = {"title": RE_SECTION.match(line).group(2).strip(), "words": []}
            themes.append(current)
            i += 1
            continue
        if current is not None and has_jp(line) and not is_kana(line) and " " not in line:
            block: list[str] = []
            i += 1
            while i < n:
                nxt = lines[i].strip()
                if has_jp(nxt) and not is_kana(nxt) and " " not in nxt:
                    break
                if RE_SECTION.match(nxt) and not has_jp(nxt):
                    break
                if nxt:
                    block.append(nxt)
                i += 1
            entry = build_word(line, block, tagger)
            if entry:
                current["words"].append(entry)
            continue
        i += 1
    return [t for t in themes if t["words"]]


def build_word(surface: str, block: list[str], tagger=None) -> dict:
    reading = block[0] if block and is_kana(block[0]) else ""
    rest = block[1:] if reading else block
    meaning = rest[0] if rest else ""
    # The example sentence is the first Japanese line after the meaning, with
    # its hiragana reading on the following line.
    sentence = ""
    sentence_reading = ""
    for idx, cand in enumerate(rest[1:], start=1):
        if has_jp(cand) and not is_kana(cand):
            sentence = cand
            if idx + 1 < len(rest) and is_kana(rest[idx + 1]):
                sentence_reading = rest[idx + 1]
            elif tagger is not None:
                sentence_reading = reading_of(tagger, cand)
            break
    return {
        "surface": surface,
        "reading": reading,
        "meaning": meaning,
        "sentence": sentence,
        "sentence_reading": sentence_reading,
    }


def parse_kanji(lines: list[str], level: str) -> list[dict]:
    """Bagian B/C/D: single-kanji entries with on/kun readings and examples."""
    entries: list[dict] = []
    i = 0
    n = len(lines)
    while i < n:
        line = lines[i].strip()
        if re.fullmatch(f"[{KANJI}]", line):
            block: list[str] = []
            i += 1
            while i < n:
                nxt = lines[i].strip()
                if re.fullmatch(f"[{KANJI}]", nxt):
                    break
                if RE_HEADER.match(nxt):
                    break
                if nxt:
                    block.append(nxt)
                i += 1
            entry = build_kanji(line, block)
            if entry:
                entry["level"] = level
                entries.append(entry)
            continue
        i += 1
    return entries


def build_kanji(kanji: str, block: list[str], tagger=None) -> dict:
    on_readings: list[str] = []
    kun_readings: list[str] = []
    for raw in block:
        m = RE_ON_KUN.match(raw)
        if not m:
            continue
        values = [v for v in re.split(r"[・、]", m.group(2)) if v and v != "-"]
        (on_readings if m.group(1) == "On" else kun_readings).extend(values)

    body = [b for b in block if not RE_ON_KUN.match(b) and b]
    meaning = body[0] if body else ""
    words: list[dict] = []
    for raw in body[1:]:
        m = RE_WORD_LINE.match(raw)
        if not m:
            continue
        if not has_jp(m.group(1)):
            continue
        words.append({
            "surface": m.group(1).strip(),
            "reading": m.group(2).strip(),
            "meaning": m.group(3).strip(),
        })
    # The example sentence is a Japanese line that is neither an example word
    # nor its reading; the sentence reading follows it.
    word_surfaces = {w["surface"] for w in words}
    tail = [
        t for t in body[1:]
        if has_jp(t) and t not in word_surfaces and not RE_WORD_LINE.match(t)
    ]
    sentence = tail[0] if tail else ""
    sentence_reading = tail[1] if len(tail) > 1 else ""
    if sentence and not sentence_reading and tagger is not None:
        sentence_reading = reading_of(tagger, sentence)
    return {
        "kanji": kanji,
        "on": on_readings,
        "kun": kun_readings,
        "meaning": meaning,
        "words": words,
        "sentence": sentence,
        "sentence_reading": sentence_reading,
    }


def split_sections(text: str) -> tuple[list[str], list[str], list[str]]:
    lines = text.splitlines()
    # The table of contents also lists "Bagian B/C", so take the LAST match.
    b = max(i for i, l in enumerate(lines) if l.startswith("Bagian B. Kanji dasar"))
    c = max(i for i, l in enumerate(lines) if l.startswith("Bagian C. Kanji level"))
    d = max(i for i, l in enumerate(lines) if l.startswith("Bagian D. Kanji tambahan"))
    return lines[:b], lines[b:c], lines[c:d], lines[d:]


def main() -> None:
    src = Path(sys.argv[1])
    text = src.read_text(encoding="utf-8")
    a_lines, b_lines, c_lines, d_lines = split_sections(text)
    tagger = load_tagger()
    themes = parse_words(a_lines, tagger)
    n5 = parse_kanji(b_lines, "N5")
    n4 = parse_kanji(c_lines, "N4")
    extra = parse_kanji(d_lines, "EXTRA")

    assets = Path("app/src/main/assets")
    assets.mkdir(parents=True, exist_ok=True)
    (assets / "kotoba.json").write_text(
        json.dumps({"themes": themes}, ensure_ascii=False, indent=2), encoding="utf-8"
    )
    (assets / "kanji.json").write_text(
        json.dumps(
            {"levels": ["N5", "N4", "EXTRA"], "kanji": n5 + n4 + extra},
            ensure_ascii=False,
            indent=2,
        ),
        encoding="utf-8",
    )
    words = sum(len(t["words"]) for t in themes)
    print(f"themes={len(themes)} words={words}")
    print(f"kanji n5={len(n5)} n4={len(n4)} extra={len(extra)}")
    for t in themes:
        print(f"  {t['title'][:44]:46s} {len(t['words'])} kata")


if __name__ == "__main__":
    main()
