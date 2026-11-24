# Timetable implementation guide

This guide describes how to implement the CSC207 timetable use cases in Java Swing. [Feature and use-case plan](project-plan.md) · [Course requirements](project-expectations.md) · [Current repository audit](repository-audit.md)

## Team decisions and scope

- **The team is new to Java.** Use the existing Java 17 Maven build and Swing. Prefer explicit variable types, small classes and methods, straightforward loops and conditionals, and ordinary lists, sets, and maps. Reuse the repo's records where helpful. Use interfaces at real boundaries where a fake or alternate implementation is useful. Keep each use case small enough for its lead to explain and debug.
- **The product target includes the official planner's functionality.** It includes full search and filters, section information and choices, a weekly grid with both terms, personal blocks and locks, generation, links, print, PDF, and PNG. Implement those workflows with the team's own Swing layout. The course blueprint still needs a clear, demonstrable MVP and appropriately scoped stories. Show the TA which slices can be demonstrated while keeping the complete feature checklist visible.
- **Five additional lead cases** cover fit with an existing timetable, checking prerequisites, walking time, choosing among generated alternatives, and same-course replacement during enrolment. Matthew, Jingyan, Vincent, Efe, and Leon are the proposed leads respectively. Their ownership table is a proposal for discussion, with current code status marked independently. Leave the substantive implementation and review opportunities to the members who own them.
- **Catalogue and personal data.** Read the official catalogue within the selected scope. Keep the active plan, computation and academic profile local in Java. Use a small versioned local plan format and keep the archived course and prerequisite fixtures for repeatable tests.
- **Text and controls should use student language.** Show course data updated, availability, waitlist, and names students see in the timetable. Use 12-hour display times, clear labels, guidance for filters, and a visible student-project notice. Preserve raw IDs and wall-clock values internally.
- **Share only the timetable.** Implement the share link and export actions in the project plan. Keep academic history, grades, and private notes in local storage.

## Run and module boundaries

`Main` sets up the active timetable, catalogue adapter, controllers, interactors, presenters, and Swing views. For live desktop search, it loads only the term and division choices before showing the window. Follow `SearchApplication` and `ViewTimetableInteractor` when wiring a use case. Keep Swing, Gson, file formats, and HTTP in outer packages.

| Work | Core types or operations | Adapter or view work |
| --- | --- | --- |
| Search | `SearchCoursesRequest`, `CourseCatalog`, exact code and selected catalogue scope | Use the official TTB endpoints and map offering fields through `JsonOfferingMapper`. Display the available term and division choices and keep live requests off Swing's event thread. |
| Details and choices | `CourseOffering`, `Section`, `SelectedSection`, `OfferingLookup`, add/lock validation | Show source wording, teaching types, dates, locations, enrolment controls, and notes. Render presenter outcomes. |
| Timetable | `Timetable` state, term view, `BlockedTime`, a pure overlap check | Grid placement, labels, side-by-side overlap lanes, blocking dialog, and section actions. |
| Generation and repairs | Build allowed section bundles, then schedule and rank them with ordinary collections and bounded backtracking | Course basket, explicit start action, progress/cancellation state, outcome, comparison, and apply. |
| Prerequisites | `AcademicProfile`, `PrerequisiteRuleLookup`, `CheckPrerequisitesInteractor`, pure rule evaluation | Decode/download a public rule file, collect local facts, show reasons and source wording. |
| Walking | Arrival allowance, route lookup interface, journey result with known/unknown state | Parse the included matrix, keep a route map for lookup, show the estimates and warnings. |
| Save/share/export | Plan snapshot, `PlanDataAccess`, save/open/apply operations | JSON file adapter, validated link parser, PDF/PNG/print rendering and file chooser. |

Only introduce a shared scheduler once at least two callers use the same rule. Start with simple helpers on ordinary collections. Keep a small generation test fixture and run it through Generate, Find Courses That Fit, and Find Replacement. For work that may block or take noticeable time, use a `SwingWorker`, capture an immutable plan snapshot, and update views on the event dispatch thread.

## Implementation handoff for the five leads

These names are starting points. Follow the existing package structure and use one case-specific controller and presenter for each added user-facing action.

