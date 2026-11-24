# U of T Timetable Builder

**Domain:** Education and student productivity.

This CSC207 Java Swing project helps U of T students browse courses, plan a weekly timetable, and prepare alternatives for enrolment. The target includes the official builder's planning workflows plus checks for course fit, prerequisites and walking time, comparison of generated schedules, and fast replacement of an unavailable section.

- [First work session after the PR](docs/project-plan.md#first-work-session-after-the-pr). This names the first shared coding task, what each lead can start, and the targets through November 27.
- [Start here for the complete project](docs/project-plan.md). Follow a student through the finished app, then read the official feature checklist, 15 use cases, the five proposed leads, and the dated build and presentation plan. The [UI sketches](docs/ui-sketches.md) show how the Swing views will look before and after an action.
- [Java implementation guide](docs/implementation-guide.md) walks through straightforward Java approaches for section linkage, schedule generation, prerequisites, walking, availability, persistence, and export. The team is using basic Java 17 and Swing.
- [Course requirements and rubric](docs/project-expectations.md), [Labs 7–9 and project guidance](docs/quercus-project-guide.md), and the [course notes summary](docs/course-notes-summary.md) contain the criteria and technical guidance in searchable text. The [lecture slide guide](docs/course-resources/lecture-slides.md) summarizes four relevant decks with page references. [Source index](docs/course-resources/README.md)
- [Repository audit](docs/repository-audit.md), [architecture](docs/architecture.md), and [testing guide](docs/testing.md) describe what works on the reviewed baseline, open requirements, and the measured checks.
- [Timetable data and walking routes](reference/timetable-data/README.md) and the [archived prerequisite rules and evaluator](reference/prerequisites/README.md) support the five planned cases.

## Team, user stories, and MVP use cases

Each proposed lead owns a complete user interaction, including the interactor, controller, presenter, and Swing integration. The [plan](docs/project-plan.md#lead-use-cases) specifies the actor, preconditions, trigger, outcome, main steps, and extensions for each case. The first assignments reflect the Java code already in the repo and give Efe and Leon a clear path to their own feature PRs. Confirm the assignments with the team. [Contribution evidence](docs/repository-audit.md#contribution-evidence-for-assignments)

| Team member | User story | Proposed lead use case | Starting point |
| --- | --- | --- | --- |
| Matthew Chen | As a student, I want to find courses I can add around my existing timetable so I can narrow my choices. | [Find Courses That Fit My Timetable](docs/project-plan.md#uc-05-find-courses-that-fit-my-timetable) | Extend the search and autocomplete stack he built in [PR #1](https://github.com/efeatalay9/207-course-project/pull/1). |
| Jingyan Lin | As a student, I want to check the prerequisites for an offering so I know what I already satisfy and what I need to verify. | [Check Prerequisites Against My Completed Courses](docs/project-plan.md#uc-07-check-prerequisites-against-my-completed-courses) | Add the academic check to the offering and section selection flow built in [PR #3](https://github.com/efeatalay9/207-course-project/pull/3). |
| Vincent He | As a student, I want to see the time needed to walk between classes so I can choose sections I can reach on time. | [Check Walking Time Between Classes](docs/project-plan.md#uc-08-check-walking-time-between-classes) | Extend the weekly grid and meeting lookup he built in [PR #7](https://github.com/efeatalay9/207-course-project/pull/7). |
| Efe Atalay | As a student, I want to compare workable timetables so I can pick a week with fewer awkward gaps. | [Compare Generated Timetables](docs/project-plan.md#uc-11-compare-generated-timetables) | Start the shared schedule check and lead generation and comparison. The catalogue setup is on `main`, and live search is in [draft PR #9](https://github.com/efeatalay9/207-course-project/pull/9). |
| Leon Ren | As a student, I want to replace a section that fills up so I can recover my saved timetable quickly. | [Replace a Section That Filled Up](docs/project-plan.md#uc-13-replace-a-section-that-filled-up) | Start with a complete [Save and Open Plan](docs/project-plan.md#uc-12-save-and-open-a-plan) feature, then reuse the saved plan for replacement. |

### Official planning workflows

The project plan assigns these workflows to leads and gives each one an acceptance check. All five lead cases above are part of the planned MVP.

| Area | Planned workflow | Status on the reviewed main branch |
| --- | --- | --- |
| Find Courses | Code suggestions, code/title/description search, faculty and session selection, official filters, reset, and result navigation | Exact-code search and suggestions work. Remaining filters and search modes are planned. |
| Offering details | See academic requirements, section times, delivery, linkage, availability, enrolment controls, locations, and syllabus links. Preview, add, and lock sections. | Basic section information and addition work. Preview, lock, and remaining metadata are planned. |
| My Timetable | Inspect and remove sections, view either or both terms, block personal time, and diagnose overlaps | Single-session view, interactive grid, and removal are merged. Both-term view, time blocks, and conflict diagnosis are planned. |
| Course Plan | Build a basket, clear it, and generate using term preferences, fixed sections, and blocked time | Planned |
| Save, share, and export | Reopen saved work, open a shared link on a second installation, print, and export PDF and PNG | Planned |

Reviewed baseline is GitHub `main` at `07ecb4a`, which includes the weekly timetable and removal from [PR #7](https://github.com/efeatalay9/207-course-project/pull/7).

This build combines the live catalogue with validated section addition. Students get visible feedback after adding a section, including when it is already selected, cancelled, missing, or the catalogue is unavailable. Sections with times to be announced can still be planned. [Add Section flow](docs/architecture.md#add-section)

## Entities

| Entity | Main fields | Behaviour |
| --- | --- | --- |
| `CourseOffering` | Offering ID, course code, title, campus, sessions, term, cancellation, sections | `id()`, `sessions()`, and `sections()` identify the offering and give its section choices. |
| `Section` | Section name, teaching method, cancellation and TBA status, meeting times, linked sections | `name()`, `meetings()`, and `linkedSections()` provide meeting and linkage data within the offering. |
| `MeetingTime` | Day, start/end, session, recurrence, location | `day()`, `start()`, `end()`, and `session()` describe the meeting's scheduled time. |
| `SelectedSection` | Offering ID and section name | `offeringId()` and `sectionName()` identify a section in a specific offering. |
| `Timetable` | Ordered set of selected sections | `addSection()`, `removeSection()`, `contains()`, and `selections()` manage chosen sections. Locks and personal blocks are planned. |
| `CoursePlan` and `AcademicProfile` | Basket and term preferences. Completed course codes and optional grades. | Planned. Stores generation choices and the local facts used in prerequisite checking. |

## API usage and persistence

The app uses the official U of T Timetable Builder adapter from PR #9. The app loads the current filter choices and fetches courses after the student chooses a term and division and searches. On November 24, the packaged Java code returned one Fall 2026 Arts & Science offering of CSC108H1 with ten sections. The prerequisite prototype uses a separate rules file and evaluates the student's profile locally. [Official Timetable Builder](https://ttb.utoronto.ca/) · [Draft API PR #9](https://github.com/efeatalay9/207-course-project/pull/9) · [Course API flow](docs/implementation-guide.md#catalogue-ingestion-and-search) · [Prerequisite reference](reference/prerequisites/README.md)

Save and reopen will use a versioned local plan file. The raw September 5, 2026 catalogue contains 8,172 offerings and 7,094 distinct codes. The walking reference covers all 2,281 within-campus building pairs in that archive. It combines 2,215 official campus map estimates with 66 OpenStreetMap pedestrian routes for 700 University Avenue. Every row has a source and collection date. [Walking file](reference/timetable-data/walking.json) · [Catalogue field mapping and walking provenance](reference/timetable-data/README.md)

## Run locally

Use **JDK 17 or newer**. The Maven wrapper downloads Maven and dependencies on the first uncached build.

```sh
./run.sh                 # Swing app using the live Timetable Builder
./run.sh --offline       # Swing app with the supplied catalogue ZIP
./run.sh --sample        # Swing app with small fictional fixtures
./run.sh --console       # Exact-code lookup in the terminal
./test.sh                # JUnit and dependency-direction tests
```

On macOS, open the checkout in Finder and double-click `Run Course Search.command` or `Run Tests.command`. In IntelliJ, open `pom.xml` as a Maven project and run [`app.Main`](src/main/java/app/Main.java) with the repository root as its working directory. The console accepts `:quit`.

## Development and review

Keep application rules behind use-case boundaries. The [search wiring](src/main/java/app/SearchApplication.java) and [Add Section flow](docs/architecture.md#add-section) show how controllers, interactors, presenters, and catalogue adapters connect.

The integration build passes 115 tests with 100% line coverage in the domain and use-case packages. Maven includes the course's Google Checkstyle rules. Use the scoped check for the Java files in a PR and run the full report to see existing findings. [Testing and style commands](docs/testing.md#style-and-coverage-work) · [Code-review guide](docs/code-review.md) · [Dated milestones](docs/project-plan.md#timeline-and-completion-evidence)
