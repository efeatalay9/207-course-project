# Timetable data and regression reference

The [September 5, 2026 Timetable Builder archive](../../uoft-ttb-raw-catalogue-2026-09-05.zip) is available for offline work and prerequisite checks. The files here support the CSC207 timetable and walking use cases.

| File | Contents and use |
| --- | --- |
| [walking.json](walking.json) | Routed campus-building distance and estimated walking time. Index it once in a small Java adapter. |
| [catalogue-edge-cases.json](catalogue-edge-cases.json) | Curated source evidence for 79 offerings, grouped by identity/topics, component links, dates and delivery, availability, academic rules, administrative obligations, and credits. Includes the raw record fields, page references, and divisional legends/indicators. |
| [catalogue-audit-counts.json](catalogue-audit-counts.json) | Whole-corpus scan counts and pattern categories. A matched text pattern is a candidate for inspection. |
| [prerequisite prototype](../prerequisites/README.md) | Generated course-level rule file, reproducible compiler, and Java test harness. |

## Catalogue capture and joining data

The public official builder returned 409 pages on September 5, 2026, between 05:03 and 05:09 Toronto time. It contained **8,172 offerings, 7,094 distinct codes, 23,733 sections, and 26,548 meeting records** across eight divisions. IDs were unique in that capture. Fall 2026, Winter 2027, and full-year were advertised. Supporting Summer is still part of the product when a catalogue contains it.

| Read from the raw catalogue | Meaning in the app |
| --- | --- |
| Offering `id`, `code`, `sectionCode`, `sessions`, `campus`, `faculty` | Use `id` for identity. The course-level `sectionCode` identifies an offering variant and can have numbered forms such as F2. Use actual sessions and campus fields. One course code can have multiple offerings and topics. |
| `cmCourseInfo` and `notes` | Display published text for description, prerequisites, corequisites, exclusions, preparation, and requirements. A note can modify a rule for a particular class, term, program, or year. |
| `minCredit`, `maxCredit`, published breadth and distribution values | Preserve the source amounts/categories and allow an observed credit drop-down. Course letters are insufficient to infer all credit values. |
| Section `name`, `teachMethod`, `sectionNumber`, subtitle/topic and `linkedMeetingSections` | Identifier within an offering, component type, the exact IDs selected by linkage, and a possible academically different topic. |
| `primaryTeachMethod`, `openLimitInd`, `cancelInd`, `currentEnrolment`, `maxEnrolment`, `currentWaitlist`, `waitlistInd` | Distinguish the primary section, registration closure, cancellation, reported capacity, and waitlist support. Enrolment group controls can change who may use an opening. |
| `meetingTimes`, `start.day`, `millisofday`, `sessionCode`, `repetition`, `repetitionTime`, `tbaInd`, delivery mode | Keep the valid timed meetings and express uncertainty about incomplete times and occurrence dates. Monday=1, Sunday=7. Use full days and Toronto local time. |
| `meetingTimes[].building.buildingCode` and map link | Join on actual campus plus building code. Keep the code separate from room display text. Unknown building codes and absent routes are legitimate states. |

Continue to use `offeringId + sectionName` in the current Java model because raw section names are unique within each selected offering. When an archived selection cannot resolve, display its saved identity and allow correction. A fallback offering identity needs sessions, division or campus, course code, and the exact variant. Match exact teaching method and section number for published linked references.

## Walking data you can use directly

The walking file covers every within-campus pair of buildings in the September timetable archive. The **official U of T campus map's Concept3D network** supplied the original 2,215 walking estimates on September 6, 2026. The other 66 pairs involve **`St. George:UY`**, 700 University Avenue, which appears in SOC202H1 TUT0201. The official map lists the building. On November 24, its route engine returned an out-of-range response for test routes in both directions, including a test from the mapped entrance. [Official U of T map](https://map.utoronto.ca/?id=1809#!m/497527) · [U of T Sociology address](https://www.sociology.utoronto.ca/about-us/our-department/contact-us)

The 66 UY routes were collected on November 24, 2026 using FOSSGIS's OSRM pedestrian router. They start or end at the mapped ground-level University Avenue entrance of 700 University Avenue. The other endpoints use official campus map pins, including current pins matched by campus and building code where an archived link had moved. Each pair was routed in both directions. The saved row uses the slower direction and the distance for that route. The router matched the UY entrance directly and snapped every destination to a pedestrian path within 35 metres of its pin. © OpenStreetMap contributors. [Mapped entrance](https://www.openstreetmap.org/node/13465111272) · [FOSSGIS router](https://routing.openstreetmap.de/about.html) · [Open Database License](https://opendatacommons.org/licenses/odbl/1-0/) · [Improve the map](https://www.openstreetmap.org/fixthemap)

