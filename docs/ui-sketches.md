# Timetable views and Lab 7 sketch

Draft for the team's Java Swing app. The layouts show inputs and outputs needed by the use cases. Example times and course combinations below are demonstration values. [Use-case specifications](project-plan.md) · [Lab 7 expectations](quercus-project-guide.md#lab-7-and-milestone-1)

## Main window

```text
U of T Timetable Planner                    Student project · Work in progress
Course data updated Sep 5, 2026                [Planning help] [Save] [Open]
[ Find Courses ] [ My Timetable ] [ Course Plan ] [ Completed Courses ]

Find Courses           Results                        Course details
Session [Fall 2026 ▾]  CSC108H1 · St. George          Title and term
Division [All ▾]       CSC148H1 · St. George          [Description]
Course/code [_____ ]   …                              [Academic Requirements]
[Filters] [Search]     [Previous] [Next]              [Sections and Availability]
[Reset] [Fits My Timetable]                           [Preview] [Add to Timetable]
                                                      [Lock] [Add Course to Plan]

My Timetable          [Fall ▾] [Both terms]            Selected sections
                      Mon   Tue   Wed   Thu   Fri     CSC108H1 LEC0101 · Preferred
  9:00 a.m.            ┌──┐                           [Remove] [Lock] [Replacement]
 10:00 a.m.            └──┘                           TBA and unresolved choices
                      [Block Off Time]                [Share] [PDF] [PNG] [Print]
```

The tabs are a simple way to reach the main surfaces. They can reuse the existing search results and weekly grid while the layout evolves. Keep form state stable during search and add. Use course terms such as **Lecture**, **Tutorial**, **Practical**, **Availability**, **Enrolment Controls**, and **Course data updated**. Enter runs the search when the search field has focus. Every action should have a usable keyboard path and a useful text label. The timetable displays all overlapping blocks in adjacent lanes with an explanation when one is selected.

## UC-05 — Find Courses That Fit My Timetable

| Before | Action and successful result |
| --- | --- |
| Course results show the ordinary search. The status says which plan and term will be used. Existing CSC108H1 meetings and a Tuesday personal block appear on the grid. | Click **Fits My Timetable**. A result displays **Fits with LEC0201 and TUT0202** and describes which current meetings and blocks were checked. **Preview these sections** paints them temporarily. |

Alternate results. **Your timetable is empty** explains that there are no existing classes or blocks to work around. **Needs a check** identifies a TBA meeting or unresolved linkage. **No matching combination** identifies a forced overlap after complete search. A separate search-failure message gives a retry path. Clear previews when changing result, term, or plan.

## UC-07 — Check Prerequisites Against My Completed Courses

| Before | Action and successful result |
| --- | --- |
| Offering details show the official prerequisite text. The Completed Courses panel has **Add a completed course** with catalogue autocomplete, optional grade, and an explanation of the history-completeness choice. | Complete the local record for CSC148H1 at 60%. **Check Prerequisites** shows **Prerequisites met** for the dated CSC207H1 example and identifies the qualifying CSC148H1 alternative. |

Alternate results. **Requirement missing** names the unmet group for a known complete history. **Needs review** asks for a missing grade, history or equivalency fact, or shows a source condition the app cannot evaluate. **No prerequisite listed** reproduces the available source information. Keep a source note and the data date visible for all outcomes. Editing this panel does not apply or remove a timetable section.

## UC-08 — Check Walking Time Between Classes

| Before | Action and successful result |
| --- | --- |
| Two adjacent timed class blocks show their section names, times and buildings with a transition marker. | Select the transition or the class. The detail reads **AB to BR · 11 min walk · 1,096 m**. The marker and text identify a back-to-back walk longer than the 10-minute transition. |

Alternate results. A BA to MP fixture shows **3 min · 248 m** with normal styling. Exactly ten minutes shows the duration without a long-walk message. An unknown building shows **Walking time unavailable**. A confirmed same-building transfer uses a label. An actual overlap shows both meetings and time conflict details.

## UC-11 — Compare Generated Timetables

| Before | Action and successful result |
| --- | --- |
| **Course Plan** lists selected offerings, term preferences, section locks, personal blocks, and controls for **Generate Timetable**. The current timetable is visible separately. | Generate complete valid schedules. Compare **Option A** and **Option B** with weekly thumbnails and concise reasons, such as one fewer short gap or an earlier Tuesday. **Apply this timetable** updates the active grid after a final validity check. |

Alternate results. Show progress and a cancel action if generation takes time. A stopped search labels any valid results found so far. A proven conflict result identifies conflicting requirements and any allowed repair path. Unknown linkage or times state what the student should check. A single option remains usable. When inputs change, preserve the active plan and ask to regenerate or revalidate before applying a preview.

## UC-13 — Replace a Section That Filled Up

| Before | Action and successful result |
| --- | --- |
| The active plan shows a selected tutorial and a course data timestamp. The student knows TUT0201 filled in ACORN. | Select **Find Replacement** and **This section is unavailable**. Preview compatible TUT0202, show its availability and the one required change. **Apply replacement** updates the timetable. **Save as backup** preserves a separate plan. |

Alternate results. **Verify availability** distinguishes a time-compatible replacement from one with a recent open observation. An incompatible linked tutorial is skipped or explained in the diagnostics. If all compatible choices fail, explain why and keep the original plan. Closing a preview leaves the original choices intact.

## Supporting flows and feedback

| Screen or control | Before | Successful resulting state | Useful alternate state |
| --- | --- | --- | --- |
| Search and filters | A code or keyword and visible filter selections | Distinct matches with available section details | Empty query help, no matches, or load error |
| Offering preview, add and lock | Specific offering and section selected | Preview can be removed, Add makes a Preferred selection, Lock marks it explicitly | Duplicate add keeps one, cancellation or unresolved requirements have clear feedback |
| Weekly grid and term picker | A known plan with Fall/Winter choices | Selected term or both terms, visible meeting details, intentional supplied start times | TBA and vanished sections appear in a list, conflict blocks each stay visible |
| Block Off Time | Term, weekday, a start and end picker | Block appears in grid and is used by fit/generate | Invalid time shows field error and leaves old plan |
| Save or open | Active plan or file picker | Restored active plan and saved state indicator | Corrupt file or unknown version leaves original intact |
| Share link | Preview of fields included in the share | Receiver can inspect and apply without sender's file | Invalid link or unresolved section details are visible |
| Export PDF, PNG and print | Term and format selected | Content matches the weekly grid and labels remain readable | Cancellation or write/print failure leaves the plan open |

For each case, the click listener calls a controller. The interactor reads or updates the domain state and emits a result over its output boundary. The presenter updates view-model display data. The view redraws from the new model, moving focus or announcing a status when needed. Hold on to useful input, and show UI state changes on the Swing event thread. [Use-case setup steps](project-expectations.md#how-to-implement-a-use-case)
