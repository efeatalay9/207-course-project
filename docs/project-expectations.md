# CSC207 project expectations and rubric

Fall 2026. This reference records the course instructions and rubric criteria in searchable text. The project and lab pages were checked on November 24. Quercus publishes changes to the draft rubric and exact presentation times. [Source index](course-resources/README.md) · [Lab and course guidance](quercus-project-guide.md) · [Detailed project plan](project-plan.md)

## Project requirements

- Build a Java application with a Java Swing GUI. A team with a compelling reason can ask the course about JavaFX. The project expectations specify a desktop Java project.
- Document a focused user story per group member as the expected scale. Each member implements at least one complete story and takes the lead on at least one MVP use case. A story can involve more than one use case.
- Demonstrate pull requests and peer reviews throughout development. Write enough of your assigned use case for reviewers to see your contribution in GitHub. Start with your own assigned code and coordinate changes to shared code.
- Apply Clean Architecture and SOLID. Be ready to explain the dependency direction and design decisions. Apply design patterns where they solve a real problem and identify a meaningful extension point.
- Use an external API for a project need. A Java library wrapper is allowed. Document what service is used, its documentation, how it is used, and whether the Java call has succeeded.
- Demonstrate persistence. Saving and loading a local file, storage API, or database are permitted approaches. The course login example points to a storage API that could cover both API use and persistence for an appropriate project.
- Use labs to coordinate and make progress outside lab. Follow your team contract. Attend meetings, share progress and delays, and give every member room to implement and review. The course encourages TA feedback, Piazza, office hours, and discussion across teams.

