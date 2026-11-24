"""Compile a saved U of T catalogue into the prerequisite file served to Java."""

import argparse
from collections import Counter
import gzip
from html.parser import HTMLParser
import json
from pathlib import Path
import re
from tempfile import NamedTemporaryFile
import unicodedata


class CatalogueText(HTMLParser):
    def __init__(self):
        super().__init__()
        self.parts = []

    def handle_data(self, data):
        self.parts.append(data)

    def handle_starttag(self, tag, attrs):
        if tag in {"p", "br", "div", "li", "td", "tr"}:
            self.parts.append("\n")

    def handle_endtag(self, tag):
        if tag in {"p", "div", "li", "td", "tr"}:
            self.parts.append("\n")


def plain(html):
    parser = CatalogueText()
    parser.feed(html or "")
    parser.close()
    text = unicodedata.normalize("NFKC", "".join(parser.parts)).replace("\u200b", "")
    return "\n".join(part for line in text.splitlines() if (part := " ".join(line.split())))


CODE = r"[A-Z]{3,4}[0-9]{2,3}[HY][0-9]"
NUMBER = r"[0-9]{1,3}(?:\.[0-9]{1,3})?"
GRADE_INTRO = r"(?:at least|(?:a )?(?:minimum|min\.?)(?: grade)?(?: of)?)"
GRADE = re.compile(rf"{GRADE_INTRO} ({NUMBER})% in ({CODE})", re.I)
GRADE_AFTER_CODE = re.compile(rf"({CODE})\s*\(\s*({NUMBER})%\s*\)", re.I)
GRADE_GROUP = re.compile(rf"{GRADE_INTRO} ({NUMBER})% in (\(.+\)|\[.+\])", re.I)
GRADE_ALTERNATIVES = re.compile(rf"{GRADE_INTRO} ({NUMBER})% in ({CODE}(?:\s*(?:/|\bor\b)\s*{CODE})+)", re.I)
CREDIT_INTRO = r"(?:completion of )?(?:(?:at least|any|(?:a )?minimum of) )?"
CREDITS = re.compile(rf"{CREDIT_INTRO}({NUMBER}) (?:full )?credits?", re.I)
CGPA = re.compile(rf"(?:(?:a )?minimum cGPA of |(?:a )?cGPA of at least )({NUMBER})", re.I)
CREDITS_WITH_CGPA = re.compile(rf"({CREDIT_INTRO}{NUMBER} (?:full )?credits?) with (.+)", re.I)
SUBJECT = r"(?P<subject>(?-i:[A-Z]{3,4}))"
LEVEL = r"(?P<level>[1-9]00)(?P<higher>\+| or higher)?[ -]level"
AMOUNT = rf"{CREDIT_INTRO}(?P<minimum>{NUMBER}) "
SUBJECT_CREDITS = (
    re.compile(rf"{AMOUNT}{SUBJECT} credits?(?: at (?:the )?{LEVEL})?", re.I),
    re.compile(rf"{AMOUNT}credits? in {SUBJECT}(?: courses?)?(?: at (?:the )?{LEVEL})?", re.I),
    re.compile(rf"{AMOUNT}credits? at (?:the )?{LEVEL} in {SUBJECT}(?: courses?)?", re.I),
)
GENERIC_CREDIT_WORDS = {"ALL", "ANY", "FULL", "GPA", "CGPA", "NON"}
CREDIT_CODE_LIST = re.compile(rf"{AMOUNT}credits? from:?\s+(?P<courses>.+)", re.I)
TRAILING_CREDIT_LIST = re.compile(rf"(.+?) and ({AMOUNT}credits? from:?\s+.+)", re.I)
CODE_LIST = re.compile(rf"{CODE}(?:\s*(?:[,/]|\bor\b)\s*{CODE})*", re.I)
INCLUDED_COURSES = (
    re.compile(rf"({CREDIT_INTRO}{NUMBER} .+?),? including:?\s+(.+)", re.I),
    re.compile(rf"({CREDIT_INTRO}{NUMBER} .+?) \(\s*including:?\s+(.+)\)", re.I),
)
EQUIVALENT = re.compile(r"(.+?) (?:\(or (?:an? )?(?:approved )?equivalent\)|or (?:an? )?(?:approved )?equivalent)", re.I)
FACT = re.compile(
    r"equivalent programming experience"
    r"|(?:by )?(?:permission|consent|approval) of (?:the )?(?:course )?instructor"
    r"|(?:by )?(?:permission|consent|approval) (?:of|from) (?:the )?(?:course )?(?:department|division|coordinator|program coordinator|program director|supervisor(?: of studies)?|associate chair(?: for undergraduate studies)?|college program director|institute director|undergraduate co-ordinator|undergraduate coordinator)"
    r"|students must obtain consent from the supervisor of studies before registering for this course"
    r"|by audition"
    r"|(?:in )?year [1-4]"
    r"|[1-4](?:st|nd|rd|th) year standing"
    r"|(?:admission to|enrolment in) [a-z0-9][a-z0-9 &’'-]{1,80}",
    re.I,
)
ENGINEERING = re.compile(r"(?:^|\n)Prerequisites? for Faculty of Applied Science and Engineering students:\s*", re.I)
ACADEMIC_NOTE = re.compile(
    r"\b(?:pre[- ]?requisites?|mandatory academic requirements?|must (?:have )?completed?)\b"
    r"|\brecommended as preparation\b"
    r"|\b(?:permission|approval|consent) (?:of|from) [^.!?]{1,90} (?:is|will be) required\b"
    r"|\b(?:may|can) be (?:admitted|enrolled) with (?:the )?(?:permission|approval|consent)\b",
    re.I,
)
SEPARATORS = re.compile(r",\s*and\b|[()\[\];,/]|\b(?:and|or)\b", re.I)