| Lead and story | Inputs and interactor work | Display and first integration check |
| --- | --- | --- |
| Matthew · Find Courses That Fit | Take the normal `SearchCoursesRequest` filters and an active timetable snapshot. Reuse exact offering lookup and a bundle checker. Produce each offering ID with a best fitting bundle or a reason the fit is undecided. | Show the lecture and linked tutorial as one suggestion. Test a catalogue filter plus a real personal block and show that pressing Enter does the same thing as Search. |
| Jingyan · Check Prerequisites | Take offering ID and load the profile from the local port. Read a `PrerequisiteRuleLookup` result containing the public rule, text, and date. Evaluate exactly once per user request, with a typed result containing the decisive reason. | Put a clear status and official wording in the existing offering details. Test exactly-at-threshold, deficient, absent grade, and an unsupported rule through the presenter. |
| Vincent · Check Walking Time | Take the displayed term and selected section keys. Resolve timed meetings and call a lookup for the consecutive known physical building pair. Keep every transition's source, distance, time or reason it is unknown. | Render a transition label in the weekly detail pane. Test a short walk, a walk over ten minutes, no route, and a true overlap. |
| Efe · Compare Generated Timetables | Take the basket, term preferences, locks, personal blocks and generation options. Generate allowed bundles, rank and present the retained results plus search state. Apply a preview through the existing timetable state after revalidation. | Show a ranked option and a visibly distinct next choice, with actual gaps and changes. Test equal schedule patterns, a score tie, a blocked time and cancellation. |
| Leon · Replace a Section That Filled Up | Take the active plan ID and the selected section ID, with an explicit user unavailable override. Exclude that choice and search other bundles in the same offering, while preserving other courses. Keep previews separate from apply and save. | Show old and new sections and availability state. Test a replacement that preserves its linked lecture and a case with no compatible replacement. Use save/reopen to demonstrate preserving the original. |

Shared contracts are offering and section identity, a meeting interval, and immutable snapshots for reading plan state. Agree on those early. Use test fixtures for each interactor and keep UI strings out of the scheduler. The reuse boundary is one plain bundle and interval checker between fit, generation and repair.

## Catalogue ingestion and search

