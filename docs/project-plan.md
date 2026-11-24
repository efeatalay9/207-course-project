# Timetable Builder specification and delivery plan

The finished CSC207 project is a desktop app U of T students can use to plan a term before enrolling. It provides the official Timetable Builder's planning workflows and five additional actions. This document starts with what the student can do, then covers the data, ownership, and delivery dates. The starting code was reviewed from `main` at `07ecb4a` on November 24, 2026. The assignments and internal dates are proposals for the team to confirm. [Java implementation](implementation-guide.md) · [UI sketches](ui-sketches.md) · [Course requirements and rubric](project-expectations.md)

## The finished app from a student's point of view

A U of T student wants to choose courses, see how their sections fit together, and have a fallback for enrolment day. They open the app on their computer and see which catalogue is loaded and when it was updated. They can plan with the bundled data offline. The app identifies itself as a CSC207 student project and sends students to ACORN for actual enrolment.

1. **Find offerings.** Pick a term and division, search by course code or by words in the course title or description, and narrow by factors such as breadth, meeting times, or available space. Opening an offering shows the course description and its actual lecture, tutorial and practical choices, their times, enrolment controls, and published links between sections.
2. **Build a plan.** Preview a section on the weekly grid, add or remove sections, and lock a choice you want to keep. Block time for a commute, job, or other commitment. While planning, **Fits My Timetable** can show another course with a complete section combination around the active schedule. The existing plan changes only on an explicit add or apply.
3. **Check the course requirements.** Add completed courses from catalogue suggestions and give a grade where relevant. Open an offering's prerequisite check. Read the outcome, the exact requirement that was met or is missing, and the published wording. If the app lacks a grade or cannot interpret a published condition, it tells you what to verify.
4. **Compare schedules.** Put the desired offerings in the course basket, pick Early, Balanced, or Late for each term, and generate complete combinations. The app keeps hard constraints such as blocked time and locks, shows meaningful alternatives, and explains the short breaks or term preferences that put one result ahead. Inspect overlaps and walking time on the grid before applying the result.
5. **Save, share, export, and prepare for changes.** Close and reopen a saved plan with the same courses, sections, blocks, locks and preferences. Share only the chosen timetable with a second installation or print/export the grid as PDF or PNG. When a selected section fills, mark it unavailable and preview a compatible replacement before applying it. Keep a separate copy of the original timetable for reference.

### Example of a complete session

A student already has a Monday afternoon class and blocks a Thursday commitment. They search for a Winter option. One offering's lecture fits, but its linked tutorial overlaps the commitment. **Fits My Timetable** shows another complete lecture/tutorial pair if there is one. When they check prerequisites, a recorded grade exactly at the published threshold can satisfy a supported alternative. If they omitted that grade, the app asks for it. The student adds the offering to the basket, compares two genuinely different weeks, and chooses the one with fewer short gaps. In the grid, a known 11-minute back-to-back walk gets a warning under the ten-minute transition convention. The student saves the plan and can get back to it after previewing a replacement for a tutorial that filled.

### Vocabulary used throughout the project

| Term | Meaning |
| --- | --- |
| Course and offering | A course code identifies a catalogue course. An offering is a particular instance in the right term, division and campus. One code can have multiple offerings. |
| Section, meeting, and linkage | A section is a selectable lecture, tutorial or practical. Each section can have several scheduled meetings. Linkage specifies which sections belong together. An offering can require more than one component. |
| Basket and timetable | The basket is the list of offerings the student wants to consider. The timetable holds the exact chosen sections. Previewing a candidate shows it temporarily. Applying commits its choices to the active timetable. |
| Lock and time block | A lock keeps one exact section during generation. A personal time block is an unavailable interval. A normal manual choice can be preferred while remaining movable. The replacement action can release the affected lock after the user specifically chooses to replace that section. |
| Availability and enrolment controls | Availability is the dated, published view of space or waitlist status. Enrolment controls describe when a course can be added and by whom. Check the real registration state in ACORN. |
| Fit, eligibility, and walking | These are separate results. **Fit** means at least one published complete bundle fits the current time constraints. **Eligibility** is based on published rules and the student's stated academic facts. **Walking** estimates campus travel between consecutive timed meetings. |
| MVP | The agreed minimum end-to-end demonstration for CSC207. It includes a lead use case per member and the supporting flows needed to run it. The official planning checklist defines the full product target, so an incomplete row remains visible even if the TA demo is a smaller subset. |

## Product and screens

Students should be able to start with a course they know, browse other courses that match their constraints, and build a timetable manually or from a course basket. After comparing choices, they can save a plan, share a view of it, and recover it when availability changes. This is a student planning app. Show the catalogue date and direct students to the official enrolment system for registration.