def outer_parts(text):
    """Separate only at the outer group level. Reject mismatched grouping."""
    parts, operators = [], []
    start = 0
    closers = []
    complete_group = False
    for match in SEPARATORS.finditer(text):
        token = match.group().lower()
        if token in ("(", "["):
            closers.append(")" if token == "(" else "]")
        elif token in (")", "]"):
            if not closers or closers.pop() != token:
                raise ValueError("mismatched closing bracket")
            if not closers and match.end() == len(text) and text.startswith(("(", "[")) and not parts:
                complete_group = True
        elif not closers:
            parts.append(text[start:match.start()].strip())
            operators.append(", and" if token.startswith(",") and token != "," else token)
            start = match.end()
    if closers:
        raise ValueError("unclosed bracket")
    return parts + [text[start:].strip()], operators, complete_group


def groups_at(parts, operators, separator):
    groups = [parts[0]]
    for operator, part in zip(operators, parts[1:]):
        if operator == separator:
            groups.append(part)
        else:
            groups[-1] += f" {operator} {part}"
    return groups


def course_only(rule):
    if rule["op"] == "course":
        return True
    return rule["op"] in {"all", "any"} and all(course_only(child) for child in rule["rules"])


def grade_for_group(rule, minimum):
    if rule["op"] == "course" and "minGrade" not in rule:
        return {**rule, "minGrade": minimum}
    if rule["op"] in {"all", "any"}:
        return {**rule, "rules": [grade_for_group(child, minimum) for child in rule["rules"]]}
    raise ValueError("Grade threshold does not clearly apply to every listed course")


def confirmed_fact(offering_id, text):
    slug = re.sub(r"[^a-z0-9]+", "-", text.lower()).strip("-")
    return {"op": "fact", "key": f"{offering_id}:{slug}", "label": text}


def including_requirement(rule, subject):
    if rule["op"] == "course":
        return subject is None or re.match(r"^[A-Z]+", rule["code"])[0] == subject
    if rule["op"] == "scopedCredits":
        return subject is None or rule.get("subject") == subject
    return rule["op"] in {"all", "any"} and all(including_requirement(child, subject) for child in rule["rules"])


