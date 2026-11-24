# CSC207 lecture slides for the project

These are the project-relevant takeaways from the Week 7–10 slides and the Lab 8 review slides. Source links identify the exact decks. Print page numbers below are PDF page indices. The repo contains the guidance in text. Original course files from this review are preserved with the private review materials.

## Week 7 — Clean Architecture, part 1

- **Pages 3–11.** SOLID and layering help keep a change local. A policy is a rule about what the app does, such as protecting a selected lecture. A detail is how it is done, such as storing the selection in JSON or displaying it in Swing. Code that decides which section combination is possible should work even if the UI or storage mechanism changes.
- **Pages 12–27.** Describe use cases from the user's point of view. Specify inputs, successful outputs, and the policy used to produce them. An entity groups durable domain information and its invariants, such as a meeting time and its valid interval. The application action applies those rules to a specific request, such as finding a section that fits the student's timetable.
- **Pages 28–45.** Imports and type references go inward. A use case can ask for catalogue or output operations over interfaces it owns. An outside adapter implements those interfaces. The caller running before or after another object at runtime does not determine the dependency direction. In the timetable project, the interactor should never need to import a Swing panel or concrete file reader.
- **Pages 46–56.** The account signup example traces a view action through a controller, input data, interactor, storage and output interface, then presenter and view model. The data-access interface and output interface give the interactor small substitutable dependencies. The design helps isolate changes and makes business rules testable without UI and storage setup.

[Clean Architecture, Week 7](https://q.utoronto.ca/courses/444184/files/45645700?wrap=1)

## Week 8 — Wiring and testing Clean Architecture

- **Pages 9–17.** A request object carries what the user action needs. A response is a new description of what happened. Entities have their own lifetimes and behaviour. An interactor can call storage before or after applying rules. Report results through the output boundary and let the presenter do display formatting.
- **Pages 18–26.** Build the shared objects in the outer startup code. Main or a small AppBuilder creates the repositories, presenters, interactors, controllers and views in dependency order and links callbacks. Reuse the same active timetable state when several cases read or update it. Give the views the correct controllers and view models.
- **Pages 27–38.** An entity test checks an actual domain object. A use-case unit test checks the real interactor against a fake data gateway and presenter. An integration test uses the real implementation of the adapter or combines actual components. A system test connects the app entry flow to its final output. The project can test the Swing path manually and use its unit and integration suite for repeatability.
- **Pages 39–50.** Arrange the fixture and injected objects, execute the interactor, then check the result and state. Have a recording presenter so a test detects a missing result callback. Include ordinary success and important failure paths, for example an already-used account name in the sample or an already-added or cancelled section in this project.
- **Page 51.** Define the course MVP from user stories. Each team member owns a meaningful case that can be run and explained. Plan the UI and boundaries so these cases join one application.

[Clean Architecture, Week 8](https://q.utoronto.ca/courses/444184/files/45915260?wrap=1) · [Course storage and testing examples](https://q.utoronto.ca/courses/444184/pages/sample-code-for-external-data-storage-and-testing-examples)

## Week 9 — Design patterns, part 1

- **Pages 3–9.** Design patterns describe solutions to recurring design problems. They can improve cohesion and reduce unnecessary coupling. Choose a pattern once you can name the actual problem and the alternative the pattern improves on.
- **Pages 10–18.** Factory chooses which concrete object to create behind a simpler creation call. Builder gradually supplies the parts of a complex object or wiring setup. Ordinary constructors are sufficient for simple Java values.
- **Pages 19–23.** Strategy lets a client delegate interchangeable behaviour through one contract. A ranking comparator is a natural place for it if the project gains different ranking policies. A stable single policy can start as a normal method.
- **Pages 24–33.** Observer lets interested objects listen for a change without the source knowing all the observers. Notification order should not be relied on. Prefer delegation or a supported listener API such as `PropertyChangeSupport` for Swing view-model updates.
- **Pages 34–44.** Adapter translates an interface to the format its consumer expects. A timetable JSON adapter can map the institution's fields to our Java catalogue types. Façade offers a simpler entry point to a subsystem. Prefer delegation where inheritance would force a false subclass relationship.
- **Pages 45–47.** Identify pattern roles in the actual project code and explain what each participant does. A class name or diagram alone does not establish an implemented pattern.

[Design Patterns, Week 9](https://q.utoronto.ca/courses/444184/files/46065060?wrap=1) · [Course notes on patterns](../course-notes-summary.md#chapter-12--patterns)

## Week 10 — Refactoring and patterns beyond the six examples

- **Pages 5–23.** Refactoring preserves observed behaviour. The slides demonstrate extracting methods, changing declarations, encapsulating fields, splitting loops, moving statements, and simplifying construction with a builder or factory method where there is a clear need. Make small steps and use tests to check each change.
- **Pages 24–31.** To replace a multi-branch implementation of interchangeable behaviour, isolate one proven operation, extract its contract, move the alternatives behind it, and separate selection from execution. The lecture example arrives at a Strategy and simple Factory. For the planner, the same approach can separate committed scheduling policy changes from a structural refactor.
- **Page 36.** The lecture guidance for full design-pattern marks calls for an **implemented pattern beyond the six course-notes patterns** (Observer, Strategy, Factory, Builder, Adapter, Façade) that solves a real problem. The November 15 rubric distinguishes a pattern beyond the starter at Excellent and significant appropriate use of a pattern beyond those presented in class at Exceptional. Use both descriptions when planning the evidence and discuss the intended pattern with the TA. [Detailed design criterion](../project-expectations.md#design-pattern-expectation)
- **Pages 37–45.** The exercise introduces Command (actions as objects), Memento (save and restore an object's state through a checkpoint), Template Method (reused fixed steps with changeable details), and Decorator (wrap a component to add optional behaviour). For the team's own candidate, identify the problem, how the pattern solves it, and which classes fill the roles. Memento is a low-effort proposal for preserving the original timetable across a chosen replacement or generation result. It appears in the lecture exercise, so verify the grading fit before counting it toward the Exceptional criterion.
- **Pages 46–52.** Practice finding actual patterns in the example repository and preview using regular expressions to recognize text. Keep course prerequisite rule grouping and grade evaluation in a typed Java rule representation. Recognizing a course-code token is one smaller step.

[Design Patterns, Week 10](https://q.utoronto.ca/courses/444184/files/46220936?wrap=1) · [November 15 draft rubric](https://q.utoronto.ca/courses/444184/files/46148571?wrap=1)

## Lab 8 — Code review and contribution evidence

The lab summary asks reviewers to look at correctness, tests, coverage, style, design, Javadoc and PR clarity. Comments should identify a specific problem and suggest how to address it. Track both substantial code contributions and reviews. Aim for reasonably balanced work and be able to explain a meaningful imbalance. The team-specific steps, example comments and PR description are in the code-review guide. [Lab summary, pp. 8–10](https://q.utoronto.ca/courses/444184/files/46149081?wrap=1) · [How the timetable team reviews code](../code-review.md)
