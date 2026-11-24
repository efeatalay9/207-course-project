# Local testing guide

## Run without typing terminal commands

In Finder, open your project checkout and double-click:

- `Run Course Search.command` to build and open the desktop application with real data.
- `Run Tests.command` to run the automated suite and keep the results visible.

The launchers open Terminal automatically; you do not need to type commands.
The application appears in a separate window titled **Course search — CSC207**.
Choose a term and division, type `CSC`, and select a suggestion to inspect its
offerings. Close an old search window before relaunching after code changes.

For IntelliJ IDEA, open `pom.xml` as a Maven project, choose a JDK at version 17
or newer, and open `src/main/java/app/Main.java`. Run `Main.main` using the green
run icon beside the method. Set the run configuration's working directory to the
project folder so it can find the catalogue ZIP. You do not need to run individual
domain classes. To debug fictional sample data, add `--sample` to program arguments.

## Setup and launch

Install or select a JDK at version 17 or newer. Check both `java -version` and
`javac -version`. The project targets Java 17 language and API compatibility.
The Maven wrapper selects the project Maven version.

From the repository root:

```sh
./test.sh
./run.sh --sample
./run.sh
./run.sh --offline
```

`run.sh` builds the executable JAR and opens a desktop window. Run `test.sh`
separately after code changes. Live mode initially loads the term and division
choices. Offline mode loads the bundled ZIP and reports its offering count.

On macOS, double-click `Run Course Search.command` for the real-data desktop tester.
Close the search window to end the application. To skip rebuilding after a successful
build, run `java -jar target/course-search.jar` from the repository root.

For a terminal-only environment:

```sh
./run.sh --console --sample
./run.sh --console
```

Enter one code per prompt. Blank lines trigger input validation; `:quit` or end of
input exits. Both desktop and console use the same controller, interactor, presenter,
and catalogue implementations for exact offering lookup. Prefix autocomplete is
available in the desktop window; the console still requires a full code.

Custom JSON and ZIP sources are supported:

```sh
./run.sh --catalog "/absolute/path/to/courses.json"
./run.sh --console --catalog "/absolute/path/to/catalogue.zip"
java -jar target/course-search.jar --help
```

A ZIP must contain a root entry named `courses.json`. Do not combine `--sample`
and `--catalog`. Missing or malformed data prevents startup and reports an error;
it does not masquerade as a successful search with no matches. Exit status 1 means
startup/input-stream failure, and 2 means invalid launch options or no desktop display.

## Manual acceptance checks

Start in sample mode. The sample is intentionally fictional and must not be used
to plan actual course enrolment.

| Action | Expected result |
| --- | --- |
| Type `csc` | Dropdown contains `CSC108H1`, then `CSC207H1`, without duplicate entries for different terms |
| Narrow to `csc1`, then delete back to `csc` | One suggestion, then both suggestions again |
| Use Down/Up and Enter, or click a suggestion | Selected full code fills the input and its offerings load |
| Type `CSC108` | Dropdown offers `CSC108H1`; select it to see both offerings |
| Press Escape | Dropdown closes without changing the typed prefix |
| Clear the input | Suggestions disappear; no stale entries remain |
| Type `ZZZ` | Dropdown states that there are no matching course codes |
| Search `CSC108H1` | Two offerings, September 2026 first and January 2027 second |
| Search `  csc108h1  ` | Same two offerings as the uppercase code |
| Inspect the first offering | Campus, term designation, lecture/tutorial, Monday and Wednesday 09:00–10:00 meetings |
| Inspect `TUT0101` | Alternate-week pattern and linked `LEC0101` retained |
| Inspect the winter offering | `LEC0201` is TBA with unknown times; `LEC9901` is visibly cancelled |
| Search `CSC207H1` | Cancelled offering, practical with manual recurrence, tutorial with unknown status/times |
| Search `ZZZ999H1` | Clear no-matches message; old results disappear |
| Search blank/whitespace | Input-validation message; old results disappear |
| Press Enter with an open dropdown | Accept the highlighted suggestion and search for its offerings |
| Click Search | Search the full code currently in the field; choose a dropdown item to complete a partial code first |
| Resize/scroll/copy text | All offerings remain readable and selectable |
| Add fall `CSC108H1 LEC0101` | Both known fall meetings appear and My Timetable confirms the add |
| Add the fall lecture again | My Timetable reports that it is already selected and still shows one lecture |
| Try adding cancelled `CSC108H1 LEC9901` | My Timetable reports the cancellation and keeps the existing selection |
| Click Add to Timetable with the selection cleared | My Timetable asks the student to choose a section first |
| Add winter `CSC108H1 LEC0201` | The selection is visible in the list with the unavailable-time state and has no invented grid block |
| Select one block of the fall lecture and remove it | The selected lecture and both of its blocks disappear |

