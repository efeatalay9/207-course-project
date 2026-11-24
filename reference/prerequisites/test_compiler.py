"""Check ambiguous text and generate a new version from a changed source sentence."""

import json
from pathlib import Path
import sys

from compile_rules import compile_offering, expression


def main():
    for ambiguous in (
            "CSC148H1/CSC148H5, CSC111H1", "[CSC148H1/CSC148H5)", "CSC148H1 except with approval",
            "Minimum grade of 70% in CSC148H1 and CSC111H1",
            "1.0 credit from SOC251H1, SOC252H1 and SOC254H1",
            "1.0 credit at the C-level in FRE courses"):
        try:
            expression(ambiguous, "test")
        except ValueError:
            continue
        raise AssertionError(f"Ambiguous input was accepted: {ambiguous}")

    total_credits, category = compile_offering({
        "id": "credit-test", "code": "TEST100H1",
        "cmCourseInfo": {"prerequisitesText": "Any 4.0 full credits"},
    })
    assert category == "compiled"
    assert total_credits["rule"] == {"op": "credits", "minimum": 4.0}

    facts, category = compile_offering({
        "id": "fact-test", "code": "TEST200H1",
        "cmCourseInfo": {"prerequisitesText": "By audition, In Year 4."},
    })
    assert category == "compiled"
    assert [rule["op"] for rule in facts["rule"]["rules"]] == ["fact", "fact"]
    assert expression("Year 2 or Year 3", "year")["rules"][0]["key"] != expression("Year 3", "year")["key"]

    assert expression("1.0 SOC credit at the 200+ level", "soc") == {
        "op": "scopedCredits", "subject": "SOC", "minimum": 1.0, "minimumLevel": 200}
    assert expression("1.0 credit at the 300-level in ARC courses", "arc") == {
        "op": "scopedCredits", "subject": "ARC", "minimum": 1.0, "level": 300}
    assert expression("15.0 credits and a minimum cGPA of 3.0", "cgpa")["rules"][1] == {
        "op": "cgpa", "minimum": 3.0}
    minimum_grade = expression("Minimum grade of 77% in CSB328H1/CSB340H1/CSB349H1/MGY311Y1/MGY312H1", "csb")
    assert minimum_grade["op"] == "any" and len(minimum_grade["rules"]) == 5
    assert all(rule["minGrade"] == 77.0 for rule in minimum_grade["rules"])
    crs = expression("SOC201H1, SOC202H1, SOC204H1 and 1.0 credit from SOC251H1, SOC252H1, SOC254H1", "soc")
    assert crs["rules"][1] == {
        "op": "scopedCredits", "minimum": 1.0, "codes": ["SOC251H1", "SOC252H1", "SOC254H1"]}
    equiv = expression("PSY201H5 (or equivalent) and PSY290H5", "psy")
    assert equiv["rules"][0]["rules"][1]["label"] == "Approved equivalent to PSY201H5"
    assert equiv["rules"][1] == {"op": "course", "code": "PSY290H5"}
    combo = expression("BIO230H1/BIO255H1, and BIO260H1/HMB265H1", "bio")
    assert combo["op"] == "all" and all(child["op"] == "any" for child in combo["rules"])

    # Text with unsupported academic thresholds must stay unresolved as a whole.
    qualified, category = compile_offering({
        "id": "qualified-test", "code": "TEST300H1",
        "cmCourseInfo": {"prerequisitesText": "15.0 credits and a minimum GPA of 3.0 in APM and MAT courses"},
    })
    assert category == "unsupported_text"
    assert qualified["rule"]["op"] == "review"

    initial = json.loads(Path(sys.argv[1]).read_bytes())
    index, offering = next((i, entry) for i, entry in enumerate(initial["offerings"]) if entry["code"] == "CSC207H1")
    original = offering["publishedText"]
    revised = original.replace("60% or higher in CSC148H1/", "70% or higher in CSC148H1/", 1)
    assert revised != original
    source = {"id": offering["id"], "code": offering["code"], "cmCourseInfo": {"prerequisitesText": revised}}
    compiled, category = compile_offering(source)
    assert category == "compiled"
    assert compiled["rule"]["rules"][0] == {"op": "course", "code": "CSC148H1", "minGrade": 70}
    assert compiled["rule"]["rules"][1:] == offering["rule"]["rules"][1:]
    initial["offerings"][index] = compiled
    Path(sys.argv[2]).write_text(json.dumps(initial, ensure_ascii=False, separators=(",", ":")))
    print("Compiler tests passed: archived rule examples, ambiguous scope, and automatic recompilation after a source edit.")


if __name__ == "__main__":
    main()
