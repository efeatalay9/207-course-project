# U of T Timetable Builder

## Team Information

### Team Members
- Matthew Chen
- Efe Atalay
- Jingyan Lin
- Vincent He
- Leon Ren

## Project Description

**Domain:** Education and student productivity.

### Description
This CSC207 Java Swing project helps U of T students browse courses, plan a weekly timetable, and prepare alternatives 
for enrolment. The target includes the official builder's planning workflows plus checks for course fit, prerequisites 
and walking time, comparison of generated schedules, and fast replacement of an unavailable section.

## User Stories

### 1. Find course sections (Matthew) — Implemented
As a student, I want to search by course code and see the matching offerings, their sections, and their meeting times, 
so I can choose sections that fit my schedule.

### 2. Add a section and view its meeting times (Jingyan) — Implemented
As a student, I want to add a course section and see its scheduled meetings so I can keep track of the sections I’ve 
chosen.

### 3. View and edit my weekly timetable (Vincent) - In Progress

As a student, I want to see my selected sections on a weekly grid andremove sections I no longer want so I can adjust my 
schedule.

### 4. Check timetable conflicts (someone) - Planned

As a student, I want to see which of my selected meetings overlap and
when they overlap so I can decide which sections to change.

## Use Cases

### Find course sections (User story #1)

- **Primary actor:** Student.
- **Precondition:** The course catalogue has loaded.
- **Trigger:** The student selects a suggested course code or submits a full code.
- **Success outcome:** The student sees the matching offerings and can inspect
  their sections and meeting times.

**Main success scenario**

1. The student enters a course code or starts typing its prefix.
2. The app displays matching course-code suggestions as the student types.
3. The student selects a suggestion or submits a full course code.
4. The app displays the matching offerings by term and campus, including their
   sections, known meeting times, and any cancellation or TBA information.

**Extensions**

- **Step 3 — Blank input:** The app asks the student to enter a course code.
- **Step 4 — No matching offerings:** The app displays a no-results message.
- **Step 4 — Catalogue access fails:** The app displays a search error.

### Add a section to the weekly timetable (User story #2)

- **Primary actor:** Student.
- **Precondition:** The student is viewing the sections for a course offering.
- **Trigger:** The student selects a section and clicks **Add to Timetable**.
- **Success outcome:** The section is selected and all its known meeting times
  appear in the weekly timetable.

**Main success scenario**

1. The student chooses a specific section from the search results.
2. The student adds the section to their timetable.
3. The app displays each of the section's scheduled meetings on the "My Timetable panel."

**Extensions**

- If the section is already selected, the app keeps one copy.
- If a meeting time is TBA, the app shows the section as selected with a
  **Time TBA** label and leaves it off the timed grid.

### Review my weekly timetable (User story #3)

- **Primary actor:** Student.
- **Trigger:** The student opens the weekly timetable.
- **Success outcome:** Each scheduled meeting for the displayed academic session appears on the correct day and at the correct time.

**Main success scenario**

1. The student opens the weekly timetable.
2. The app displays the meetings for the selected sections on the weekly grid.

**Extensions**

- If there are no selected sections, the app shows an empty grid.
- If a selected section has no knokwn meeting times, the app lists it separately as **Time TBA**

### Remove a section from my timetable (User story #3)

- **Primary actor:** Student.
- **Precondition:** The timetable contains at least one selected section.
- **Trigger:** The student chooses **Remove** for a selected section.
- **Success outcome:** The section and all of its meeting blocks disappear from the timetable.

**Main success scenario**

1. The student selects a section they previously added.
2. The student chooses **Remove**.
3. The app removes the section and refreshes the weekly grid.

## EXTRA ADDITION TO UOFT TTB: Team, user stories, and MVP use cases

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

## Design Diagrams

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