[Project timeline](https://q.utoronto.ca/courses/444184/pages/project-timeline-2) · [Project overview and expectations](https://q.utoronto.ca/courses/444184/pages/project-expectations) · [Lab summary](https://q.utoronto.ca/courses/444184/files/46149081?wrap=1)

## Dates and deliverables

| Course milestone | Lab or due date | What the team should be able to show |
| --- | --- | --- |
| **1. Planning and initial development** | Nov 9 or 10 lab. Complete Fri Nov 13. | Shared repo, README blueprint with a named lead for each MVP use case, designed and implemented core entities, and initial UI and interactor work. Show the TA a sketch of the main UI elements. |
| **2. Individual progress and working prototype** | Nov 16 or 17 lab. Complete Fri Nov 20. | Each member submits a substantial PR for their work, reviews at least one teammate PR, and can run and discuss their work. The team can call the external APIs it uses and show at least one complete end-to-end use case. One member submits the repo URL to MarkUs `GroupProject`. |
| **3. Near-complete prototype** | Nov 23 or 24 lab. Complete Fri Nov 27. | At least one unit test per interactor, implemented MVP use cases integrated into the application, and a demo of the agreed prototype for TA feedback. |
| **4. Polish and presentation preparation** | Nov 30 or Dec 1 lab. Complete Fri Dec 4. | 100% line coverage for the Entity and Use Case layers, outstanding bugs addressed, and a polished, rehearsed presentation. |
| **Final presentations** | Dec 7 or 8 labs. Exact slots announced by course staff. | Explain and demonstrate the MVP, give every member a speaking role, and answer questions about functionality, design, and implementation. |
| **Individual submission** | End of Tue Dec 8 | Submit a reflection of up to one page and complete peer evaluations on MarkUs. |
| **Clean Architecture Reflection Quiz** | Tue Dec 8, 11:59 p.m. | Complete the separate Quercus quiz. The course encourages trying CAVE first. |

[Timeline and lab details](quercus-project-guide.md) · [Presentation expectations](https://q.utoronto.ca/courses/444184/pages/final-project-presentation-expectations) · [Clean Architecture Reflection Quiz](https://q.utoronto.ca/courses/444184/quizzes/519344)

## README, user stories, and specification

The optional course README template lists the information the team should make available. Include the team members and project name, domain and short description, focused user stories, tested use cases and their owners, the agreed MVP, entities with variables and relevant methods, API information and call status, and available design diagrams. Keep run instructions and development notes useful as the app grows. The course suggests recording relevant README updates during PRs.

In Lab 4 the course asks for a short list of initial user stories in the team repo, where the team contract was placed. A short specification like the Todo List example is optional at that initial stage. Teams can change direction early and are expected to agree on a direction and stories within the two-week planning window described in the instructions. A course team member must approve the chosen project scope. The Week 5 brainstorm list includes Course Scheduler. [Project scope instructions](https://q.utoronto.ca/courses/444184/pages/project-domains-and-sample-user-stories) · [Week 5 ideas](https://q.utoronto.ca/courses/444184/pages/project-ideas-from-week-5-user-stories-shared-in-class)

Propose one focused student goal per leading story. For each use case, document its name, primary actor, preconditions, trigger, success outcome, numbered main scenario, and extensions at the relevant steps. Scope the MVP around useful end-to-end paths. The course encourages early sketches of before and after views and saving sequence and class diagram sources, with PlantUML suggested. Show at least two entities and give each a clear role in the domain. Explore APIs early. A Java proof of concept and a fixture can help demonstrate the design while integration is in progress.

[README and MVP](https://q.utoronto.ca/courses/444184/pages/project-readme-and-minimum-viable-product-mvp) · [Developing user stories](https://q.utoronto.ca/courses/444184/pages/developing-project-user-stories) · [README template](https://q.utoronto.ca/courses/444184/files/45644698?wrap=1) · [Course notes and API resources](course-resources/README.md#course-and-reference-links)

## How to implement a use case

Complete the use-case specification first. The supplied guide recommends the following sequence and encourages iteration when a test or view reveals a missing value.

1. Sketch before and after views when planning how the user interacts with the case. Lab 7 specifically asks the team to show the TA a sketch of the main UI elements.
2. Identify the entities the case operates on. Add only the entities needed.
3. Specify input data from the actor and output data that drives the result.
4. Define any needed data access in the use-case layer's data access interface.
5. Start with a simple data access implementation or fake, such as a map.
6. Write interactor tests for success, failures, and important boundaries. A test presenter can record output so the test can check that the correct callback occurred after execution.
7. Implement the input boundary and interactor. The guide recommends `execute` where it fits. Get the interactor tests passing before finishing the UI.
8. Connect the view action to its controller. The controller creates the input data and calls the boundary.
9. Implement the presenter. It receives output data through the output boundary, updates the view model, and makes the change visible to the view.
10. Update the view to react to the result and verify the full interaction in the running app.

The use-case lead owns most of the interactor and related interfaces, the case-specific controller and presenter, the UI action, and the resulting UI update. Coordinate shared views with other leads. Consult the course signup, login, and logout examples for the full flow. [Course implementation guide](https://q.utoronto.ca/courses/444184/pages/how-to-implement-a-use-case) · [Draft timetable UI sketches](ui-sketches.md)

## Style and Checkstyle

**For Excellent or Exceptional in Code Quality, the November 15 draft requires evidence that the team used Checkstyle or a similar tool.** The same rubric allows existing style issues when there is clear evidence of use. The lab summary also identifies style and Javadoc as code-review topics. Correctness, tests and coverage, Clean Architecture and SOLID, and the clarity of PR information also belong in a review. Give specific, actionable feedback. [Draft rubric](https://q.utoronto.ca/courses/444184/files/46148571?wrap=1) · [Lab summary](https://q.utoronto.ca/courses/444184/files/46149081?wrap=1)

The course notes use Google Java Style, Checkstyle 10.26.1, and a shared Google checks configuration for Maven and CheckStyle-IDEA. The notes explain class and method Javadoc and recommend consistent formatting. The team repo's Maven Checkstyle goal uses the same rule set. Run it on the changed files, and use the testing guide's JaCoCo command to reproduce the coverage report. [Team checklist and review examples](code-review.md) · [Style and coverage evidence](testing.md#style-and-coverage-work) · [Course style guide](https://github.com/CSC207-UofT/207-course-notes/blob/HEAD/code-style-and-documentation.md)

For presentation evidence, show a representative review, the style report or fixes prompted by it, and the coverage result for the two inner layers. An IDE formatter is useful for formatting. A Checkstyle run supplies analysis evidence, and the JUnit and coverage runs supply behaviour and coverage evidence.

## Design pattern expectation

The Week 10 slides call for an implemented pattern outside the six course-notes examples for full marks on the pattern part of the presentation. Those six examples are Observer, Strategy, Factory, Builder, Adapter and Façade. The November 15 rubric asks for an appropriate pattern beyond the starter code at Excellent and adds significant, appropriate use of a pattern beyond those presented in class at Exceptional. Show a real problem, the actual collaborating classes, and the resulting benefit. Memento for preserving and restoring the original timetable is proposed in the plan and appears in the Week 10 practice activity. Confirm the desired band with the TA. [Week 10 slides, pp. 36–45](https://q.utoronto.ca/courses/444184/files/46220936?wrap=1) · [Draft rubric](https://q.utoronto.ca/courses/444184/files/46148571?wrap=1) · [Project design proposal](project-plan.md#design-choice-to-show-in-the-presentation)

## Group presentation rubric

The lab summary gives a **100-point group rubric** for the project presentation, which accounts for **20% of the course grade** before the individual scale factor. The table restates the detailed November 15 draft's bands. [Lab summary](https://q.utoronto.ca/courses/444184/files/46149081?wrap=1) · [November 15 draft rubric](https://q.utoronto.ca/courses/444184/files/46148571?wrap=1)

| Criterion | Points | Inadequate ≤1/5 | Acceptable 2/5 | Good 3/5 | Excellent 4/5 | Exceptional 5/5 |
| --- | ---: | --- | --- | --- | --- | --- |
| Presentation materials | 15 | Many required elements missing. | Most elements appear, some thin or missing. Significant issues with polish, readability, or errors. | All required elements and readable slides, with minor errors or specific visual or proofreading improvements. | Complete, polished slides with effective diagrams, screenshots, or other visuals throughout. | Professional slides and visuals that clarify complex ideas, showing effort beyond expectations. |
| Presentation delivery | 15 | Hard to follow, with little rehearsal. | Limited rehearsal, frequent pauses or reading, or unclear explanations. | Prepared and mostly clear, with some uneven pacing, rough transitions, or reading. | Clear, smooth explanations. **Within 15 minutes and every member speaks** for this band or higher. | Confident and engaging group delivery with seamless transitions. |
| Demo | 10 | No working demo shown. | Partial use-case coverage or significant delays or failures with little explanation. | Most use cases work, with minor hiccups and mostly clear explanation. | Smooth end-to-end use cases, prepared data and setup, and clear explanation of each step. | Seamless demonstration of each member's user story linked to use cases and design shown in the slides. |
| Question answering | 5 | Unable to answer project questions. | Answers some questions. | Answers most questions with few obvious errors. | Answers all questions with no obvious errors. | Accurate, concise answers demonstrate deep knowledge of the project and course. |
| Specification | 10 | Incomplete, hard to understand, and includes implementation details. | Somewhat clear, still emphasizes aspects of implementation. | Mostly clear and mostly focused on observable behaviour, with some implementation emphasis. | Clear, readable description of what the software does. | Exceptionally clear behaviour and purpose, including for an unfamiliar non-programmer. |
| Project scope | 5 | Fewer stories than members and insufficient scope. | At least one story per member, with minimal ambition or significant overlap. | Significant stories for every member with some overlap. | Appropriately ambitious, significant stories with little overlap. | Distinct, substantial stories and scope clearly beyond expectations. |
| API usage | 5 | No API used. | Minimal or poorly motivated API use. | Actual API use and a brief explanation with missing details. | Actual API use with specific explanation in the slides. | Significant, appropriate API use with a clear explanation of how it supports the project. |
| Data persistence | 5 | No persistence. | An attempt without implementation or clear evidence. | At least partial persistence and minimal evidence. | Implemented persistence with presentation evidence. | Clearly demonstrate both saving and loading. |
| Clean Architecture | 10 | Few attempts and clear Dependency Rule violations. | Partial adherence with significant violations and insufficient explanation or diagrams. | Largely follows the architecture with at least one appropriate diagram that gives evidence. | Almost complete adherence, strong diagrams, and discussion and justification of violations. | Dependency Rule adherence evident, convincing diagrams, and discussion and justification of any minor violations. |
| Overall design | 10 | Little design discussion. | Limited discussion of at least one SOLID principle and an attempt at a pattern or extensibility example. | Rich discussion of at least one SOLID principle and either a clear implemented pattern beyond the starter or a clear extension example. | Rich discussion of at least two SOLID principles, an appropriate implemented pattern beyond the starter, and a clear extensibility example. | Exceptional explanation of the Excellent elements, including significant appropriate use of a pattern beyond those presented in class. |
| Code quality | 10 | Little evidence of team effort. | Some effort with an unclear or inadequate process. | Reasonable effort to ensure quality on main, with some details unclear. | Clear significant effort toward quality on main. Evidence of Checkstyle or a similar tool is required. | Excellent-level effort with a particularly clear account of how quality was assured, for example a representative PR. |
| **Total** | **100** | | | | | |

## Presentation and demonstration

- Prepare about 15 minutes plus up to 5 minutes of questions. The detailed delivery rubric ties Excellent and above to staying within 15 minutes and having everyone speak. Indicate the speaker on each slide and rehearse hand-offs.
- State the product and user stories briefly, about one minute in the supplied template. Give about 30 seconds each to API use and persistence. Show what the API returns or enables, and demonstrate persistence during the demo or separately.
- Plan about six minutes for design using the template as a guide. Include two concrete SOLID examples, an appropriate pattern the team introduced, and a real extension point. The suggested one to two minute use-case walkthrough can show the user story, before and after views, a UML diagram, the interactor, and the runtime flow. Use that explanation to support the Clean Architecture assessment.
- Rehearse about six minutes of core functionality, with the dataset and setup ready. Use clear labels and narrate the meaningful steps. Live or recorded demonstrations are acceptable in the template guidance. Have the working app available for live follow-up.
- Reserve about a minute for quality evidence such as Checkstyle or similar runs and a representative peer-reviewed PR. Make slide visuals legible and useful to a second grader seeing the app for the first time. Prepare all members to answer questions concisely and accurately.

[Presentation instructions](https://q.utoronto.ca/courses/444184/pages/final-project-presentation-expectations) · [Slide template](https://q.utoronto.ca/courses/444184/files/46149397?wrap=1)

## Individual contribution and scale factor

The group project score is multiplied by the member's individual factor. Most students are expected to receive 1. Exceptional adjustments above 1 are at course staff discretion. The supplied guide starts at 1, subtracts at most one group-issue deduction and one code-contribution deduction, and floors the factor at zero.

| Deduction | Category and example |
| --- | --- |
| −0.05 | Minor group issue. Examples include several unexplained absences, low lab engagement, missing PR reviews, or a noticeably less prepared presentation share. |
| −0.10 | Major group issue. Examples include repeated failure to follow the team contract, consistently low engagement or attendance, or missed deadlines that affect the team. |
| −0.20 | Significant code contribution concern. Examples include only small or documentation-only merged contributions in your own use case, or doing teammates' work or rewriting it without discussion to the point that collaboration breaks down. |
| −1.00 | No significant repository code contribution. The course gives examples such as no merged PRs or no meaningful progress toward the assigned use case. |

| Applied deduction | Factor | If team earns 85% | If team earns 70% |
| --- | ---: | ---: | ---: |
| None | 1.00 | 85.0% | 70.0% |
| Minor group issue | 0.95 | 80.75% | 66.5% |
| Major group issue | 0.90 | 76.5% | 63.0% |
| Code concern | 0.80 | 68.0% | 56.0% |
| Code concern and minor group issue | 0.75 | 63.75% | 52.5% |
| Code concern and major group issue | 0.70 | 59.5% | 49.0% |
| No significant code contribution | 0.00 | 0% | 0% |

Aim for roughly balanced contributions and explain meaningful differences when appropriate. Keep one or more substantial PRs related to your own story merged on main. Review other members' PRs. Shared team duties such as meeting coordination, progress updates, presentation preparation, and documentation also matter. Respect team agreements and raise unresolved issues with course staff early. Submit the one-page-or-shorter reflection and peer evaluations on **MarkUs by the end of Tuesday, December 8**. The reflection can cite specific PRs if the graders are considering a deduction. [Individual instructions](https://q.utoronto.ca/courses/444184/pages/project-individual-scale-factor) · [Lab summary](https://q.utoronto.ca/courses/444184/files/46149081?wrap=1)