def expression(text, offering_id, depth=0):
    if depth > 24:
        raise ValueError("too many nested groups")
    text = " ".join(text.split()).strip().removesuffix(".").strip()
    # Treat a grade qualifier as part of a single course predicate.
    text = re.sub(rf"({NUMBER})% (?:or (?:higher|above|greater)) in\b", r"at least \1% in", text, flags=re.I)
    text = re.sub(r",\s+or\s+", " or ", text, flags=re.I)
    if re.fullmatch(CODE, text, re.I):
        return {"op": "course", "code": text.upper()}
    if match := GRADE.fullmatch(text):
        minimum = float(match[1])
        if minimum <= 100:
            return {"op": "course", "code": match[2].upper(), "minGrade": minimum}
    if match := GRADE_AFTER_CODE.fullmatch(text):
        minimum = float(match[2])
        if minimum <= 100:
            return {"op": "course", "code": match[1].upper(), "minGrade": minimum}
    if match := GRADE_GROUP.fullmatch(text) or GRADE_ALTERNATIVES.fullmatch(text):
        minimum = float(match[1])
        if minimum <= 100:
            group = expression(match[2], offering_id, depth + 1)
            return grade_for_group(group, minimum)
    if match := CREDITS.fullmatch(text):
        minimum = float(match[1])
        if 0 < minimum <= 100:
            return {"op": "credits", "minimum": minimum}
    if match := CGPA.fullmatch(text):
        minimum = float(match[1])
        if 0 < minimum <= 4:
            return {"op": "cgpa", "minimum": minimum}
    if (match := CREDITS_WITH_CGPA.fullmatch(text)) and CGPA.fullmatch(match[2]):
        return {"op": "all", "rules": [expression(part, offering_id, depth + 1) for part in match.groups()]}
    for pattern in SUBJECT_CREDITS:
        if match := pattern.fullmatch(text):
            minimum = float(match["minimum"])
            if 0 < minimum <= 100 and match["subject"] not in GENERIC_CREDIT_WORDS:
                rule = {"op": "scopedCredits", "subject": match["subject"], "minimum": minimum}
                if match["level"]:
                    key = "minimumLevel" if match["higher"] else "level"
                    rule[key] = int(match["level"])
                return rule
    if match := CREDIT_CODE_LIST.fullmatch(text):
        minimum = float(match["minimum"])
        codes = match["courses"]
        if codes.startswith(("(", "[")) and codes.endswith(")" if codes.startswith("(") else "]"):
            codes = codes[1:-1].strip()
        if 0 < minimum <= 100 and CODE_LIST.fullmatch(codes):
            return {"op": "scopedCredits", "codes": sorted(set(re.findall(CODE, codes.upper()))), "minimum": minimum}
    if match := TRAILING_CREDIT_LIST.fullmatch(text):
        try:
            other = expression(match[1].removesuffix(","), offering_id, depth + 1)
            quota = expression(match[2], offering_id, depth + 1)
        except ValueError:
            pass
        else:
            if "codes" in quota and (other["op"] != "any" or outer_parts(match[1].removesuffix(","))[2]):
                return {"op": "all", "rules": [other, quota]}
    for pattern in INCLUDED_COURSES:
        if match := pattern.fullmatch(text):
            try:
                credit_threshold = expression(match[1], offering_id, depth + 1)
                included = expression(match[2], offering_id, depth + 1)
            except ValueError:
                continue
            if (credit_threshold["op"] in {"credits", "scopedCredits"} and "codes" not in credit_threshold
                    and including_requirement(included, credit_threshold.get("subject"))):
                return {"op": "all", "rules": [credit_threshold, included]}
    if match := EQUIVALENT.fullmatch(text):
        try:
            requirement = expression(match[1], offering_id, depth + 1)
        except ValueError:
            pass
        else:
            if course_only(requirement) and (requirement["op"] == "course" or outer_parts(match[1])[2]):
                return {"op": "any", "rules": [requirement, confirmed_fact(offering_id, f"Approved equivalent to {match[1]}")]}
    fact_text = re.sub(r": https?://\S+$", "", text) if text.lower().startswith("admission to ") else text
    if FACT.fullmatch(fact_text) and not re.search(rf"\b{CODE}\b", fact_text, re.I):
        return confirmed_fact(offering_id, fact_text)

    parts, operators, wrapped = outer_parts(text)
    if wrapped:
        return expression(text[1:-1], offering_id, depth + 1)
    for separator in (";", ", and"):
        if separator in operators:
            pieces = groups_at(parts, operators, separator)
            return {"op": "all", "rules": [expression(piece, offering_id, depth + 1) for piece in pieces]}
    for part, operator, following in zip(parts, operators, parts[1:]):
        if (operator in {",", "/", "or"} and CREDIT_CODE_LIST.fullmatch(part)
                and re.fullmatch(CODE, following, re.I)):
            raise ValueError("Unclear end of credit quota course list")
    if operators:
        kinds = {"any" if operator in ("/", "or") else "all" for operator in operators}
        if len(kinds) == 1:
            children = [expression(piece, offering_id, depth + 1) for piece in parts]
            if (GRADE.fullmatch(parts[0]) and any(
                    child["op"] == "course" and "minGrade" not in child and not outer_parts(piece)[2]
                    for piece, child in zip(parts[1:], children[1:]))):
                raise ValueError("Unclear grade threshold scope")
            return {"op": kinds.pop(), "rules": children}
    raise ValueError("unsupported text or ambiguous grouping")