The current session picker lists sessions with timed selected meetings. Add a timed
section in a second session to test switching. Review the planned behaviour for
asynchronous and TBA-only sessions in the [project plan](project-plan.md#uc-04-review-the-timetable-and-remove-a-section).

Then launch with the real catalogue and repeat searches for `CSC108H1` and
`CSC207H1`. The supplied snapshot has **8,172 offerings**. Do not expect real
results to match the fictional sample's sections or times.
Type `CSC` and scroll through the dropdown: the real catalogue contains more than
eight matching codes. Eight rows are visible at once; the full matching set must
remain accessible. The catalogue has 7,094 distinct course codes overall.

## Automated verification

`./test.sh` runs JUnit tests for:

- Domain identity/interval validation and defensive collection copies.
- Normalization, blank input, no matches, all matching offerings, deterministic
  order, locale independence, and catalogue-access failure.
- JSON/ZIP loading, optional fields, exact numeric time conversion, missing files,
  malformed records, duplicate offering IDs, recurrence, and section links.
- Presentation of unknown/cancelled/manual schedules and replacement of stale results.
- The console flow through real startup wiring, including repeated searches and exit.
- Sorted prefix lookup on deliberately shuffled data, deduplication across terms,
  first/last/gap boundaries, no truncation, and comparison with a linear-scan reference.
- Autocomplete normalization, blank input, failure states, and replacement of matches
  as a prefix is narrowed or broadened.
- Swapping catalogue adapters and visualizer outputs through application interfaces.
- Weekly timetable projection, session fallback, unknown or missing sections, selection
  order, remove-section outcomes, and presenter state.
- Compiled dependency rules enforcing the agreed Clean Architecture layers.

Tests use small fixtures, not the large catalogue. Reports are written under
`target/surefire-reports/`. Desktop window layout and interaction are manual checks;
the automated tests cover the shared behaviour through the console and presenter.
On November 24, the reviewed baseline `07ecb4a` passed all 96 tests with JDK 17
and Maven 3.9.11, including the compiled dependency-direction test.

On the separate live API branch, all 101 Maven tests passed. The packaged Java
search returned one Fall 2026 CSC108H1 offering and ten sections in Arts &
Science, then correctly reported no matches for the same code in the UTSC scope.
The first result remained available to the timetable after switching scope.
[Draft API PR #9](https://github.com/efeatalay9/207-course-project/pull/9)

The combined local build passed all **115 tests**. Add Section has direct tests
for an add, duplicate, incomplete selection, missing offering or section,
cancellation, TBA, and a catalogue failure. A Swing-panel test verifies that an
add refreshes the grid and preserves its status message. Maven runs the tests
headlessly so they can run on a machine without a display.

A packaged sample Swing run also exercised the search and Add to Timetable
buttons. It verified successful addition, duplicate feedback, cancelled-section
feedback, and the message shown when no section is selected.

## Planned acceptance cases

Add fixtures for these behaviours as they are integrated. See the full feature
checklist in the [project plan](project-plan.md#official-planning-workflow-checklist).

| Scenario | Expected evidence |
| --- | --- |
| Filter by division/session and primary-section availability | Matches and labels follow the selected filters. A waitlist-only result appears only in the appropriate mode. |
| Match required components | A lecture with an incompatible tutorial does not certify fit. A linked tutorial that fits can be shown as the working combination. |
| Compare time intervals and recurrence | Same-day concurrent actual meetings conflict. End-to-start adjacency and separate academic terms remain compatible. TBA and unresolved date overlaps are labelled. |
| Generate with locks and personal blocks | A valid result preserves locks, includes required sections, and respects unavailable time. A limited search reports incomplete status. |
| Check prerequisites | Test any-of and all-of, the exact and below-grade boundary, credit quotas, missing facts, and a rule that needs review. Confirm that a changed official rule or qualifying note triggers review and that the student profile stays local. |
| Show walking transitions | A known back-to-back walk above ten minutes shows the warning. A short walk and an unknown route remain distinct. |
| Compare rank and diversity | Show two different weeks with a reason for their order. Confirm the active timetable changes only when the student applies one. |
| Save and open | Close and relaunch after saving, verify the selected sections and preferences, then try invalid or obsolete plan data. |
| Replace an unavailable section | Find a confirmed compatible same-course bundle, preserve other choices, and retain the original if no replacement is available. |
| Share, PDF, PNG, and print | Open the link in a second installation. Inspect PDF, PNG, and print preview for terms, labels, and concurrent blocks. |

Use a valid and a failure fixture for each interactor. Keep file and network
adapters independently testable. Demonstrate one working external API call from
the team build and keep screenshots or command output with the date, request
purpose, and safe response evidence.

In a unit test, instantiate the interactor with a small fake gateway and a presenter that records the last success and failure outputs. Execute the use case, assert the expected output was called and contains the right data, and inspect the changed state. A test with assertions only inside a presenter callback can pass without testing anything when the callback is never called. Separately exercise the real file or HTTP adapter. Use manual testing for Swing layout and the wired user interaction. The course's automated Swing system test is an optional example. [Course examples and Lab 9 guidance](quercus-project-guide.md#testing-and-persistence-examples-posted-november-23)

## Style and coverage work

The draft rubric requires use of Checkstyle or a similar tool for Excellent or
above in Code Quality and evidence of that use. See the [course expectations](project-expectations.md#style-and-checkstyle).
Maven now has an opt-in Checkstyle goal using version **10.26.1** and the course's
Google rules. The changed Java files use Google Java Format 1.17.0 and document
their public classes, constructors, and behaviour. Run a strict check on the
files being reviewed. For example, this checks the Add Section code and tests:

```sh
./mvnw checkstyle:check '-Dcheckstyle.includes=**/addsection/*.java'
```

To collect findings from the entire repo without making existing findings fail
the run:

```sh
./mvnw checkstyle:check -Dcheckstyle.failOnViolation=false
```

The combined build passed a strict Checkstyle run over all **31 changed Java
files**. The whole-repo report still has findings in other files. Maven writes
the XML report to `target/checkstyle-result.xml`.

A November 24 baseline audit of commit `07ecb4a` ran the course's Google checks
in report mode with Checkstyle **10.26.1** and Maven Checkstyle Plugin **3.6.0**
from a separate configuration. It found **1,817 warnings across 57 production
files**. The largest groups were indentation (1,559), line length (76), import
ordering (60), missing method Javadoc (50), and missing type Javadoc (37). This
count is a style baseline the team can address in reviewed changes.

On that baseline, JaCoCo 0.8.12 measured **67/67 domain lines** and **166/172 use-case lines (96.51%)**. The combined build now covers **68/68 domain lines and 232/232 use-case lines (100% each)**. The Add Section tests and the tests for invalid search and autocomplete results cover the previously missed paths. Repeat the report as the team adds interactors. [Baseline coverage gaps](repository-audit.md#coverage-gaps-on-baseline)

Reproduce the line report without editing the Maven configuration:

```sh
./mvnw -B -ntp org.jacoco:jacoco-maven-plugin:0.8.12:prepare-agent test org.jacoco:jacoco-maven-plugin:0.8.12:report
```

Open `target/site/jacoco/index.html` and look at `domain` and each `usecase.*` package. `target/site/jacoco/jacoco.xml` has line counts suitable for both package-group checks. Check lines and meaningful result paths. Coverage cannot exercise code a test avoids invoking or fully evaluate the correctness of a source rule.

### Proposed team workflow

1. Configure CheckStyle-IDEA with the same Google checks and engine as the
   [course notes](https://github.com/CSC207-UofT/207-course-notes/blob/HEAD/code-style-and-documentation.md).
   Run it on the files you own and record representative results and fixes.
2. Run the Maven Checkstyle goal on the files changed by your PR. Write Javadoc
   for new public types and methods. Keep larger style changes in reviewable
   slices so the feature diff stays easy to follow.
3. Use the JaCoCo report for `domain` and `usecase` and keep coverage repeatable.
   Read the resulting report after every feature merge. The course target
   is 100% line coverage for those layers by December 4.
4. Follow the [code-review checklist](code-review.md#reviewer-checklist) for correctness, tests, dependency direction, Javadoc and PR clarity.
   Keep a representative reviewed PR, a style report, and the measured
   coverage report for presentation evidence.

## IDE and build notes

Open `pom.xml` as a Maven project in your IDE and select a JDK of at least 17.
Run `app.Main` with the working directory set to the repository root; add
`--sample` as a program argument for the fictional catalogue. Keep generated IDE
files out of commits.

The project uses the standard [Apache Maven wrapper](https://maven.apache.org/tools/wrapper/),
[Gson](https://github.com/google/gson), and [JUnit 5](https://docs.junit.org/5.13.4/user-guide/).
Versions are pinned in `pom.xml` and `.mvn/wrapper/maven-wrapper.properties`.
The launch scripts keep downloaded Maven and dependencies under ignored `.mvn/`
directories. Windows users can run `mvnw.cmd test`, `mvnw.cmd package`, and
`java -jar target/course-search.jar`; the `.sh` launchers are for macOS/Linux.

If an initial dependency download fails, check network access and rerun the command.
Subsequent cached builds can use `./test.sh -o`. A Maven `sun.misc.Unsafe` warning
can occur on JDK 25; it is emitted by Maven's dependency, not the application.
If no desktop is available, use `--console`.