The file keeps `schemaVersion: 1`, `profiles: ["walking"]`, and a source notice. Each route has `from`, `to`, `minutes`, `distanceMeters`, `source`, `observedAt`, and `personal`. Display distances in metres and durations rounded up to whole minutes. All shipped rows have `personal: false`.

| Campus key prefix | Source buildings | Published routes | Expected within-campus pairs |
| --- | ---: | ---: | ---: |
| `St. George` | 67 | 2,211 | 2,211 |
| `Scarborough` | 11 | 55 | 55 |
| `University of Toronto at Mississauga` | 6 | 15 | 15 |
| Total | 84 | 2,281 | 2,281 |

```text
St. George:BA  ↔ St. George:MP    3 minutes,   248 metres
St. George:AB  ↔ St. George:BR   11 minutes, 1,096 metres
St. George:BA  ↔ St. George:UY   10 minutes,   737 metres
St. George:PB  ↔ St. George:UY    3 minutes,   201 metres
Scarborough:AA ↔ Scarborough:AC   1 minute,     55 metres
```

Retain a single row per unordered pair and index it in both directions or under a sorted pair key. These are building-level estimates. Room-to-room distance and indoor travel time are outside their scope. Keep walks unknown or inapplicable for cross-campus trips, buildings outside the archive, unknown locations, and meetings with no physical location.

## Representative catalogue cases for tests

These are examples from the captured data. A course's current offering may have changed. Use the included evidence or the existing raw archive to build small test fixtures, and express ambiguity as a data result in the Java app.

| Example or source condition | Why it matters |
| --- | --- |
| ARC451H1 numbered Fall variations and ENG378H1 different subtitles | The course code and semester may describe several offerings or substantively different topics. Preserve the selected topic during generation or repair. |
| PEY400Y1 term label and its actual session array | Duration and term can diverge from the course's letter or visible label. Use `sessions`. |
| CHM327H1 and MGTA38H3 | A lecture's linked section number need not match a simple suffix rule. Use raw associations. |
| PHY241H5 | A practical can be linked directly to a tutorial. Check relationships across all required component types. |
| BIO203H5 | The source contains contradictory practical/tutorial evidence. Accept a validated match or report uncertain data. |
| FSTA03H3, STAC50H3, and BIO220H1 | A mandatory tutorial missing from the feed, a tutorial required only if offered, and optional tutorial attendance produce different results. |
| CIV498H1 and other PRA-primary offerings | A practical can be the primary component. Retain TUT-primary as well. |
| WGS299Y5 | A known meeting in Fall can coexist with an unresolved Winter schedule. |
| PHY294H1, CME270H1, and alternate-week schedules | Manual recurrence or alternate-week patterns may contain a time but no reliable actual dates. |
| Duplicate meeting rows, simultaneous locations, assessments and off-campus venues | A duplicated record cannot create a self-conflict. Preserve distinct activity/delivery states. Show multiple possible locations and conditional attendance as unresolved when needed. |
| Closed registration, cancelled sections, zero availability, waitlist, and enrolment group denials | Keep separate fields and retain the divisional legends and effective dates. Display reported availability separately from the student's enrolment eligibility. |
| BIOB97H3 winter site, Sheridan College, Centennial College, and Off Campus | Catalogue suffix is insufficient to infer a physical destination or travel time. |
| CSC207H5, MATA22H3, and courses with related exclusions | Minimum-grade thresholds, specific campus requirements, directionality, and waiver/equivalence need exact treatment. |
| Courses with alternate eligibility dates, admissions, aggregate credit or grade rules, and topic limits | Preserve clauses and their effective dates. Use the static predicate language for supported rules and review other conditions. |
| Variable credits and unusual course codes | Preserve what the source publishes. Broader prerequisites and filters cannot assume only conventional half/full-year codes. |

The raw data contains rich notes and explicit structured fields. A shared Java parser should keep source values distinct, test small focused cases, and use the text as review evidence for conditions the app cannot confirm. The [implementation guide](../../docs/implementation-guide.md) gives the Java-facing behaviour.
