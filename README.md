# 207-course-project
A better timetable builder!

Work on a branch and open a pull request for review instead of pushing directly
to `main`.

## First user story

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