| Area | Controls | Result |
| --- | --- | --- |
| **Find Courses** | Faculty or division and session, query, advanced filters, search, reset, page controls, **Fits My Timetable** | A list of offerings with distinguishable terms, campuses, and course codes. Selecting an offering opens its academic and section details. |
| **Offering details** | Expand section information, **Preview**, **Add to Timetable**, **Lock**, **Add Course to Plan**, **Check Prerequisites** | Shows the published descriptions, requirements, sections, and conditions for that specific offering. Preview is temporary. Add changes the active timetable. Add to Plan changes the generation basket. |
| **My Timetable** | Term selector, both-term view, meeting selection, remove and lock actions, **Block Off Time**, walking estimates, **Find Replacement** | Shows scheduled blocks, overlap and walking explanations, plus a list of unscheduled or unresolved selections. |
| **Course Plan** | Basket, remove course, clear plan, Early/Balanced/Late per term, **Generate Timetable**, comparison, **Apply** | Shows valid alternatives with their differences and any constraints that still need attention. The active timetable changes after approval. |
| **Completed Courses** | Catalogue autocomplete, optional grade, edit or remove a record | Keeps the facts used for prerequisite checking on this device. |
| **File and Share** | Save and open plan, copy and open share link, print, PDF, PNG | Supports reopening for the same user and sending a timetable view to another user. |

Use labels students can recognize from Timetable Builder. Show friendly names for campus, breadth, delivery and credit choices. On initial launch, an empty grid teaches the next action. With no selected classes or blocked times, tell the student that there is nothing in their timetable to work around. Use notices for no results, unknown times or locations, unavailable data and unsuccessful imports. A failed action preserves the prior timetable. Keep course filters stable when searching or adding an offering, show both conflicting blocks on the grid, and use Toronto-local 12-hour display times.

## How the program works

In live mode the Java app loads the term and division choices, then fetches the offerings within the student's selected scope. It keeps returned offerings by ID so the timetable can still show selections from earlier searches. In offline mode it loads and indexes the bundled catalogue. An **entity** holds a timetable rule or piece of state, such as a section or meeting. A **use case** is one student action, such as finding a replacement. The **controller** turns the click and fields into a request. The **interactor** carries out the action and reports a result. The **presenter** converts that result into text and state the **Swing view** can show. Small data interfaces let the team swap a fixture for the real file or API in tests without changing how the action works.

