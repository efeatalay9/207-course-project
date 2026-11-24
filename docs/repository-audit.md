# CSC207 timetable repository audit

Reviewed **November 24, 2026** from `main` at **`07ecb4a`**. The local review build combines the live catalogue change, Add Section validation and feedback, and the follow-up tests and style work. The GitHub contribution evidence and earlier baseline measurements are dated separately below. [Project expectations](project-expectations.md) · [Quercus lab guide](quercus-project-guide.md) · [Implementation guide](implementation-guide.md)

## Requirements and evidence

| Requirement or milestone | Current evidence | Work or evidence to finish |
| --- | --- | --- |
| Java desktop GUI and runnable prototype | Java 17, Maven wrapper, Swing `Main`, real-data and sample launchers. Existing exact-code search, autocomplete, basic add and single-session weekly grid and remove. | Rehearse from the shipped ZIP and the offline demo fixture. Verify the redesigned flows in the actual window after implementation. |
| Story and supported product scope | Five proposed complete lead stories and 15 specified use cases. The official timetable checklist tracks gaps by case. The Quercus sample-domains page asks for course staff approval of project scope. | Confirm there is course staff approval for the current scope and agree on the assignments and demo subset. Mark behaviour finished after integration and acceptance checks. |
| Team contract and review trail | The tracked tree and its reviewed history have no `TeamContract.md` or other contract filename. The first-parent history includes search PR #1, basic add #3, weekly grid/remove #7, and documentation PRs #2, #4, #5. | Locate the team's agreed contract and confirm it is in the shared repo. Confirm the status of each person's substantive code PR and review and preserve the evidence. |
| Milestone 1 blueprint and Lab 7 view planning | README, 15 case descriptions, core entities and a design sequence are available. Text sketches show before/after, error states and the shared Swing controls. | Show the sketch and scoped use cases to the TA. The team's feedback and presentation status need confirmation. |
| Milestone 2 prototype and Lab 8 submission | One end-to-end search path has wired code. Substantial PR activity is visible. The draft API branch completed a real course search using the packaged Java code. | Check that a teammate submitted the repo URL to MarkUs `GroupProject`. Verify each member's PR and review, and demonstrate the live search once integrated. |
| Milestone 3 integrated MVP and Lab 9 interactor tests | The local review build has direct JUnit tests for all five existing interactors, including Add Section. The five additional lead cases are in the plan. | Integrate and test each lead slice. Show the agreed prototype for TA feedback by the Nov 27 milestone. |
| API integration | On this baseline, `JsonCatalogLoader` reads the bundled ZIP. [Draft PR #9](https://github.com/efeatalay9/207-course-project/pull/9) adds the official TTB filter choices and scoped course search. The packaged Java code returned ten sections for Fall 2026 CSC108H1 in Arts & Science and no matches in the UTSC scope. All 101 Maven tests on the PR branch passed. | Review and integrate the live catalogue change. Demonstrate it from the shared build. Keep the archived prerequisite data separate. |
| Data persistence | An in-memory `Timetable` keeps choices while the app is open. | Implement save and reopen, validate age/version and identifiers, and demonstrate restoring a plan after restarting Swing. |
| Dependency direction | The compiled `ArchitectureTest` and the Maven suite passed. The review build routes Add Section through an input boundary, validated interactor, output boundary, and presenter. | Use the same pattern for each lead's next case and update the sequence diagrams as the cases land. |
| Clean Architecture reflection and CAVE | Team-specific package names and compiled dependency checks are documented. | The individual quiz is due Dec 8, 11:59 p.m. CAVE can supplement the existing test. Verify package recognition and actual dependencies in any report. |
| Clean Architecture diagrams and design | Dependency diagram and current search flow are in the repo. The proposed prerequisite sequence, SOLID examples, and Week 10 pattern guidance are in text. Memento for preserving an original timetable is a candidate. | Keep diagrams aligned with merged code. Demonstrate two applicable SOLID principles, an implemented pattern for the desired rubric band, and a real extension point. |
| Code quality evidence | Maven uses Checkstyle 10.26.1 with the course's Google rules. The strict run over the local review build's 31 changed Java files passed, with Javadocs and formatting checked. The full report still shows 1,042 production findings and 715 test findings. | Run the scoped check for new PRs and address existing style findings in reviewable groups. Keep a representative peer-reviewed PR and report for the presentation. |
| Milestone 4 coverage by Friday Dec 4 | **115 tests pass. Domain 68/68 lines (100%). Use cases 232/232 (100%).** Counts from JaCoCo 0.8.12 on the combined local review build. | Rerun after integrating the new interactors so they are represented in the presentation evidence. |
| Presentations and individual submissions | Draft rubric, template, demo sequence, and self-contained criteria are recorded. | Select and rehearse speakers and slides, confirm official presentation slot, and plan individual project reflection and peer evaluation submissions via MarkUs. |

The Git tree can establish what code and docs were present. Team agreement, TA demos, MarkUs completion, any off-repo contract, each person's review history and oral contribution need independent evidence from the team. [GitHub baseline](https://github.com/efeatalay9/207-course-project/tree/07ecb4ac4cd94b86e1ea134348e2f34b51ca95cc) · [Course timeline](https://q.utoronto.ca/courses/444184/pages/project-timeline-2)

The November 24 GitHub review check returned a submitted approval with written feedback on each of PRs #3 and #7. It returned no submitted review for the other four merged PRs examined, #1, #2, #4 and #5. This check covers GitHub review submissions on those PRs. Team discussion and other reviews can provide additional evidence. [Add-section PR #3](https://github.com/efeatalay9/207-course-project/pull/3) · [Weekly timetable PR #7](https://github.com/efeatalay9/207-course-project/pull/7) · [Code-review workflow](code-review.md)

## Contribution evidence for assignments

The November 24 ownership review checked commit authors, PR status, and the Java files changed in each merged feature. File counts show the size of a diff. The actual behaviour and layers are the basis for the next assignments. Authorship, review, catalogue work, and drafts appear separately here so the team can also account for paired or branch work.

| Teammate | Evidence in the repo | Proposed next ownership |
| --- | --- | --- |
| Matthew Chen | Built the original search, autocomplete, catalogue adapter and launch/test paths. PR #1 changed 33 production and 11 test Java files. The implementation commit's Git author is `matthewchen`, linked by GitHub to `Potatovender`. [Implementation commit](https://github.com/efeatalay9/207-course-project/commit/24fc0880d05bfedd24d5674807e44955f8ef6c40) · [PR #1](https://github.com/efeatalay9/207-course-project/pull/1) | Lead **Find Courses That Fit** as an extension of his search flow. Review the live adapter's use of his search interfaces and extend the remaining filters after fit works. |
| Jingyan Lin | Built the add-section flow and selected-section state in PR #3, which changed eight production Java files. Also contributed README work in PRs #2 and #5. [PR #3](https://github.com/efeatalay9/207-course-project/pull/3) · [PR #2](https://github.com/efeatalay9/207-course-project/pull/2) · [PR #5](https://github.com/efeatalay9/207-course-project/pull/5) | Lead **Check Prerequisites** in the offering view. Build on the tested Add Section flow and coordinate offering and timetable UI changes with Vincent. |
| Vincent He | Built the weekly grid, session picker, offering lookup, and removal. PR #7 changed 23 production and six test Java files. Also updated the use-case notes and reviewed PR #3. [PR #7](https://github.com/efeatalay9/207-course-project/pull/7) · [PR #4](https://github.com/efeatalay9/207-course-project/pull/4) · [PR #3 review](https://github.com/efeatalay9/207-course-project/pull/3) | Lead **Check Walking Time** in his grid. Add personal blocks and expand conflict display after the walking path works. |
| Efe Atalay | Set up the repo, added the bundled catalogue, and reviewed PR #7. The live API work changes 13 production and one test Java file in draft PR #9, which is pending review. [Catalogue commit](https://github.com/efeatalay9/207-course-project/commit/9464e14) · [PR #7 review](https://github.com/efeatalay9/207-course-project/pull/7) · [Draft PR #9](https://github.com/efeatalay9/207-course-project/pull/9) | Lead the first shared scheduling check and take **Generate and Compare** through its own tested Swing feature PR. |
| Leon Ren | The reviewed commit authors and PRs did not identify a merged Java feature attributable to Leon. Confirm branch and paired work with the team. | Start with a complete **Save and Open Plan** PR, which uses the existing selection state. Then use that work to lead **Find Replacement**. |

The five stories still need their own working implementation and test evidence. Keep the catalogue and documentation integration in the combined PR and have each lead own their subsequent feature changes. [First assignments and dates](project-plan.md#first-work-session-after-the-pr)

## Coverage of the documentation and reference transfer

| Requested context | Where it is in the checkout |
| --- | --- |
| Complete final product and build path | [Project specification](project-plan.md) gives the student walkthrough, core terms, official parity checklist, all 15 cases, ownership, and current statuses. Its [timeline](project-plan.md#timeline-and-completion-evidence) gives course deadlines, proposed team milestones, integration dependencies, and completion evidence. |
| All relevant course expectations, project pages and Labs 7–9 | [Expectations and rubric](project-expectations.md) and the [Quercus guide](quercus-project-guide.md) record the obligations in text. The [lecture notes](course-resources/lecture-slides.md) cover four relevant PDFs from the weekly modules and the lab review guidance. The [source index](course-resources/README.md) lists the underlying course material. |
| Course notes, novice Java and code review | The [course notes summary](course-notes-summary.md) covers Chapters 0–14 and supplemental guidance. The [implementation guide](implementation-guide.md) keeps the proposed design in basic Java 17 and Swing. The [code-review guide](code-review.md) supplies both author and reviewer instructions. |
| Prerequisites and walking | The [archived rule data and evaluator](../reference/prerequisites/README.md) and the [catalogue examples and walking routes](../reference/timetable-data/README.md) supply the inputs for the course use cases. |

The working guides live in the repository as searchable text. Original course files are preserved in the private local materials alongside it. The team's Java feature implementation, TA approval, actual reviews, and course submissions are separate milestones. The prototype tests establish the reference behaviour and leave the integrated app's external call to verify.

## Clean Architecture inspection

- The search and autocomplete controllers call input interfaces and the interactors return through output boundaries. The presenters update adapter-layer view models. `SearchApplication` wires an in-memory JSON catalogue implementation through narrow data access interfaces.
- `ViewTimetableInteractor` obtains offerings through the lookup port, combines selection and meeting data, and publishes a response. Remove also publishes an outcome. The Swing panel explicitly refreshes its display from the view model as part of the event action.
- `AddSectionInteractor` accepts the offering ID and section name, verifies them through the shared `OfferingLookup`, and publishes a typed outcome. `AddSectionPresenter` updates the timetable status. The tests cover successful, duplicate, missing, cancelled, TBA, and failed catalogue lookups.
- Use-case ports should express operations and outcomes the interactor needs. Put Swing formatting and shared UI state in adapter/view code. Keep external field names, JS, gzip, Gson and HTTP status handling at the data-access edge. Use case-specific observers if many UI actions refresh the same shared view model.

[Team dependency diagram and proposed flow](architecture.md)

## Coverage gaps on baseline

| File | Uncovered lines on `07ecb4a` | What the new tests cover |
| --- | --- | --- |
| [AddSectionInteractor.java](../src/main/java/usecase/addsection/AddSectionInteractor.java) | 14, 15, 16, 20 | The constructor and execution path, including adds, duplicates, and validation outcomes that preserve timetable state. |
| [SearchCoursesResponse.java](../src/main/java/usecase/searchcourses/SearchCoursesResponse.java) | 17 | Invalid result invariant. A failed search cannot carry offerings. |
| [SuggestCourseCodesResponse.java](../src/main/java/usecase/suggestcoursecodes/SuggestCourseCodesResponse.java) | 11 | Invalid result invariant. An unavailable suggestion service cannot report a list of suggestions. |

The baseline passed 96 tests, with domain coverage of 67/67 lines and use-case coverage of 166/172 lines. The combined local build covered every line in both layers. [Reproduction commands](testing.md#style-and-coverage-work)