Use `GET /ttb/reference-data` with the JSON accept header to read the terms and divisions the official Timetable Builder currently advertises. After the student selects one term and division, use `GET /ttb/getOptimizedMatchingCourseTitles` for prefix suggestions and `POST /ttb/getPageableCourses` for exact-code search. Follow the response page metadata and store successfully read offerings by source ID so the weekly grid can still resolve a section after the scope changes. Reuse `JsonOfferingMapper` for the API and offline ZIP. TTB returns a specific no-results code even when the HTTP response is 404. Other failures stay distinct from an empty search. [Draft PR #9](https://github.com/efeatalay9/207-course-project/pull/9)

The bundled September 5, 2026 ZIP contains 8,172 offerings and 23,733 sections. It is available for offline use, tests, and prerequisite rule compilation. It contains Fall 2026, Winter 2027, and full-year offerings. Use the live session choices when a Summer term is published. [Captured data and field mapping](../reference/timetable-data/README.md)

1. Use the source offering `id` and exact `Section.name()` for planned selections. Keep the human course code, term designation, campus, and topic as display and recovery context. A course code alone can identify several offerings. Keep unresolved saved selections visible after an update.
2. Read course code, title, amount of credit, department, faculty, session, campus, description, breadth, and section attributes from their own raw fields. Keep course text and the source's exact identifiers in the domain mapping. Use the code as an opaque string. Add missing display metadata to the mapper rather than duplicating a second course catalogue.
3. Preserve the exact-code map and distinct prefix index for offline search. Extend the scoped live request for the other official catalogue filters as needed. Use friendly labels and source keys. Use observed catalogue credits for the credit drop-down and show genuine weekend meetings on the grid.
4. Apply availability to the source-designated primary component for the official search options. Primary sometimes means practical or tutorial. With section-level criteria such as instructor, day, and delivery, check that the criteria refer to compatible sections before displaying them as a single match. Course-level availability and match status cannot prove a complete timetable fits.
5. Show open/closed space, waitlist, source date, and enrolment-control notes clearly. A numerical space count is an observation. Stale or absent observations stay unknown under a strict availability-only filter. An opening in a section can be subject to a faculty, program, or priority window.
6. Keep search controls stable when searching, adding a course, or refreshing display state. Enter submits the current search. Separate actions can save a search or pick a result. Distinguish no matches from bad input or a load error.

Use two stable keys, one for the offering and one for the selection. Prefer the verified source ID plus the section's real source identifier. If source IDs are absent, keep a deterministic key built from academic sessions, division and campus, course identity, the exact offering discriminator, component type and section code. Page numbers, array positions and display order are ephemeral. If an exact saved key stops resolving, display its old course, term and section and let the student repair it. An automatic substitution would change a choice or topic without their approval.

Show the availability state with its known observation time. A stale or absent value needs verification for an availability-only search. Marking a section filled makes it unavailable immediately for that repair. Direct use of the published TTB data does not establish the moment at which ACORN updated availability.

## Section choices, conflicts, and candidate generation

### Build a complete course choice

Treat a course choice as a small list of sections that must travel together. For each offering, group the candidate sections by teaching method. Choose one from each required type when the published rule says so, and support optional or conditional components where the source makes that explicit. A practical can be primary. A lecture may have several meetings. Meeting rows do not represent additional independently selectable sections.

For each candidate combination, check all trustworthy links among the choices, including practical-to-tutorial associations. Resolve linked targets by teaching type and exact section number in the same offering. Preserve an explicitly chosen substantive topic, even if changing it would improve the time score. A contradiction between source notes and structure, or a required type with missing rows, yields a course with a condition to review. Example fixtures from the September capture are **BIO203H5** for contradictory linkage and **FSTA03H3** for a missing required tutorial. [Concrete examples and evidence](../reference/timetable-data/README.md)

### Check times and generate

`MeetingTime` currently stores an academic session, ISO weekday, local start and end, and recurrence. Keep `start.millisofday` and `end.millisofday` as Toronto wall-clock offsets. When actual dates are established, compare occurrences with matching dates. For known weekly intervals in the same session and weekday, overlap when both `aStart < bEnd` and `bStart < aEnd`. Ends that equal another start are allowed. Dates/term boundaries and recurrence matter. An unresolved alternate week can yield a possible overlap even if representative weekly blocks look like a collision. A known time with a TBA location can still affect the time check.

Use separate statuses for scheduled, confirmed asynchronous, TBA time, TBA location, arranged/manual, cancelled, and source-unknown cases where applicable. Blocked time is term, weekday, and a half-open interval. Preserve a section manually added as preferred. Lock it only on an explicit action. Preserve locked sections, selected topics, blocks, and hard constraints when generating.

```text
create the list of allowed section bundles for each offering
start with locked choices and personal blocks
choose the next offering and loop over its bundles
    skip a bundle that violates a known hard constraint
    add a viable bundle and try the next offering
    remove the bundle when returning to try another option
save valid complete plans and their explanatory metrics
report whether the search finished and which source conditions were uncertain
```

Order the offerings by fewest bundles if search becomes slow. Limit the number of returned schedules separately from search completion. A known valid result can be useful even if more results may exist. An exhaustive search with reliable data can report no valid plan. A timed-out or stopped search reports that it stopped, and unresolved source data stays a separate outcome. Preserve a student's active plan until they apply an offered result.

Use a small result enum and a separate `searchFinished` flag. Hold candidate plans and diagnostics in the response. This gives each caller enough information to render the following outcomes.

| Code | Meaning to present |
| --- | --- |
| `FOUND` | At least one confirmed solution preserves the current hard rules. If the search stopped early, label this as a solution found so far. |
| `PROVED_INFEASIBLE` | An exhaustive search with sufficient data found no solution under the supplied constraints. |
| `SEARCH_INCOMPLETE` | Time, cancellation or a search cap prevented establishing a conclusion. |
| `DATA_UNKNOWN` | Missing or contradictory source facts prevent establishing fit. Give the affected course, component or observation. |
| `ERROR` | An unexpected processing or data access error failed the operation. Preserve the plan and offer retry when sensible. |

Use hard validity first, preserve the student's Preferred sections where possible, then score convenience. Compare fields in this order with a simple comparator. Show which course pairs overlap when the search cannot find a conflict-free result. For comparison, walk the ranked list and retain schedules with meaningfully different meeting patterns. Keep the top result and show why it leads.

### Use the same schedule check in the three student flows

- **Fits My Timetable** checks whether at least one complete bundle fits around the current selections and blocked time. Keep the current plan fixed. Explain the empty-plan case and show the valid section combination.
- **Generate and Compare** obeys the requested offerings, blocks, locks, and required components before comparing conveniences. Score Early/Balanced/Late per term and explain short gaps or changes in the meeting pattern. The plan proposes a 20–75 minute range for short gaps. Different section names at identical times belong to the same comparison option.
- **Find Replacement** lets the student say a selected lecture, tutorial, or practical filled before the source has refreshed. Replace only what is needed in that offering, hold other courses fixed, and preserve the rest of the locks and personal blocks. Validate the full linked combination and rank by fewest edits. A stale or unknown opening can be labelled **Verify availability**. Opening the candidate only previews it. Apply an accepted replacement and keep the original available as a saved plan.

## Prerequisites and academic history

The [prerequisite reference](../reference/prerequisites/README.md) contains the archive compiler, Java 17 evaluator, generated rule data, coverage and rebuild instructions.

### Local profile and offering identity

- Offer catalogue autocomplete to record completed course codes. Collect numerical grades, total awarded credits, cumulative GPA and confirmed equivalents as applicable to supported rules. Distinguish complete history from a partly filled profile and use facts as of the relevant deadline. For subject or level quotas, get the eligible subject, level, and credits from verified records. A course code or a published credit range alone cannot establish what the student earned. Keep this profile in a local file that can be saved and imported. Plan sharing exports only the intended timetable information.
- Check the rule for the exact offering ID and course code. Compare the normalized current prerequisite field and relevant official notes with the archived evidence. For a missing or changed rule, show the current source text with **Needs review**. Allow the student to browse timetable choices independently of whether the academic rule could be fully evaluated.
- Keep prerequisite, corequisite, exclusion, and recommended preparation semantics distinct. A planned concurrent course can be relevant to a corequisite. An exclusion relation can suggest possible similar preparation, and the student can confirm a valid equivalent through the published rule or the department. Show academic rules next to, rather than inside, timetable overlap logic.

### Compile public text into static data

The working prototype reads `cmCourseInfo.prerequisitesText`, qualifying academic text in `cmCourseInfo.note`, and relevant course-level `notes`. It strips markup, keeps the published wording, and compiles recognized expressions into JSON. Predicate types cover course completion, minimum grades, total credits, credits from an explicit list or subject and level, cumulative GPA, and confirmed facts such as approved equivalents. `all`, `any`, and `if` preserve combinations and conditions. The compiler records an expressly empty field as `none` and marks missing fields, unsupported phrasing, ambiguous scope, and potentially qualifying notes for review.

For example, the CSC207H1 offering rule is an `any` of the four listed exact course codes with their 60% thresholds, including CSC148H1. The tree keeps alternatives separate, so passing one verified alternative is sufficient. A finished course with a missing grade leaves that branch uncertain. A 59.99% grade is below the requirement even if the course was completed. The Java test also verifies that a source wording change from 60% to 70% changes the result after a file refresh with the same application code.

Supply the validated rule file separately from the course fetch. Java reads and evaluates it on the device. Match it to the exact offering and preserve the source date and published wording. A missing or outdated rule can show **Needs review** while search and timetable planning remain usable. The official TTB catalogue fetch is the external API integration for CSC207.

### Rule results and limits

| Situation | User-visible outcome |
| --- | --- |
| Required leaves all succeed, or one accepted alternative succeeds | **Prerequisites met**, with the decisive alternative or requirements |
| A required item fails, or every confirmed alternative fails with complete relevant facts | **Requirement missing**, with the missing course, grade, credits, or condition where known |
| A missing fact or unsupported rule could change the answer | **Needs review**, with the published wording and the information to verify |
| The source has an empty or expressly none-listed prerequisite field | **No prerequisite listed** |

The September archive contains 8,172 offerings. The compiler converted **3,632 of 6,352** with published prerequisite text and recorded **1,439** with none listed. It flagged **3,101** for review, including 370 missing fields, 385 qualifying notes, and 2,346 unsupported rules. Supported rules can still require a grade, an awarded credit, a verified approval, or a complete history before the app can give a definite answer. Averages over subsets, unresolved program or timing exceptions, and ambiguous grouping remain review cases.

Run the small upstream compiler when a complete source export changes. Publish the generated file to one static URL. The Java app can use a validated cached copy when a refresh fails and must check its wording against the current offering before using it. The compiled data requires no per-offering manual editing.

### Suggested Java implementation

Jingyan can start with `CheckPrerequisitesRequest(offeringId)`, the use-case-owned lookup interface, `AcademicProfile`, a simple evaluator, and an output with status, published text, and reasons. Keep Gson JSON objects and HTTP response parsing in the adapter. Use one interface and ordinary immutable Java objects for rule nodes; a small enum plus list/field records works. Test the evaluator with the bundled rules before connecting a background refresh or saving a profile. Key JSON by offering ID. Keep test cases for a missing code in a partial history, exact 60%, missing grade, ambiguous AND/OR, invalid version, and a failed refresh that retains the old file.

## Walking distance and transition warnings

The included walking file covers **2,281 same-campus pairs across all 84 archived buildings**. It retains 2,215 Concept3D estimates from the official U of T map and adds 66 FOSSGIS OpenStreetMap pedestrian estimates for **St. George:UY**, 700 University Avenue. Both sources use metres and durations rounded up to whole minutes. Keep each row's source when displaying the estimate. [Walking data](../reference/timetable-data/walking.json) · [Counts, samples, and provenance](../reference/timetable-data/README.md#walking-data-you-can-use-directly)

1. Preserve raw `meetingTimes[].building.buildingCode` as a separate field in the Java mapper. The current `location` string is for display and can contain a room suffix. Build the lookup key with the actual campus plus normalized building code. Different campuses reuse codes.
2. Read the file once into a map keyed by an unordered pair of campus/building keys. Treat those records as building-level estimates. Leave missing routes unknown and label a same-building transition clearly.
3. For each applicable actual day or known weekly occurrence, sort timed events and inspect consecutive, non-overlapping in-person meetings. Use published time for the grid and conflicts. Where U of T time applies, the arrival budget for a published back-to-back transition is ten minutes. Compare the estimate with the available gap plus the applicable allowance.
4. Show the building-to-building duration and distance when known. Automatically warn only when a **back-to-back** walk takes **more than 10 minutes** under the normal allowance. At exactly ten minutes there is no long-walk warning. An unknown room still allows an estimate if the building is known, and missing building, cross-campus, online-only, unclear recurrence, and an absent route should get an unavailable/inapplicable state. Estimate validity and true time conflicts are separate outputs.

A useful fixture has `St. George:BA` to `St. George:MP` at 3 minutes and 248 m, plus `St. George:AB` to `St. George:BR` at 11 minutes and 1,096 m. Check the OpenStreetMap estimate from `St. George:UY` to `St. George:PB` at 3 minutes and 201 m, the same pair in reverse, and the unknown result for a building outside the matrix. Use the local matrix for building-level estimates.

## Local persistence, share, and output

Use one small versioned snapshot with course basket, selections and labels, term, locks, personal blocks, preferences, and source timestamp or catalogue revision. Save the student's completed courses and optional grades separately or in a private profile portion. Validate imported versions, IDs, and shape before updating the shared active model. If a section vanished, retain its previous ID and label and prompt the student to resolve it. Prefer a short, human-readable JSON structure. The local store can use a temporary file and replacement after successful write.

Keep the plan and private profile formats separate. Put a version in each file and validate it before applying loaded data. Keep the prior plan when parsing or writing fails. Use ordinary Java file APIs and Gson.

Share a self-contained timetable link that another installation can preview and apply. Keep the private profile out of the payload. Check sender and receiver workflows. Use one layout for PDF, PNG, and print so exports show the same times, names, and overlapping blocks. Render PNG with the Java drawing APIs and `ImageIO`.

The reusable uncertainty contracts are consistent across cases. Preserve what the source actually says, distinguish timed and real-date completeness, distinguish availability from permission, recheck source identity when refreshing, and show uncertain rather than claiming a known answer from missing data. See the [catalogue evidence and regression scenarios](../reference/timetable-data/README.md) and the [UI sketches](ui-sketches.md).
