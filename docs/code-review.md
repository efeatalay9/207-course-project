# How we review a pull request

A pull request (PR) is a proposal to merge changes into the shared code. The author shows what the student can now do and how to reproduce it. A teammate reads the code, tries the behaviour, and records their review in GitHub. Use this workflow alongside the team's agreed contract.

### Course review standard

The six review checks below come from the **Code Reviews** slides in the Lab 8 presentation, *CSC207 Project Rubrics — Lab Summary 2026*, pp. 9–10. The slides ask reviewers to check style, correctness, tests and coverage, Clean Architecture and SOLID, Javadoc, and clarity of the PR information. Feedback should name a concrete problem and offer an actionable next step. The project timeline asks each member for a substantial PR and at least one teammate review. The author steps and template below apply these checks to the timetable app and follow the official Git notes for the PR workflow. Follow the team's contract for agreed review and merge arrangements. [Lab 8 grading slides](https://q.utoronto.ca/courses/444184/files/46149081?wrap=1) · [Project timeline](https://q.utoronto.ca/courses/444184/pages/project-timeline-2) · [Course Git notes](https://github.com/CSC207-UofT/207-course-notes/blob/8dbb627ee1e099ebbeefe52feec1d13b6459b74a/00-introduction-to-git.md)

## Author checklist

1. Start from the current `main` on your own feature branch. Pick a use case and name the actual student outcome. If it changes an interface used by other cases, agree on the fields and behaviour with the affected leads early.
2. Keep the diff to one understandable change. Carry a small user flow through the relevant domain rules, interactor, controller, presenter, view, and tests. If the work spans several PRs, state what works in each and what another PR supplies.
3. Read your diff, run `./test.sh`, and run the Maven Checkstyle check on the Java files you changed. Document new public types and methods. For a Swing change, launch `./run.sh --sample` or the stated fixture and try it. For a parser, file or HTTP change, exercise normal data and malformed, missing or stale data as appropriate. Record the test and style results in the PR. [Maven commands](testing.md#style-and-coverage-work)
4. Write the description so someone else can repeat what you did. Say which fixture, steps, and expected results the reviewer should use. Mention scope limits, pending dependencies, and any known failure honestly. Use fictional profile data in screenshots and logs.
5. Request a teammate's review once there is something they can verify. A draft PR is fine for an early design question. Tell the reviewer which part you want feedback on and whether they can run it independently.

## What to put in the PR description

```markdown
## Student outcome
UC-__, one sentence about what a student can now do.

## Changes and design
- Main files or responsibilities that changed.
- Any shared interface, data format, dependency, or privacy impact.

## How to check
- Fixture and exact steps, with expected success and one important alternate result.
- ./test.sh result, plus style or coverage evidence when relevant.
- One screenshot for a meaningful UI change, using fictional student data.

## Open points
- A known limit, decision for this reviewer, or dependency on another PR.
```

For example, a Find Replacement PR can tell the reviewer to mark the original tutorial unavailable, check that the preview preserves its lecture and other courses, cancel once to verify the original timetable remains, then accept and reopen the backup. If availability data is stale, the reviewer should see **Verify availability** on the proposed choice.

## Reviewer checklist

Read the description and use-case acceptance example before you open **Files changed**. Trace one action from the Swing event through the controller, interactor, data interfaces, and presenter back to the view. Open the affected tests and try the normal path and a consequential alternate path. Focus on the parts of the checklist the change touches.

| Check | What to look for in this project |
| --- | --- |
| Behaviour and correctness | Does the promised student operation actually work? Try a linked tutorial that overlaps, a missing grade, a ten-minute walk, a stale section or a corrupt file when relevant. Check the plan state before and after any failed action, cancelled preview, or failed refresh. |
| Tests and coverage | Check that the interactor tests success and important alternate results. The tests should verify that the right presenter method received the right outcome. Check important domain branches and use `./test.sh` or the documented JaCoCo command. Read assertions as well as metrics. |
| Dependencies and SOLID | The entities and use cases use ordinary Java types. The interactor owns the input, output, and storage interfaces it needs. Swing, JSON, and HTTP stay in the outside adapters. Reuse scheduling rules shared by fit, generation, and replacement. Ask which actual change a new class or interface makes simpler. |
| Style and Javadoc | Apply the agreed Google Java Style checks, use clear names and small methods, and document public behaviour and constraints. Focus style requests on the changed code. Keep wholesale reformatting reviewable on its own. |
| Source and student data | Check the source field, offering or section key, and date for decisions based on catalogue data. Missing source facts stay visible. Profiles and grades stay local, and exports and any network writes include only their intended data. |
| PR clarity | Can another member see what changed, the reason for the design, the exact fixture, and what they should expect? Ask for a reproducible example if any are unclear. |

[How to run tests and measure coverage](testing.md) · [Timetable acceptance examples](project-plan.md#lead-use-cases) · [Runtime and package direction](architecture.md)

## Write feedback and finish the review

- **Blocks merge.** Describe the concrete input, current result, expected result, and a proposed fix or regression test. Example. “With the tutorial linked to LEC0101 marked full, the picker proposes a tutorial linked only to LEC0201 while keeping LEC0101. Please check the full bundle and add this case to the repair test.”
- **Suggestion.** Mark optional improvements as optional and tie them to a benefit. Example. “Consider naming this `publishedAt` so a reader can tell that this is the publisher's timestamp.”
- **Question.** Ask for rationale or missing evidence. Example. “What should happen when only the building is known? Please include that fixture in the check steps.”
- **Approval.** Say which behaviour and tests you checked. Approve once the change is sound and the blocking points are resolved. In GitHub, use **Comment** for feedback or questions without a merge blocker and **Request changes** for a change needed before merge.

The author responds in the affected thread, explains the change or tradeoff, and requests another look at fixes when warranted. The reviewer checks the follow-up against the reported case. Before merging, follow the team contract, check conflicts and test status, and keep the PR description aligned with the final diff. After merge, verify the feature on `main` and update its use-case status. Keep links to actual substantial PRs and reviews for the presentation and each member's reflection.
