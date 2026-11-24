# Archived prerequisite rules

This standalone reference compiles published U of T prerequisites into static JSON and tests local evaluation in Java 17. The CSC207 prerequisite use case can use the same file format through its own Clean Architecture boundaries. Both the compiler and the archive it reads are included. [Integration design](../../docs/implementation-guide.md#prerequisites-and-academic-history)

| File | Use |
| --- | --- |
| `compile_rules.py` | Standard-library Python publisher. Reads official course prerequisite fields and selected academic notes, parses supported grammar, and emits a versioned tree or a review result. |
| `prerequisites.json` | Generated public data from the same September 5, 2026 catalogue archive already in the repo. Plain UTF-8 JSON containing 8,172 offering records. |
| `capture.json` | Provenance from the original full, 409-page capture. Contains the UTC start/end, completeness, and counts for rebuilding the checked fixture. |
| `coverage.json` | Outcome counts. Compiled 3,632 of 6,352 with published prerequisite text. Of 8,172 total offerings, 1,439 have none listed and 3,101 need review. |
| `PrerequisiteCatalog.java` | Demonstrates file validation, one fixed GET, and a deterministic evaluation against local Java data. A production adapter can split JSON parsing and HTTP from the pure evaluator. |
| `PrerequisiteTest.java`, `test_compiler.py`, `test.sh` | Fixture assertions and a local HTTP refresh check. The tests exercise rules, thresholds, malformed files, ambiguous source text, and recovery after a bad refresh. |

## Rule file and decision semantics

The top-level JSON has a numeric `version` of 2, UTC `capturedAt`, and an `offerings` list. An offering has its official `id`, display `code`, `publishedText`, a `rule`, and optional academic `notes`. Join using the offering ID and confirm the course code. For a live offering, compare the normalized current prerequisite field and relevant notes against this evidence before applying the archived rule. The data contains no student records.

The compiler recognizes exact courses with optional `minGrade`, total `credits`, `scopedCredits`, `cgpa`, and a `fact` requiring explicit confirmation. Groups support `all`, `any`, and `if`. Scoped credit rules use an explicit list of eligible codes or a subject with an optional exact or minimum level. The evaluator counts awarded credits from verified records. Missing metadata can make a shortfall uncertain. Permission or equivalence requires its own confirmed fact. An exclusion on a recorded course is grounds to verify a possible equivalent with the department.

All 8,172 records were scanned. The compiler emits `none` for an explicitly empty requirement and `review` with a reason for the 2,346 unsupported texts, 385 potentially qualifying notes, and 370 missing fields. Some compiled rules still need a grade, credit breakdown, or confirmed approval from the student. The percentages measure supported source shapes.

```json
{
  "op": "any",
  "rules": [
    {"op": "course", "code": "CSC148H1", "minGrade": 60.0},
    {"op": "course", "code": "CSC148H5", "minGrade": 60.0},
    {"op": "course", "code": "CSCA48H3", "minGrade": 60.0},
    {"op": "course", "code": "CSC111H1", "minGrade": 60.0}
  ]
}
```

This is the published CSC207H1 example in this capture. Select the actual offering to perform a check. An AND group fails on a definite failure and succeeds when all children pass. An OR group succeeds on a definite success and fails when all children fail. An unknown child matters when the known children have not already settled the result. Evaluating a known threshold uses the student's earned grade. An unentered grade or a missing code in an incomplete history can keep a condition unknown. A definite failure from a complete history and an unavailable rule are different outcomes.

## Rebuild and test with the team repository

From the repository root, Python 3 can regenerate the file from the supplied catalogue. This example writes to a temporary directory. The raw ZIP and the captured metadata must describe the same collection.

```sh
capture_dir=$(mktemp -d)
unzip -p uoft-ttb-raw-catalogue-2026-09-05.zip courses.json > "$capture_dir/courses.json"
cp reference/prerequisites/capture.json "$capture_dir/coverage.json"
python3 reference/prerequisites/compile_rules.py "$capture_dir" --out "$capture_dir/generated"
cmp "$capture_dir/generated/prerequisites.json" reference/prerequisites/prerequisites.json
```

Run the normal Maven tests once to cache Gson, or set `PREREQ_GSON_JAR` to an existing Gson JAR. The sample script uses Java 17 and Python 3.

```sh
bash reference/prerequisites/test.sh
bash reference/prerequisites/test.sh --http
```

The second command also serves two versions of the file on a temporary loopback port. It changes the grade threshold in a copy of the CSC207H1 wording, recompiles it automatically, and checks that a refresh changes the local answer. It verifies fixed bodyless requests and that the old valid snapshot still works after a malformed refresh. The official catalogue API is demonstrated separately. [Draft API PR #9](https://github.com/efeatalay9/207-course-project/pull/9)

## Integration direction

Periodically regenerate the rule file from an official catalogue capture and publish it at a fixed public URL. Keep a validated copy locally for failed refreshes. The Java app should read rules through its use-case-owned lookup, check offering identity and current wording, and evaluate against the local academic profile. For changed, unsupported, or incomplete requirements, show **Needs review** with the current published evidence.