def convert(text, offering_id):
    if match := ENGINEERING.search(text):
        return {
            "op": "if",
            "condition": {"op": "fact", "key": "faculty.is-engineering", "label": "Faculty of Applied Science and Engineering student"},
            "then": expression(text[match.end():], offering_id),
            "else": expression(text[:match.start()], offering_id),
        }
    return expression(text, offering_id)


def compile_offering(row):
    fields = row.get("cmCourseInfo") or {}
    raw = fields.get("prerequisitesText")
    text = plain(raw)
    notes = [plain(fields.get("note"))]
    notes.extend(plain(note.get("content")) for note in (row.get("notes") or []) if note.get("type") == "COURSE")
    relevant = [note for note in notes if ACADEMIC_NOTE.search(note)]
    entry = {"id": row["id"], "code": row["code"], "publishedText": text}

    if relevant:
        entry["notes"] = list(dict.fromkeys(relevant))
        rule = {"op": "review", "reason": "An official note may qualify or change the prerequisite"}
        category = "qualifying_note"
    elif raw is None:
        rule = {"op": "review", "reason": "Prerequisite field absent from the source"}
        category = "missing_field"
    elif re.fullmatch(r"(?:none|n/?a|not applicable)?\.?", text, re.I):
        rule = {"op": "none"}
        category = "none_listed"
    else:
        try:
            rule = convert(text, row["id"])
            category = "compiled"
        except ValueError:
            rule = {"op": "review", "reason": "Source text could not be converted with the supported grammar"}
            category = "unsupported_text"

    entry["rule"] = rule
    return entry, category


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("capture", type=Path, help="Directory containing the saved courses.json and coverage.json")
    parser.add_argument("--out", type=Path, default=Path(__file__).resolve().parent)
    args = parser.parse_args()
    coverage = json.loads((args.capture / "coverage.json").read_text())
    rows = json.loads((args.capture / "courses.json").read_bytes())
    if (not coverage["complete"] or not rows or len(rows) != coverage["recordsCollected"]
            or len(rows) != len({row["id"] for row in rows})):
        raise ValueError("Expected a complete capture with unique offering IDs")

    entries, categories = zip(*(compile_offering(row) for row in rows))
    counts = Counter(categories)
    document = {"version": 2, "capturedAt": coverage["finishedAt"], "offerings": entries}
    encoded = json.dumps(document, ensure_ascii=False, separators=(",", ":"), allow_nan=False).encode()
    stats = {
        "offerings": len(rows),
        "with_published_text": sum(bool(entry["publishedText"]) for entry in entries),
        "compiled": counts["compiled"],
        "none_listed": counts["none_listed"],
        "unresolved": len(rows) - counts["compiled"] - counts["none_listed"],
        "breakdown": dict(sorted(counts.items())),
        "bytes": len(encoded),
        "gzip_bytes": len(gzip.compress(encoded, mtime=0)),
    }
    args.out.mkdir(parents=True, exist_ok=True)
    with NamedTemporaryFile(dir=args.out, prefix=".prerequisites-", delete=False) as temp:
        temp.write(encoded)
    Path(temp.name).replace(args.out / "prerequisites.json")
    (args.out / "coverage.json").write_text(json.dumps(stats, indent=2) + "\n")
    print(json.dumps(stats, indent=2))


if __name__ == "__main__":
    main()
