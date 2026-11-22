# Local testing guide

## Run without typing terminal commands

In Finder, open `Documents/ChatGPT/CSC207 final project` and double-click:

- `Run Course Search.command` to build and open the desktop application with real data.
- `Run Tests.command` to run the automated suite and keep the results visible.

The launchers open Terminal automatically; you do not need to type commands.
The application appears in a separate window titled **Course search — CSC207**.
Type `CSC`, choose a suggestion, and inspect its offerings. Close an old search
window before relaunching after code changes, since running windows retain old code.

You can also ask Codex in this chat to run the app or tests. Codex launches the
local program; Swing renders its own desktop window. The Codex terminal can be
opened with Control + backtick on macOS, as documented in the
[official keyboard shortcuts](https://learn.chatgpt.com/docs/reference/commands#keyboard-shortcuts).

For IntelliJ IDEA, open `pom.xml` as a Maven project, choose a JDK at version 17
or newer, and open `src/main/java/app/Main.java`. Run `Main.main` using the green
run icon beside the method. Set the run configuration's working directory to the
project folder so it can find the catalogue ZIP. You do not need to run individual
domain classes. To debug fictional sample data, add `--sample` to program arguments.

## Setup and launch

Install/use a JDK at version 17 or newer. Check both `java -version` and
`javac -version`. The project targets Java 17 language/API compatibility; the initial
local verification used the installed JDK 25. No system Maven installation is needed.

From the repository root:

```sh
./test.sh
./run.sh --sample
./run.sh
```

`run.sh` builds the executable JAR and opens a desktop window. It skips automated
tests to keep repeated manual launches short; run `test.sh` separately after code
changes. The full catalogue loads once before the window appears, directly from
the repository ZIP. The terminal reports the loaded offering count.

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
- Compiled dependency rules enforcing the agreed Clean Architecture layers.

Tests use small fixtures, not the large catalogue. Reports are written under
`target/surefire-reports/`. Desktop window layout and interaction are manual checks;
the automated tests cover the shared behaviour through the console and presenter.

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