| Job | Data and simple design |
| --- | --- |
| Read the catalogue | Use the official Timetable Builder API for live search. Load term and division choices before requesting offerings. Use the ZIP for offline use and tests. Map source IDs, times and sections in the data-access adapter. |
| Make and check combinations | Form legal sets of required sections within each offering. Compare timed meetings and blocks locally. Fit checks candidate courses against the active timetable. Generation tries combinations from the basket. Replacement checks the same offering with the filled section excluded and everything else fixed as far as possible. |
| Explain prerequisites | Validate the separately supplied rule file and evaluate it against the student's local profile. Keep published wording and show **Needs review** if the archived rule cannot be matched to the offering. [Rule table and compiler](../reference/prerequisites/README.md) |
| Estimate walking | Look up a pair of mapped campus buildings in the included walking matrix. Show minutes and metres. Warn when an applicable back-to-back walk exceeds ten minutes. Say when a route cannot be established. [Route data and provenance](../reference/timetable-data/README.md#walking-data-you-can-use-directly) |
| Store and share plans | Save versioned JSON on the computer, validate it before loading, and keep old selections visible if a catalogue update removes them. A private backup can include a local profile. A copied share contains only the intended timetable fields. A share or export can be previewed before it changes the receiver's plan. |
| Prove the external API | Demonstrate a live Term and Division search in the Java app using the official Timetable Builder. Record the filter scope, result and date. Retain the offline ZIP for reproducible cases. |

Use a single active plan, one shared set of rules for detecting clashes, and a simple versioned local file format. Keep network and potentially long searches off the Swing event thread. The [implementation guide](implementation-guide.md) provides the file formats, identifiers, source edge cases, search results, ranking order and handoffs. The [architecture document](architecture.md) shows the Java packages and runtime sequence.

To get oriented in the current checkout, run `./test.sh` and open `./run.sh --sample` with Java 17 or newer. Try exact-code search, add a section, switch terms, and remove the selection. That shows the existing build before the other features in this specification are implemented. [Launch commands and baseline](../README.md)

## Team ownership and repository baseline

| Proposed lead | Complete user goal | Supporting official use cases |
| --- | --- | --- |
| Matthew Chen | UC-05 Find Courses That Fit My Timetable | UC-01 Find and Filter Courses |
| Jingyan Lin | UC-07 Check Prerequisites Against My Completed Courses | UC-02 Inspect an Offering and UC-03 Preview, Add, or Lock a Section |
| Vincent He | UC-08 Check Walking Time Between Classes | UC-04 Review the Timetable and Remove a Section and UC-06 Block Off Time |
| Efe Atalay | UC-11 Compare Generated Timetables | UC-09 Manage the Course Plan and UC-10 Generate a Timetable |
| Leon Ren | UC-13 Replace a Section That Filled Up | UC-12 Save and Open a Plan, UC-14 Share a Timetable, and UC-15 Export or Print a Timetable |

A contribution check of the merged code and GitHub PRs guided these assignments. Matthew and Vincent already built the two largest Java slices, so their first targets extend their existing screens. Jingyan already built section addition and can add the academic check to the same offering view. Efe's catalogue setup and draft live API are the foundation for his next full scheduling use case. Leon gets **Save and Open Plan first** as an independent end-to-end feature, with replacement next. Efe and Leon should get substantive feature work into review early in this pass. [Contribution evidence by teammate](repository-audit.md#contribution-evidence-for-assignments)

A lead writes most of the interactor, its boundaries and request/response types, controller, presenter, and the Swing trigger and update for their use case. Each lead tests both successful and alternate outcomes. Collaborate on shared classes through review. A member can lead more than one related use case. [Course implementation checklist](project-expectations.md#how-to-implement-a-use-case)

The reviewed baseline at `07ecb4a` had exact-code lookup, autocomplete, basic details, section addition and removal, and a per-session weekly grid. The [integration PR](https://github.com/efeatalay9/207-course-project/pull/9) adds scoped live searches and validates section choices before adding them. The five extra cases remain planned in this repo. The prerequisite rule converter described below was tested as a separate prototype.

## Official planning workflow checklist

Keep these as acceptance checks for the planned CSC207 app. Mark an item complete when its normal and error paths work in the integrated Swing build.

| Official workflow | Acceptance check | Use case and current status |
| --- | --- | --- |
| Division, session, and term selection | Choose one or more faculties or divisions and search Fall, Winter, full-year, or Summer when offered. Refresh dependent course and department choices when the selection changes. | UC-01 planned |
| Search and suggestions | Match a full code case-insensitively, suggest codes by prefix, and find offerings by title or description. Show each distinct offering for a code. | UC-01 code search and suggestions merged. Title and description planned. |
| Additional filters | Combine department or subject, breadth or distribution, instructor, level, delivery mode, weekdays, time of day, campus, and credit weight. Read values from the catalogue and show useful labels. | UC-01 planned |
| Primary-section availability | Provide all courses, courses with available space, and courses with space or a waitlist. Apply these choices to the source-designated primary section. Display data freshness and distinguish group enrolment controls from general availability. | UC-01 and UC-02 planned |
| Search controls and navigation | Reset filters, move between pages or browse the complete result set, expand details, and show an empty-result or load-error state. | UC-01 partial |
| Offering details | Show code, title, session, division, campus, department, description, credits, breadth or distribution, prerequisites, corequisites, exclusions, recommended preparation, notes, and syllabus link where published. | UC-02 partial |
| Section details and explanations | Show teaching method, instructor, section time and recurrence, building or map link, delivery mode and definitions, availability, waitlist, enrolment controls and explanations, and published links between components. | UC-02 partial |
| Preview, choose, and lock | Preview a section on the grid and clear the preview without changing the timetable. Add a chosen section. Lock or unlock it explicitly. | UC-03 basic add merged |
| Weekly views and conflict display | Open one term or both. Show each meeting at the right time. Select a block to see its section, remove it when requested, and show all simultaneous blocks and their conflict explanation. | UC-04 single-session grid and remove merged. Both-term view and conflict decisions planned. |
| Unscheduled and exceptional meetings | Keep confirmed async, TBA time or location, arranged, alternating, and cancelled states distinct. Show known in-person assessments for online sections where the source records them. | UC-02 and UC-04 partial |
| Block Off Time | Choose a term, day, start, and end. Display, remove, or revise the block. Exclude its overlaps during generation. | UC-06 planned |
| Course basket and generation | Add a course separately from adding a section. Remove one or clear the basket. Generate complete combinations with the term preferences, locks, personal blocks, and published component rules. Show actionable conflicts or incomplete data. | UC-09 and UC-10 planned |
| Save and reopen | Save a plan, close the app, reopen it, and compare course choices, sections, locks, blocks, and preferences. | UC-12 planned |
| Shareable link | Copy a plan link and open it in a separate Swing installation. Show the resulting timetable and keep the recipient's current plan until they accept it. | UC-14 planned |
| PDF, PNG, and print | Produce the selected term view or both-term views. Open the PDF and PNG, check labels and overlapping meetings, and preview the same layout for printing. | UC-15 planned |

## Lead use cases

### UC-05 Find Courses That Fit My Timetable

**Lead and actor.** Matthew Chen. The actor is a student planning another course.

**Preconditions and trigger.** A catalogue and an active timetable are available. The student applies **Fits My Timetable** to a normal search.

**Success outcome.** A confirmed offering shows at least one complete section combination that fits the student's existing selections and personal blocks.

1. Run the selected catalogue filters to get candidate offerings.
2. Build valid component combinations for each candidate. Compare known meetings with the active timetable and personal blocks.
3. Show one fitting combination and its lecture, tutorial, and practical choices. Distinguish confirmed fits from offerings that still need a check.
4. The student can inspect the offering, preview the suggested sections, or choose a separate add action.

**Extensions.** At step 2, unclear linkage or TBA time prevents a confirmed fit. A genuine asynchronous section with no timed meetings can fit the time check. With an empty timetable, explain that there are no existing selections or blocks to constrain the search. With no complete bundle after a complete search, say what prevents the fit. Hold current selections fixed during this check, including unlocked selections.

**Acceptance fixture.** A lecture fits on its own, but its mandatory tutorial overlaps a block. A second linked tutorial fits. Show the legal lecture/tutorial pair. A tutorial belonging to another lecture must never count as the fitting choice.

### UC-07 Check Prerequisites Against My Completed Courses

**Lead and actor.** Jingyan Lin. The actor is a student checking a specific offering.

**Preconditions and trigger.** An offering is open. The student selects **Check Prerequisites**.

**Success outcome.** The app states which listed requirements are satisfied, which are missing, or which require review, with the published wording available.

1. The student adds completed courses from catalogue suggestions. They may add the grade required by a minimum-grade rule.
2. The interactor gets the rule for the offering ID. The evaluator compares it with completed course codes, known grades, and any supported profile facts kept locally.
3. The presenter shows **Prerequisites met**, **Requirement missing**, **Needs review**, or **No prerequisite listed**. When possible it explains the decisive requirement.
4. The student can edit a grade or completed course and check again.

**Extensions.** At step 1, distinguish an empty or incomplete history from a confirmed history. Planned and in-progress courses do not satisfy a completed-course requirement. At step 2, an unsupported or missing rule, missing grade, or unresolved equivalence stays uncertain when it affects the answer. Reuse the last valid rule file after a refresh failure and display its date. An exclusion is evidence to check for equivalent preparation. Give credit for a substitution only when a published rule or verified equivalency supports it.

**Acceptance fixture.** The archived CSC207H1 rule accepts 60% or more in one of four specifically listed courses. A fixture with 60% in CSC148H1 satisfies that branch. A fixture with 59% and a complete history without another qualifying option does not. A missing grade produces a request to check it.

### UC-08 Check Walking Time Between Classes

**Lead and actor.** Vincent He. The actor is a student inspecting a timetable.

**Preconditions and trigger.** The timetable has two consecutive timed meetings. The student opens the grid or selects the transition between them.

**Success outcome.** The transition shows the estimated walk time and distance, and a warning appears for a known back-to-back walk over ten minutes when the U of T start convention applies.

1. Identify consecutive, non-overlapping meetings on the same day and applicable occurrence.
2. Resolve the locations to campus building IDs and read the route estimate.
3. Show duration and distance between the meetings. For a scheduled back-to-back pair, count the normal ten-minute transition where applicable.
4. Flag a known walk longer than that transition. Show the buildings and estimate so the student can evaluate it.

**Extensions.** At step 1, an actual overlap is a conflict. At step 2, a known building with a missing room supports a building-level estimate. Unknown buildings, uncertain dates, unsupported campus pairs, and online activities without a physical location leave the walk unconfirmed or inapplicable. At step 4, a ten-minute walk gets no long-walk warning.

**Acceptance fixture.** Show estimates for two back-to-back pairs at nine and eleven minutes. Warn only for eleven. Repeat with an unknown building and no invented estimate.

### UC-11 Compare Generated Timetables

**Lead and actor.** Efe Atalay. The actor is a student picking a weekly schedule.

**Preconditions and trigger.** A course plan has produced valid timetable candidates. The student opens the generated alternatives.

**Success outcome.** The student can compare meaningfully different options, understand their order, and apply the preferred timetable.

1. Display the best-ranked valid option and the available alternatives.
2. Use the selected term preference and a short-gap score to explain the order. Show the count or total duration of short gaps and the changed meeting blocks. Start with a proposed short-gap range of 20–75 minutes and display it with the result.
3. Return up to a small, useful set of schedules. After ranking, skip options that differ only in section labels or that make trivial meeting-time changes. Keep the best result first.
4. When the student chooses **Apply**, replace the active choices with the selected option and refresh the grid.

**Extensions.** At step 1, showing one valid option is a success with one result. When generation ends early, label the results found so far. At step 4, rerun the shared validity checks if the plan has changed while results are open. Preserve the active timetable when the student cancels or a result can no longer be applied.

**Acceptance fixture.** Three candidates have the same hard validity. Two place the same meetings at the same times. The third moves a meeting to another morning and leaves one fewer short gap. Show the meaningful alternatives and why their order changes when the preference changes.

### UC-13 Replace a Section That Filled Up

**Lead and actor.** Leon Ren. The actor is a student preparing to enrol.

**Preconditions and trigger.** A timetable is open and contains a selected section. The student chooses **Find Replacement** and marks the section unavailable if necessary.

**Success outcome.** The app proposes a compatible same-course combination with the fewest necessary changes, shows its availability and changes, and applies it only on approval.

1. Treat the selected section as unavailable for this repair.
2. Check other sections of the same offering. Keep other courses, personal blocks, and unaffected locks fixed. Change linked tutorials or practicals in this offering only where required.
3. Rank viable alternatives by fewest changed sections and then the established preference order. Display the course data timestamp and whether availability is confirmed or needs checking.
4. Preview the result. If the student accepts, apply it to the active plan and offer to preserve the original in a separate backup. The replacement can also be saved independently.

**Extensions.** At step 2, a confirmed unavailable section cannot serve as the replacement. Unknown or stale availability may support a time-compatible suggestion labelled **Verify availability**. If no candidate fits, explain the constraint and keep the active timetable. At step 4, cancelling or closing the preview keeps the original choices.

**Acceptance fixture.** A chosen tutorial fills. One alternate tutorial belongs to a different lecture. A correctly linked tutorial fits without changes to other courses. Show the legal option and confirm that the saved original remains available.

## Supporting use cases

The primary actor below is a student. The recipient is the primary actor for opening a shared link.

### UC-01 Find and Filter Courses

**Lead and actor.** Matthew Chen. The actor is a student browsing the catalogue.

**Preconditions and trigger.** The catalogue is loaded. The student opens **Find Courses** and submits a search or chooses a code suggestion.

**Success outcome.** The result list contains offerings that meet the selected search criteria.

1. Choose a division and session. Enter a code or keyword and select any additional filters.
2. The app applies the criteria and shows the matching offerings. Each result identifies its term, campus, code, and title.
3. Browse results, open details, or reset the filters for a new search.

**Extensions.** At step 1, blank optional criteria browse the selected division/session. The current exact-code-only view still validates a blank code. At step 2, an empty match list and an unavailable catalogue have distinct messages, and both replace stale results.

Use course fields for faculty, terms, credits, breadth, and description. For a time, weekday, delivery, or instructor filter, document the source semantics. Confirm consistent combined conditions before saying a particular section matches. An offering-level filter match alone is insufficient to confirm that a complete bundle fits.

### UC-02 Inspect an Offering

**Lead and actor.** Jingyan Lin. The actor is a student considering a specific offering.

**Preconditions and trigger.** A result is present. The student opens its details.

**Success outcome.** The student sees the academic and section information associated with that offering.

1. Show course metadata, notes, and published academic requirements.
2. Display the selected section's times, location, delivery, and enrolment information. Expose delivery definitions, course links, and enrolment explanations where provided.
3. The student can compare sections, preview one, or open the prerequisite check.

**Extensions.** At step 1, keep the published wording visible beside any computed check. At step 2, label unavailable fields and preserve cancelled section labels. An inaccessible external syllabus or map leaves the rest of the offering visible.

### UC-03 Preview, Add, or Lock a Section

**Lead and actor.** Jingyan Lin. The actor is a student choosing a section.

**Preconditions and trigger.** An offering is open and the student has chosen a section. The student selects **Add to Timetable**.

**Success outcome.** The selected section appears in the active timetable with its meeting or availability state.

1. The app checks the offering ID, section identity, and cancellation status.
2. Add the requested selection and update the weekly display from the timetable state.
3. If the student decides to preserve that choice during generation, use **Lock**. **Unlock** restores flexibility.

**Extensions.** Before step 1, **Preview** shows a temporary overlay. At step 1, reject a missing or cancelled section. At step 2, a duplicate keeps one choice. A TBA choice is shown without a timed block. Manual additions can show an overlap so the student can explore alternatives.

### UC-04 Review the Timetable and Remove a Section

**Lead and actor.** Vincent He. The actor is a student reviewing or adjusting selected sections.

**Preconditions and trigger.** The catalogue is loaded. The student opens **My Timetable** or changes term.

**Success outcome.** The selected term view shows each known meeting and provides a clear status for each other selection.

1. Read selected sections and resolve their offering and meeting data.
2. Place the timed meetings in the correct term, day, and time. Show all overlapping blocks and the conflict explanation. List unknown or unscheduled entries alongside the grid.
3. The student chooses a block or section row to inspect it.

**Extensions.** At step 1, an empty plan has a clear starting state and a missing selection remains visible for repair. At step 2, uncertain recurrence may leave an overlap unconfirmed. Switching the term or choosing both terms refreshes the displayed grid.

**Remove a selected section.** The student selects **Remove** with one of their sections selected. The remove use case deletes that selection, rebuilds the timetable display, and confirms the removal. If the selection is empty or already absent, report the outcome and keep the other selections. The existing remove interactor remains a separate case from the weekly view.

### UC-06 Block Off Time

**Lead and actor.** Vincent He. The actor is a student adding a personal constraint.

**Preconditions and trigger.** An active plan is open. The student selects **Block Off Time**.

**Success outcome.** The interval appears on the timetable and constrains fit and generation.

1. Enter the term, weekday, start, and end.
2. Validate the interval and add it to the plan.
3. Show the result in the grid. The student can edit or remove the block.

**Extensions.** At step 2, an invalid or reversed interval leaves the plan intact. At step 3, an existing class overlap is visible as a conflict with this block.

### UC-09 Manage the Course Plan

**Lead and actor.** Efe Atalay. The actor is a student choosing courses for generation.

**Preconditions and trigger.** The student has an offering open and chooses **Add Course to Plan**, or opens their existing basket.

**Success outcome.** The basket contains the offerings the student wants the generator to consider.

1. Add the selected offering and show its identity and period in the plan.
2. Review the basket and remove an offering or clear the basket if desired.
3. Open the generation action with the remaining offering list.

**Extensions.** At step 1, a repeated add keeps one entry. At step 2, removing a basket item changes generation input. A separate action controls the sections on the active timetable. At step 3, an empty basket prompts the student to add courses.

### UC-10 Generate a Timetable

**Lead and actor.** Efe Atalay. The actor is a student generating section choices for their planned courses.

**Preconditions and trigger.** The basket contains offerings. The student chooses Early, Balanced, or Late per term and selects **Generate Timetable**.

**Success outcome.** The app presents at least one complete configuration satisfying the selected hard constraints.

1. Read the basket, term preferences, active locks, and personal blocks.
2. Build each offering's allowed sets of sections. Search combinations with required components, published linkage, known overlaps, locks, personal blocks, and any explicit availability constraint.
3. Display the resulting candidates and relevant diagnostics. Open UC-11 for comparison and application.

**Extensions.** At step 2, indicate a proven infeasible outcome only after exhaustive checking. An early stop or cancelled search reports incomplete results. Unknown linkage, times, or availability is a separate data outcome if it affects a decision. At step 3, preserve the user's active plan when they have yet to apply a candidate.

The [solver outcomes and ranking](implementation-guide.md#check-times-and-generate) specify how to represent completed and stopped searches and keep meaningfully different high-ranking results.

Start with backtracking and prune a partial selection when an added bundle conflicts. Order courses by fewest possible bundles if needed. Limit retained results separately from the search-completion status. Present candidates in UC-11 and apply after the student selects one.

### UC-12 Save and Open a Plan

**Lead and actor.** Leon Ren. The actor is a student saving or resuming their work.

**Preconditions and trigger.** A plan is open or a saved plan file is available. The student selects **Save Plan** or **Open Plan**.

**Success outcome.** The timetable can be restored after the app closes with its choices and settings intact.

1. Choose a file location and save the plan with a version, the catalogue date, selected offering/section IDs, locks, blocks, basket, and preferences.
2. Open the saved file from the same app after a restart.
3. Validate the full file and show the restored state on the timetable and course plan screens.

**Extensions.** At step 1, a cancelled dialog or a write failure leaves the active plan available. At step 3, a broken file or unsupported version leaves the current plan open. Keep missing selections unresolved with their original identifiers and display labels. Keep the local academic profile separate from timetable shares.

### UC-14 Share a Timetable

**Lead and actors.** Leon Ren. The students are the sender and recipient of a timetable.

**Preconditions and trigger.** The sender has a populated plan and selects **Copy Share Link**.

**Success outcome.** The recipient can open a preview of the shared timetable in another Swing installation.

1. Copy the generated link for the sender's chosen timetable.
2. The recipient uses **Open Shared Link** to preview it.
3. The recipient may apply the timetable or close the preview.

**Extensions.** At step 1, include only the intended data and handle a clipboard or storage failure. At step 2, an invalid or expired link leaves the recipient's active plan available. Test the link without the sender's local file. Use the simplest supported delivery, such as a self-contained plan URI. If the team selects a storage API, publish a share-specific snapshot after the explicit share action.

### UC-15 Export or Print a Timetable

**Lead and actor.** Leon Ren. The actor is a student keeping or distributing a timetable view.

**Preconditions and trigger.** The timetable is open. The student selects a term view and **Export PDF**, **Export PNG**, or **Print**.

**Success outcome.** The file or print preview presents the selected plan and dates legibly.

1. Choose the desired term view and format.
2. Produce the timetable layout with time labels, overlapping blocks, and course and section labels.
3. Open the saved file or review the print preview.

**Extensions.** At step 2, retain the plan and show feedback if export fails or the student cancels. At step 3, check a crowded fixture and both terms. Reuse one layout model for PDF, PNG, and print.

## Shared rules and data contracts

| Decision | Behaviour |
| --- | --- |
| Offering and section identity | Reuse the existing offering ID and section name model. Include term and campus context for display and validation across catalogue updates. A course code such as CSC207H1 can refer to several offerings. A saved selection preserves its ID and stays unresolved if it disappears. |
| Component combinations | Use published required/optional components and linkage. Include each required lecture, tutorial, or practical exactly as the source specifies. An incomplete or contradictory linkage rule stays uncertain. Avoid inferring linkage from section-number proximity. |
| Meeting states | Preserve term, day, start/end, recurrence, location, and cancellation. Separate true asynchronous/no meeting, TBA time, TBA room, arranged dates, and cancelled sections. A known time with an unknown room can be checked for timed overlap. |
| Overlap | Compare meetings within the same occurrence using half-open intervals. Terms that do not intersect can reuse a time slot. With unresolved dates, distinguish a possible overlap from a definite overlap. Keep display-lane grouping separate from scheduling decisions. |
| Lock and block semantics | An explicit lock preserves that section through generation and replacement. Personal time blocks are unavailable intervals. A repair can replace the section the student marks unavailable after confirmation. |
| Generation results | Use distinct outcomes for viable results, exhaustive infeasibility, incomplete search, unknown source data, and errors. Carry conflicts or missing values through the output boundary. |
| Preferences and comparison | Apply hard rules first. Then rank viable candidates using agreed user priorities and term/gap preferences. Explain measurable tradeoffs and keep a few genuinely different candidates. Simplicity matters more than searching every ranking permutation. |
| Availability | Record the public observation date. A stale observation becomes uncertain for an availability-only constraint. A recent reported opening still requires the student's enrolment conditions, as described in the source. |
| Walking calculations | Preserve published time on the grid. Apply the ten-minute U of T transition convention when estimating an appropriate back-to-back walk. Use the routed building estimate and label unknown routes. |
| Local student data | The rule refresh is a fixed public data download. Checking completed courses, grades, and schedules uses local Java data. Plan shares include only the timetable data needed by the recipient. |

### Catalogue and external services

Load the archived ZIP and keep the code index in offline mode. Use official filter choices and scoped requests in live mode. Add missing metadata to the shared JSON mapper as each use case needs it. Preserve session tags verbatim when a display name is unknown. Treat September 5, 2026 availability as snapshot data. Use publication or collection time for the course data timestamp.

The prerequisite prototype generated a static rule file from the archived catalogue and applied it in Java 17. Rules cover course combinations and alternatives, grades, total or scoped credits, cumulative GPA, and confirmed conditions. It processed **8,172** offerings and compiled **3,632 of 6,352** with published prerequisite text. It recorded **1,439** with none listed and **3,101** for review. Its loopback test regenerated a rule after a source edit and verified that downloading the updated file changed the local answer without sending a student profile. [Prerequisite compiler, evaluator, and tests](../reference/prerequisites/README.md) · [Integration design](implementation-guide.md#prerequisites-and-academic-history)

For integration, keep lookup by offering ID and check the source wording and qualifying notes. Validate the downloaded version and rule types and retain the last valid copy if the download fails. Run the compiler upstream and keep ambiguous cases reviewable in the UI.

Draft PR #9 uses the official Timetable Builder API for direct, scoped searches. Start with term and division choices, then fetch within those selections. Exact-code search, prefix suggestions, offline mode, and no-results handling are in that draft. The packaged Java code verified a Fall 2026 CSC108H1 search in Arts & Science. Keep archived prerequisite rules as a separate input. [Draft API PR #9](https://github.com/efeatalay9/207-course-project/pull/9) · [Course API guidance](quercus-project-guide.md#api-selection-and-verification)

### Scheduling implementation and ownership

1. Jingyan and Vincent extend the existing add and remove flows using the same selection identity, status labels, and grid refresh.
2. Matthew, Efe, and Leon agree on operations for building valid section combinations and checking schedule constraints. Inject the catalogue behind a use-case-owned interface. Keep the pure combination and overlap logic reusable by the three interactors.
3. Vincent uses the same overlap decision for calendar conflicts. The view decides lane positions, colours, and labels.
4. Efe builds the search and ranking over valid bundles. Matthew calls the fit check on filtered candidates. Leon calls the same checker for repair while holding other courses fixed.
5. Leon keeps local plan serialization in the data adapter. Jingyan keeps the local profile and public rule mapping in their adapters. `Main` connects these to the same active plan.

Use ordinary Java collections and small methods. The sorted index handles offline autocomplete. For live queries, send supported filters to the official service, then apply any remaining filters to the returned offerings. Use Swing background work for network requests and generation tasks that may take noticeable time. Publish UI updates on the event dispatch thread.

## Integration and verification order

### First work session after the PR

Once the combined PR lands, update from `main`, run `./test.sh`, and open `./run.sh --sample`. Try searching, adding a section to the weekly grid, and removing it. Confirm the five proposed leads and pick the fixture the team will use for the next checkpoint. Work that uses the current sample data can start during PR review.

**Efe owns the first shared coding task.** Write one small test for choosing a complete linked lecture and tutorial around an existing class. Use the existing `SelectedSection(offeringId, sectionName)` to identify choices. Implement the shared check with ordinary lists and a time-overlap function. Aim to have the check and its test ready by the end of November 25. Matthew and Leon should review which combinations the checker returns before wiring their use cases to it. The same check powers **Find Courses That Fit**, **Generate and Compare**, and **Find Replacement**. Leon starts the complete Save and Open use case in parallel using the selection IDs already on `main`.

For the first test, give the student a fixed Monday class from 10 to 11 a.m. A candidate has a Tuesday lecture and three linked tutorial choices. The Monday 10:30 a.m. tutorial overlaps. The Monday 11 a.m. and Thursday 1 p.m. tutorials fit. Generation can return both workable combinations. If the Monday 11 a.m. tutorial fills, replacement keeps the lecture and chooses Thursday. Add the unresolved-data and other edge cases using the existing [scheduling contracts](#shared-rules-and-data-contracts).

### Who starts what in parallel

These are proposed team targets for the November 27 checkpoint. Each lead owns the code and tests for their case and asks a teammate to review their feature PR.

| Lead | Start November 24–25 | Aim to have reviewable by November 26 |
| --- | --- | --- |
| **Efe** | Write the shared fixture and the linked-section and overlap check. Agree on its inputs and results with Matthew, Leon, and Vincent. | Open a **Generate and Compare** feature PR. Generate the two valid options from the fixture and show their order and a concrete difference in Swing. |
| **Matthew** | Review how the API change uses the search ports, then wire **Fits My Timetable** to the existing search results with a small test fake. | Use the shared check to show a complete lecture and tutorial choice that fits, with an interactor test for a tutorial that overlaps. Extend the rest of the catalogue filters after this path works. |
| **Jingyan** | Use the validated Add Section and offering view as the starting point for a local completed-course profile and prerequisite lookup. | Wire **Check Prerequisites** for the archived CSC207H1 example. Test a grade at the published cutoff, one below it, and a missing grade. |
| **Vincent** | Load the included walking matrix and map two selected meetings to their campus and building codes. Help check the fixture's meeting times against the grid. | Show a walk in the weekly timetable and test the back-to-back warning and an unknown route. Add personal time-block controls after this path works. |
| **Leon** | Start **Save and Open Plan** in his own feature branch. Save the existing offering IDs and section names to a versioned local file and restore them after a restart. | Open the **Save and Open Plan** feature PR with a round-trip test, an invalid-file test, and a working Swing action. Then connect **Find Replacement** to the shared fixture to preview a compatible tutorial. |

### Order of integration

| Order | Work | Review evidence |
| --- | --- | --- |
| 1 | Confirm owners, run the merged app, and settle the shared scheduling test. Start the five assignments in parallel. | Each lead can name their first task, the person they depend on, and the result they will test. |
| 2 | Use the shared check in fit, generation, and replacement. Integrate the prerequisite and walking actions with the grid and details. | Show a complete linked choice, a conflict or uncertainty, an alternate schedule, a cutoff grade, and a long walk. |
| 3 | Bring the agreed MVP together by November 27. Run the offline demo, a real scoped TTB search, and save/reopen. | Each interactor has a unit test. The same integrated build can demonstrate the team's agreed cases. |
| 4 | Finish the supporting cases assigned to each lead. Extend official search, offering details, section preview and locks, time blocks, and both-term views. | Verify the remaining official planning rows, an unavailable data source, and a saved selection that needs review. |
| 5 | Finish sharing, export, and all remaining parity rows. Run the course tests, style tool, coverage report, and rehearsed presentation. | Open a share link on a second installation, inspect PDF/PNG outputs, and have each lead demonstrate the assigned story. |

## Timeline and completion evidence

Dates use Toronto local time. Course dates and requested outcomes are from the published timeline and lab instructions. Suggested work between them reflects the November 24 baseline. Check the team's agreement, the actual PR record, and TA feedback to update the statuses. [Course timeline](https://q.utoronto.ca/courses/444184/pages/project-timeline-2) · [Lab requirements](quercus-project-guide.md) · [Repository audit](repository-audit.md)

| Date | Course checkpoint | Team work and evidence |
| --- | --- | --- |
| **Nov 9–13** | Lab 7 blueprint and initial build, due Fri Nov 13. | Blueprint with leads, entities and UI sketches. Core entities and initial Swing search are visible. Confirm what the team agreed with the TA. |
| **Nov 16–20** | Lab 8 progress and end-to-end prototype, due Fri Nov 20. Each member should have a substantial PR and a teammate review. | The baseline contains runnable search, add, grid and remove. Check the team's actual PR/review records, successful external calls and MarkUs `GroupProject` repo URL submission. |
| **Nov 23–25** | Lab 9 progress demo for TA feedback on Nov 23 or 24. | Use the current build to show what already works. Agree on lead assignments, the demo subset, the shared offering/section IDs and bundle shape. Settle the real external API path early and keep a deterministic fixture. Draft or update one tracked issue per lead so dependencies and review owners are visible. |
| **Nov 25–27** | Integrated MVP with at least one unit test per interactor by Fri Nov 27. | Target five working vertical slices with shared fixtures and a runnable flow on `main`. Run one real external call from the app, open a saved plan again, and have each lead test one success and one important alternate path. If something is still pending, show it accurately and agree on the recovery with the TA. |
| **Nov 28–Dec 1** | Final-polish lab Nov 30 or Dec 1. | Each lead closes their remaining [supporting use cases](#team-ownership-and-repository-baseline), including both-term grid, filter coverage, sharing and export. Run a clean launch from another checkout or laptop. Test an unavailable data source, stale availability and an unresolved saved selection. Show the TA the chosen pattern in actual code and the save/reopen and external API evidence. |
| **Dec 2–4** | Fix outstanding bugs, reach 100% line coverage in the Entity and Use Case layers, and be ready to present by Fri Dec 4. | Run `./test.sh`, Checkstyle or equivalent, and the JaCoCo report. Target the remaining missed paths, review representative PRs and rehearse all handoffs with a timed demo. Check all applicable rows in the final acceptance checklist. |
| **Dec 5–6** | Suggested rehearsal and recovery time. | Test the packaged app and demo fixture again. Prepare the same fixture on a second machine or capture the demo. Record the endpoint result and selected snapshot date. |
| **Dec 7–8** | Team presentations in the assigned lab slot. | Each member speaks. Stay within 15 minutes for the prepared talk and demo, with up to 5 more minutes for questions. Use the final Quercus slot announcement. |
| **Dec 8** | Individual MarkUs reflection and peer evaluations. Separate Clean Architecture Reflection Quiz by 11:59 p.m. | Each person checks their own submission. Cite relevant PRs, reviews and contributions in the reflection. |

### When a feature is complete

- The relevant normal and alternate steps work through the real Swing entry point in the integrated build. The team can run and reproduce them using a named fixture.
- The interactor has a meaningful unit test, its output boundary is exercised, and the test suite passes. Check that the domain, JSON, and UI concerns are in their respective packages.
- The feature has a reviewed PR with the author's role, test evidence and any known limitations. The lead updates the status in the README and plan once it reaches the shared branch. [Review workflow](code-review.md)
- A shared release has all official planning rows and five extra cases verified, a run path from a fresh checkout, a local save/open check, and at least one documented live external API call from the app. Track unfinished rows explicitly at course checkpoints.

## Design choice to show in the presentation

The smallest proposed pattern outside the six course-notes examples is **Memento** for recovering an original plan. When a student confirms a generated schedule or replacement, the active plan provides an immutable snapshot of what they chose before the change. The save/apply workflow holds the snapshot and can offer it back to the plan through the same validated restore path. Use the existing intended backup action and test that it restores the original sections, locks and blocks. This gives the pattern a visible purpose without a history system. Efe and Leon can agree on snapshot creation and restore. Memento is covered in the Week 10 exercise, so confirm how it fits the team's targeted rubric band with the TA. [Course design criterion](project-expectations.md#design-pattern-expectation)

## Final demo

Use one short, prepared plan in the final demo. Find a candidate that fits, inspect its prerequisites, generate and compare schedules, inspect a walking warning, save and reopen, then replace a section marked unavailable. Each lead can demonstrate their action on the same plan. Prepare one deterministic fixture and a dated live API result. Use the remaining time to show how the request moves through the layers, two concrete SOLID decisions, the implemented pattern and extension point, tests and coverage, style evidence, and one substantive PR review. Have the runnable app available for questions. [Presentation instructions and complete grading checklist](project-expectations.md#presentation-and-demonstration)
