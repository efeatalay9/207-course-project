# U of T Timetable Builder

## Team Information

### Team Members
- Matthew Chen
- Efe Atalay
- Jingyan Lin
- Vincent He
- Leon Ren

## Project Description

### Domain
Education and student productivity.

### Description
Our project is a Java Swing timetable builder for U of T students. The planned app will let students search for courses, add sections to a weekly timetable, see which meetings overlap, and save and reopen their plans.

Work on a branch and open a pull request for review instead of pushing directly
to `main`.

## User Stories

### 1. Find course sections (Matthew) — Implemented
As a student, I want to search by course code and see the matching offerings,
their sections, and their meeting times, so I can choose sections that fit my
schedule.

The desktop search suggests every matching course code in alphabetical order as
you type a prefix, ignoring capitalization and surrounding whitespace. Choose a
code to retrieve all its offerings, labelled by term and campus, with sections,
meeting times, cancellation status, and unknown-time information. The console
continues to accept exact full course codes.

Autocomplete uses a distinct, sorted in-memory code index and binary search.
**The index must remain sorted using the same ordering as binary search.** It is
built once from the catalogue and kept immutable; see the architecture plan for
the invariant and complexity. The source JSON does not need to be sorted.

See [the architecture plan](docs/architecture.md) for the agreed responsibilities,
interfaces, implementation order, and coding conventions.

### 2. Add a section and view its meeting times (Jingyan) — Implemented
As a student, I want to add a course section and see its scheduled meetings so I can keep track of the sections I’ve chosen.

### 3. something () — In Progress

### 4. something () — In Progress

### 5. something () — In Progress

## Use Cases

### Find course sections

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

### Add a section to the weekly timetable

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

## Minimum Viable Product (MVP)
| Team member | MVP use case | Status |
|---|---|---|
| Matthew | Find course sections | Implemented |
| Jingyan | Add and remove sections from the weekly timetable | In progress |
(add more later)

## Entities

| Entity | Main fields | Relevant behavior |
|---|---|---|
| `CourseOffering` | Offering ID, course code, title, campus, term, sections | Provides the sections for a particular offering. |
| `Section` | Section identifier, teaching type, status, meeting times, linked sections | Provides the meeting times and links for a section. |
| `MeetingTime` | Day, start time, end time, academic session, recurrence, location | Represents when and where a meeting takes place. |
| `Timetable` (proposed) | Academic term and selected sections | Records section selections for a weekly plan. |

## API Usage
(add notes here)

## Design Diagrams
(add notes here)

## Catalogue data

`uoft-ttb-raw-catalogue-2026-09-05.zip` contains `courses.json`, a catalogue snapshot
with 8,172 offering records and 7,094 distinct course codes (about 130 MB
uncompressed). Course codes can appear in multiple offerings. The data is a
snapshot, not live enrolment information.

## Project status

The first search flow is implemented with a Swing desktop tester, an interactive
console, a sample catalogue, and automated tests. Timetable editing and conflict
detection are not implemented yet.

## Run locally

Use **JDK 17 or newer**. Maven is downloaded automatically by the included wrapper;
the first build needs internet access. Run these commands from the repository:

```sh
./run.sh                 # Desktop search using the supplied real catalogue ZIP
./run.sh --sample        # Desktop search using small fictional sample data
./run.sh --console       # Console search using the real catalogue
./test.sh                # Automated tests using small fixtures
```

On macOS, you can also double-click `Run Course Search.command` to open the app or
`Run Tests.command` to run the tests; no terminal typing is required. In IntelliJ,
open `pom.xml` as a Maven project and run the `main` method in
[`src/main/java/app/Main.java`](src/main/java/app/Main.java) with a JDK of at least
17. Set the run configuration's working directory to the repository root so it
can find the supplied catalogue. The console accepts `:quit` to exit. No ZIP
extraction is needed. Build output and dependency caches are ignored by Git.

Try typing `CSC`, narrowing it to `CSC1`, and choosing a code from the dropdown.
Use Up/Down and Enter, or click a suggestion; Escape dismisses the dropdown.
Every match is available by scrolling, with no fixed result-count limit.
Also try `CSC108H1`, `  csc108h1  `, an unknown code, and a blank search.
In sample mode, `CSC108H1` deliberately includes two offerings, TBA information,
an alternate-week tutorial, and a cancelled section. All sample data is fictional.

See [the testing guide](docs/testing.md) for expected results, custom catalogue
paths, IDE setup, and troubleshooting.

## Code structure and contributions

Production code is in `src/main/java`; automated tests are in `src/test/java`.

| Package | Responsibility |
| --- | --- |
| `domain` | Immutable offerings, sections, and meeting times |
| `usecase` | Search and autocomplete rules, with narrow input/output and catalogue interfaces |
| `dataaccess.catalog` | JSON/ZIP loading and the indexed in-memory catalogue |
| `interfaceadapter` | Controllers, presenters, and view models |
| `view` | Swing and console user interfaces |
| `app` | Startup and wiring of concrete modules |

[`SearchApplication.connect`](src/main/java/app/SearchApplication.java) accepts
catalogue and output interfaces. A new data source or visualizer can implement
those interfaces without changing the search rules. Keep dependencies directed
toward the domain and use cases, use explicit variable types instead of `var`,
and add plain-language comments that explain class and method responsibilities.
See [the architecture plan](docs/architecture.md) before adding a feature.

Run `./test.sh` before requesting review. Tests cover catalogue validation,
search and autocomplete behavior, presenters, interchangeable implementations,
and compiled dependency boundaries. For a clean build including the runnable
JAR, use `./mvnw -B -ntp clean verify`.
