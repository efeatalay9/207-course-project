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

U of T Timetable Builder is a student project for planning courses before enrolment.
Students can look up lectures, tutorials, and practicals, put their choices in a
weekly timetable, check for overlapping meetings, and return to a saved plan later.
Students enrol separately in ACORN.

Work on a branch and open a pull request for review instead of pushing directly
to `main`.

## User Stories

### 1. Find course sections (Matthew)

As a student, I want to find a course by code and compare its sections by term,
campus, and meeting time so I can decide which section to add.

### 2. Add a section to my timetable (Jingyan)

As a student who has chosen a section, I want to add it to my timetable so I can
keep track of the lectures, tutorials, and practicals I plan to attend.

### 3. View and edit my weekly timetable (Vincent) - In Progress

As a student, I want to see my selected sections on a weekly grid and
remove sections I no longer want so I can adjust my schedule.

### 4. Check timetable conflicts (lead to confirm)

As a student, I want to see which of my selected meetings overlap and
when they overlap so I can decide which sections to change.

### 5. Save and reopen a timetable (lead to confirm)

As a student planning across several days, I want to save my timetable and reopen
it after restarting the app so I can pick up where I left off.

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
- **Success outcome:** The planned section is selected and its meeting details
  appear in My Timetable.

**Main success scenario**

1. The student chooses a specific section from the search results.
2. The student adds the section to their timetable.
3. The app shows the added section and its meeting details in My Timetable.

**Extensions**

- **Step 2 — Already selected:** The app keeps one copy.
- **Step 3 — Time TBA:** The section remains selected with its time marked TBA.

### Review my weekly timetable

- **Primary actor:** Student.
- **Precondition:** The course catalogue has loaded.
- **Trigger:** The student opens the weekly timetable.
- **Success outcome:** Each scheduled meeting for the displayed academic session appears on the correct day and at the correct time.

**Main success scenario**

1. The student opens the weekly timetable.
2. The app displays the meetings for the selected sections on the weekly grid.

**Extensions**

- **Step 2 — Empty timetable:** The app shows an empty grid.
- **Step 2 — Time TBA:** The app lists the section separately from the timed grid.

### Remove a section from my timetable

- **Primary actor:** Student.
- **Precondition:** The timetable contains at least one selected section.
- **Trigger:** The student chooses **Remove** for a selected section.
- **Success outcome:** The section and all of its meeting blocks disappear from the timetable.

**Main success scenario**

1. The student selects a section they previously added.
2. The student chooses **Remove**.
3. The app removes the section and refreshes the weekly grid.

**Extension:** If the section is no longer selected at step 3, the app reports
that it could not remove it and keeps the timetable as it was.

### Check timetable conflicts

- **Primary actor:** Student.
- **Precondition:** The timetable contains selected sections.
- **Trigger:** The student asks to check an academic session for conflicts.
- **Success outcome:** The student sees which confirmed meetings overlap and when.

**Main success scenario**

1. The student chooses a session and requests a conflict check.
2. The app compares meetings that occur on the same day in that session.
3. For each confirmed overlap, the app shows both course and section labels, the
   day, and the time they share.

**Extensions**

- **Step 2 — Back-to-back meetings:** A meeting ending as another begins has no overlap.
- **Step 2 — Unconfirmed time or recurrence:** The app flags the affected meetings
  for review if it cannot determine whether they occur together.
- **Step 3 — No confirmed overlaps:** The app reports that it found none and still
  shows any meetings that need review.

### Save my timetable

- **Primary actor:** Student.
- **Precondition:** The student has a timetable open.
- **Trigger:** The student asks to save the current timetable.
- **Success outcome:** The chosen sections can be restored after restarting the app.

**Main success scenario**

1. The student chooses where to save the timetable.
2. The app saves the selected sections and confirms the save.

**Extension:** If saving fails at step 2, the app explains the problem and keeps
the current timetable open for another attempt.

### Reopen a saved timetable

- **Primary actor:** Student.
- **Precondition:** The student has previously saved a timetable.
- **Trigger:** The student asks to open a saved timetable.
- **Success outcome:** The saved section selections are restored and visible.

**Main success scenario**

1. The student picks a saved timetable.
2. The app restores the selected offerings and sections and shows their meeting details.

**Extensions**

- **Step 2 — File cannot be read:** The app explains the problem and leaves the
  current timetable intact.
- **Step 2 — Section has disappeared from the catalogue:** The app identifies the
  missing selection by its saved label and restores the sections it can match.

## Minimum Viable Product (MVP)

The core demo follows one student from course search through selecting and
editing sections, checking a conflict, and saving and reopening their plan after
restarting. The catalogue refresh provides current source data when available.
The bundled snapshot also lets the team demonstrate the full planning workflow
offline. Automatic timetable generation and calendar export are stretch goals.

| Team member | MVP use case | Status |
|---|---|---|
| Matthew Chen | Find course sections | Implemented using the bundled catalogue |
| Jingyan Lin | Add a section to my timetable | Implemented |
| Vincent He | Review the weekly timetable and remove sections | In review ([PR #7](https://github.com/efeatalay9/207-course-project/pull/7)) |
| Efe Atalay (proposed) | Check timetable conflicts | Planned |
| Leon Ren (proposed) | Save and reopen a timetable | Planned |

The conflict and persistence leads are proposed assignments for the team to confirm.

## Entities

| Entity | Instance variables | Relevant methods |
|---|---|---|
| `CourseOffering` | ID, code, title, campus, sessions, term designation, cancellation status, sections | `id()`, `sessions()`, `sections()` |
| `Section` | Name, teaching method, cancellation and TBA flags, meetings, linked sections | `name()`, `meetings()`, `linkedSections()` |
| `MeetingTime` | Day, start, end, session, location, recurrence and repetition pattern | `day()`, `start()`, `end()`, `session()` |
| `Timetable` | Section selections keyed by offering ID and section name | `addSection(...)` prevents duplicate selections. Viewing and removing selections are in review. |

## API Usage

**Source:** [University of Toronto Timetable Builder](https://ttb.utoronto.ca/).
U of T's [EASI documentation](https://easi.its.utoronto.ca/student-information-systems/timetable-builder/)
describes the official data source and its coverage. The official client uses
[`GET /ttb/reference-data`](https://api.easi.utoronto.ca/ttb/reference-data) for
sessions and divisions and `POST /ttb/getPageableCourses` for course offerings,
sections, and scheduled meetings.

- **How the app uses it:** Course search and meeting displays read the bundled
  snapshot collected from the course endpoint. A planned Java refresh will
  retrieve updated catalogue data and keep the bundled snapshot available offline.
- **Call status:** The course endpoint supplied the bundled September 5, 2026
  catalogue in an earlier collection. A separate HTTP probe on November 23, 2026
  confirmed a `200` JSON response from the reference endpoint. Live requests from
  the Swing app are still planned.

## Design Diagrams

The [architecture plan](docs/architecture.md#approach-and-dependencies) includes
the course-search class diagram and the catalogue, use-case, and display boundaries.

## Catalogue data

`uoft-ttb-raw-catalogue-2026-09-05.zip` contains `courses.json`, a catalogue snapshot
with 8,172 offering records and 7,094 distinct course codes (about 130 MB
uncompressed). Course codes can appear in multiple offerings. The data is a
snapshot, not live enrolment information.

## Project status

Course search, autocomplete, and adding sections are on `main`. The weekly grid
and section removal are in review. Conflict checking, timetable persistence, and
live API refresh are planned.

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
