"""Parse the JFT v2 PDF text dump into kotoba/kanji JSON assets.

The v2 PDF already ships 3 sentences per entry with hiragana readings and
Indonesian translations, so no LLM generation is needed.
"""
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DUMP = Path(r"D:\tmp\jft_v2.txt")
KOTOBA_OUT = ROOT / "app/src/main/assets/kotoba.json"
KANJI_OUT = ROOT / "app/src/main/assets/kanji.json"

RE_SECTION = re.compile(r"^([1-9])\.\s+(.+)$")
RE_THEME = re.compile(r"^([1-9])\.\s+[A-Z]")
RE_ON = re.compile(r"On:\s*([^Kun]*)")
RE_KUN = re.compile(r"Kun:\s*(.*)")
RE_NUM = re.compile(r"^([1-3])\.\s+(?![A-Z])(.+)$")

KANA = "぀-ゟ"
KANJI_RE = "[一-鿿]"


def has_jp(text):
    return bool(re.search(f"[{KANA}]", text) or re.search(KANJI_RE, text))


def load_dump():
    lines = DUMP.read_text(encoding="utf-8").splitlines()
    return lines


def find_section_start(lines, marker):
    """Return the line index of the last occurrence of `marker` (skips the TOC)."""
    idx = None
    for i, line in enumerate(lines):
        if line.strip().startswith(marker):
            idx = i
    return idx


def split_sections(lines):
    a = find_section_start(lines, "Bagian A. Kotoba")
    b = find_section_start(lines, "Bagian B. Kanji")
    c = find_section_start(lines, "Bagian C. Kanji")
    d = find_section_start(lines, "Bagian D. Kanji")
    return {
        "A": lines[a:b],
        "B": lines[b:c],
        "C": lines[c:d],
        "D": lines[d:],
    }


def parse_kotoba(lines):
    themes = []
    theme = None
    i = 0
    n = len(lines)
    while i < n:
        line = lines[i].strip()
        m = RE_SECTION.match(line)
        if m and RE_THEME.match(line):
            theme = {"title": m.group(2).strip(), "words": []}
            themes.append(theme)
            i += 1
            continue
        # A word entry: surface / reading / meaning
        if theme is not None and has_jp(line) and line and not RE_NUM.match(line) \
                and not line.startswith(("Kata", "Kalimat", "===", "Kotoba &", "Bagian")) \
                and "hiragana" not in line:
            surface = line
            if i + 2 < n:
                reading = lines[i + 1].strip()
                meaning = lines[i + 2].strip()
                if has_jp(reading) and not re.search(KANJI_RE, reading) \
                        and meaning and not has_jp(meaning):
                    sentences = collect_sentences(lines, i + 3)
                    if sentences:
                        theme["words"].append({
                            "surface": surface,
                            "reading": reading,
                            "meaning": meaning,
                            "sentence": sentences[0]["jp"],
                            "sentence_reading": sentences[0]["reading"],
                            "sentences": sentences,
                        })
                        i += 3
                        i = skip_sentence_block(lines, i)
                        continue
        i += 1
    return themes


def collect_sentences(lines, start):
    out = []
    i = start
    n = len(lines)
    while i < n and len(out) < 3:
        m = RE_NUM.match(lines[i].strip())
        if not m:
            break
        jp = m.group(2).strip()
        reading = lines[i + 1].strip() if i + 1 < n else ""
        meaning = lines[i + 2].strip() if i + 2 < n else ""
        if not jp or not reading or not meaning:
            break
        out.append({"jp": jp, "reading": reading, "id": meaning})
        i += 3
    return out


def skip_sentence_block(lines, i):
    n = len(lines)
    while i < n and RE_NUM.match(lines[i].strip()):
        i += 3
    return i


def parse_kanji(lines, level):
    entries = []
    i = 0
    n = len(lines)
    while i < n:
        line = lines[i].strip()
        if len(line) == 1 and re.match(KANJI_RE, line):
            kanji = line
            on, kun, meaning = [], [], ""
            j = i + 1
            while j < n and j < i + 6:
                cur = lines[j].strip()
                if cur.startswith("On:"):
                    on = [x.strip() for x in RE_ON.search(cur).group(1).replace("・", " ").split() if x.strip() and x.strip() != "-"]
                elif cur.startswith("Kun:"):
                    kun_raw = RE_KUN.search(cur).group(1).strip()
                    kun = [x.strip() for x in kun_raw.replace("・", " ").split() if x.strip() and x.strip() != "-"]
                elif cur and not has_jp(cur) and not cur.startswith(("On:", "Kun:")):
                    meaning = cur
                    break
                j += 1
            # Example words then sentences
            words = []
            k = j + 1
            while k < n:
                cur = lines[k].strip()
                if RE_NUM.match(cur):
                    break
                if has_jp(cur) and k + 1 < n:
                    wsurface = cur
                    wrest = lines[k + 1].strip()
                    parts = wrest.split(" ", 1)
                    if len(parts) == 2 and has_jp(parts[0]):
                        words.append({
                            "surface": wsurface,
                            "reading": parts[0],
                            "meaning": parts[1].strip(),
                        })
                        k += 2
                        continue
                k += 1
            sentences = collect_sentences(lines, k)
            if sentences:
                entries.append({
                    "kanji": kanji,
                    "on": on,
                    "kun": kun,
                    "meaning": meaning,
                    "words": words,
                    "sentence": sentences[0]["jp"],
                    "sentence_reading": sentences[0]["reading"],
                    "sentences": sentences,
                    "level": level,
                })
            i = skip_sentence_block(lines, k)
            continue
        i += 1
    return entries


def main():
    lines = load_dump()
    sections = split_sections(lines)

    themes = parse_kotoba(sections["A"])
    words = sum(len(t["words"]) for t in themes)

    kanji = []
    kanji += parse_kanji(sections["B"], "N5")
    kanji += parse_kanji(sections["C"], "N4")
    kanji += parse_kanji(sections["D"], "EXTRA")

    n5 = sum(1 for k in kanji if k["level"] == "N5")
    n4 = sum(1 for k in kanji if k["level"] == "N4")
    extra = sum(1 for k in kanji if k["level"] == "EXTRA")

    print(f"themes={len(themes)} words={words} kanji={len(kanji)} (n5={n5} n4={n4} extra={extra})")

    missing_w = [w["surface"] for t in themes for w in t["words"] if len(w["sentences"]) < 3]
    missing_k = [k["kanji"] for k in kanji if len(k["sentences"]) < 3]
    if missing_w:
        print("words missing sentences:", missing_w[:20])
    if missing_k:
        print("kanji missing sentences:", missing_k[:20])

    KOTOBA_OUT.write_text(json.dumps({"themes": themes}, ensure_ascii=False, indent=1), encoding="utf-8")
    KANJI_OUT.write_text(json.dumps({"kanji": kanji}, ensure_ascii=False, indent=1), encoding="utf-8")
    print("written:", KOTOBA_OUT.name, KANJI_OUT.name)


if __name__ == "__main__":
    sys.exit(main())
