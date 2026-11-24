# CSC207 course notes for the timetable team

This standalone summary covers the official notes' numbered chapters 0–14, setup, style, API and file I/O, developer roles, and embedded ethics. The notes revision checked on November 24 is `8dbb627`. The chapter descriptions state the course concepts. Timetable examples are team applications of those concepts. [Official notes revision](https://github.com/CSC207-UofT/207-course-notes/tree/8dbb627ee1e099ebbeefe52feec1d13b6459b74a) · [Course requirements](project-expectations.md) · [Timetable implementation](implementation-guide.md)

## Setup and working knowledge

### Chapter 0 — Git, reviews, and collaboration

Git tracks snapshots with a history of commits. The working tree is the current files, the index is what the next commit would include, and local and remote branches can differ. Inspect status and diffs before staging, after switching branches, and before asking for review. Fetch and compare before merging teammates' work. Work on a feature branch, make focused commits, and use a GitHub pull request to discuss and review a proposed merge. Resolve merge conflicts by understanding both behaviours and re-running tests. Coordinate before rewriting shared history and use a revert to undo an already-published change when appropriate.

For this project, give each story a tracked scope, use the shared Maven tests, state the behaviour changes and fixture in the PR, and keep authorship and review evidence. Use the actual team contract to decide how many approvals to ask for. When two people touch the same view, agree on the model and boundary first. [Team code-review guide](code-review.md) · [Chapter 0](https://github.com/CSC207-UofT/207-course-notes/blob/main/00-introduction-to-git.md)

### Chapter 1 — Java programs and basic types

The compiler translates source to bytecode and the JVM runs it. Declare the types of variables, returns, and parameters. Use primitive numbers and booleans for simple values and classes for references such as strings, arrays, and application objects. A primitive assignment copies the value. A reference assignment can give two names to the same mutable object. Method arguments are passed by value, including the copied reference value. A shallow copy of a collection can still share its elements.

Use `equals` for string contents, remember strings are immutable, and use `StringBuilder` when building a long text incrementally. A Java array has a fixed length and element type. Multidimensional arrays are arrays of arrays and may have unequal row lengths. Choose a loop and condition that matches the problem, use a for-each loop for simple traversal, and preserve boundary checks. Instantiate classes for independent state and use class methods where instance state is unnecessary. For timetable logic, start with explicit names and types and plain loops. Take an immutable snapshot of selections when running a search so changes in the UI cannot change its inputs halfway through. [Chapter 1](https://github.com/CSC207-UofT/207-course-notes/blob/main/01-introduction-to-java.md)

### Chapter 2 — Classes, constructors, state, and equality

Give a class one purpose with fields and methods that model its state and behaviour. Instance fields belong to individual objects. Static members are shared at the class level. Constructors establish valid initial state, including default values and which superclass constructor runs. Access modifiers control who can read or change members. A final reference cannot be assigned again, while the object it points to can still be mutable. Use field encapsulation to make a valid state easy to keep.

Overloading provides variants under a method name and different parameter lists. Overriding changes the implementation of an inherited method on a subtype. Use `@Override` for clarity and compiler checking. Implement `equals` and `hashCode` consistently when putting value objects into hash-based sets or maps. Make `toString` readable for debugging while keeping the student-facing description in a presenter. Timetable equality should use stable selection identity such as offering plus section. Two records that display similarly can represent different offerings. [Chapter 2](https://github.com/CSC207-UofT/207-course-notes/blob/main/02-classes-in-java.md)

### Chapter 3 — Relationships, polymorphism, ordering, and UML

Use interfaces for capabilities multiple implementations can fulfill and abstract classes when related subclasses genuinely share an abstraction and implementation. Understand subtype substitution, method dispatch, constructors and `super`, and the risk of casts. A reference's declared type controls which methods callers can invoke, while overriding dispatches to the actual implementation. Use an explicit `Comparator` for task-specific ordering. `Comparable` describes an obvious natural order inherent to a type.

Read UML class boxes as names, attributes, and operations. Pay attention to ownership, visibility, multiplicities, associations, generalization, realization, and dependencies. Put stable relationships in the class diagram. For the timetable team, a catalogue implements lookup ports, an offering has many sections, and a section can have multiple meetings. The generator can compare schedules in several meaningful ways without building them into the entity's natural order. Use PlantUML when a checked-in diagram source makes changes easier to review. [Chapter 3](https://github.com/CSC207-UofT/207-course-notes/blob/main/03-relationships-between-classes.md)

### Chapter 4 — Swing and event-driven views

Build the window from an outer frame and panels. Make layouts explicit using Swing layout managers and standard components. Attach action listeners so controls respond to user actions. Read input, call the appropriate controller, and display results. Keep view setup and display changes on the Swing event thread. Keep slow HTTP/file work and potentially expensive loops outside the event thread, with a safe way to deliver the result back to the UI. The course examples separate testable behaviour from layout and visual checks.

For the timetable use fields, lists, tables, date or term choices, and ordinary buttons with meaningful names. Keep tests for click-independent decisions in the use case. A manual UI check covers rendering, keyboard navigation, and empty and error states. [Chapter 4](https://github.com/CSC207-UofT/207-course-notes/blob/main/04-GUIs-with-swing.md)

### Chapter 5 — Java subtleties

A local parameter can shadow a field. Use a clear assignment or `this.field` in constructors. Copy arrays deliberately. Distinguish equality by number or contents from reference identity, especially with boxed values and the small-integer cache. Unboxing `null` throws at runtime. Catalogue values such as unknown availability or grades require a deliberate representation before comparison. Zero and missing have different meanings. [Chapter 5](https://github.com/CSC207-UofT/207-course-notes/blob/main/05-java-gotchas-and-subtleties.md)

### Chapter 6 — Generics

A type parameter preserves useful compile-time information in a reusable class or method. A bound restricts the allowed types and tells the implementation which operations they support. Standard generic collections are enough for most team work. Use e.g. `List<Section>` and `Map<String, CourseOffering>` instead of raw collections. Introduce a custom generic class when the type parameter solves an observed repeated problem. [Chapter 6](https://github.com/CSC207-UofT/207-course-notes/blob/main/06-generics.md)

### Chapter 7 — Collections

Program with `List`, `Set`, and `Map` interfaces when callers do not need a particular implementation. Lists keep ordered sequences and can contain repeated values, sets track membership, and maps retrieve values by key. Implementation choices determine order and operation costs. An array-backed list is a straightforward choice for ordered traversal and a hash map works for exact-ID lookup. A linked hash set or map gives deterministic insertion order when that is part of the product.

Use immutable copies for results and protect a collection while it is shared across callers. Maintain the ordering invariant for the existing sorted course-code prefix index. Use ordinary filters and maps for a few thousand offerings. Distinguish the real ordering of results from the order in which they were found. [Chapter 7](https://github.com/CSC207-UofT/207-course-notes/blob/main/07-collections.md)

## Specification, design, and architecture

### Chapter 8 — Specification, user stories, use cases, and sequence diagrams

A specification describes what a user or other outside actor can accomplish and observe. Use nouns and verbs in the domain to propose classes, data and operations, then refine them as evidence improves. A scenario walk-through reveals missing information, duplicated responsibilities, or steps with no owner. A story should express actor, goal, and benefit. A useful story is a user-visible outcome of reasonable scope, such as replacing a filled tutorial while preserving the rest of a plan.

Record each use case's actor, preconditions, trigger, success outcome, main steps and extensions tied to specific steps. Include failure and no-op results the user can observe. A sequence diagram adds runtime lifelines and calls between objects. For each message, identify the operation on the receiving class, what data travels, and whether the object already exists or gets created by the message. Draw important alternate flows as separate cases or clearly marked branches. Use the class and sequence diagrams together to find missing operations before coding the view. [Chapter 8](https://github.com/CSC207-UofT/207-course-notes/blob/main/08-program-specification.md) · [Timetable use cases](project-plan.md#lead-use-cases)

### Chapter 9 — SOLID and design judgement

| Principle | Meaning | Timetable example |
| --- | --- | --- |
| Single Responsibility | A class should have a coherent purpose and a clear reason to change. | A meeting overlap routine knows intervals. A Swing grid knows layout. |
| Open/Closed | Useful extension points allow additions with small changes to stable code. | A different route source can implement the existing lookup interface, leaving travel rules unchanged. |
| Liskov Substitution | Implementations of a type must honor the public contract and remain substitutable for callers. | A fake catalogue and loaded catalogue use the same absent-result and failure semantics. |
| Interface Segregation | Callers depend only on operations they need. | Autocomplete needs a code index, while timetable rendering needs exact offering lookup. |
| Dependency Inversion | Core policy declares the abstractions it needs. Outer details implement them. | The prerequisite interactor declares the rule lookup and output ports, with Gson and Swing in the outer layers. |

Review class size and naming in context. A change that reduces duplication can still hide important domain rules if it bundles unrelated responsibilities. Explain principles with actual code and decisions. [Chapter 9](https://github.com/CSC207-UofT/207-course-notes/blob/main/09-design-principles.md)

### Chapter 10 — Layers, MVC, and MVP

An event-driven program is a network of objects. Layering groups similar responsibilities and helps control coupling and knowledge of implementation details. MVC splits data or model state from presentation and user input handling. MVP gives a presenter a clearer role in coordinating display state, with a view that can focus on what the user can see and interact with. Architectures vary by which objects directly know each other. Decide from the dependency diagram, not the name of the pattern. A fine-grained architecture can isolate core rules from UI and storage choices.

The project uses the course's controller/use-case/presenter style. Also, the project word MVP refers to the minimum viable product in planning and grading. [Chapter 10](https://github.com/CSC207-UofT/207-course-notes/blob/main/10-introduction-to-layered-architectures.md)

### Chapter 11 — Clean Architecture

The Dependency Rule places dependencies toward inner policy. Domain entities enforce domain concepts and rules. Use cases coordinate actions using domain objects and their own boundary interfaces. Outer interface adapters convert requests and results. Swing, databases, file systems, network clients, and application startup are outer details.

The typical runtime flow is view event, controller and input data, interactor through its input boundary, any domain or data-access work, then a result on the output boundary. A presenter implements that output interface and writes presentation data to a view model. The view reads or observes that model and draws it. An outer adapter may run at the interactor's call site through a port the interactor owns. The runtime direction of a call can then differ from the source-code dependency direction.

Create actual views, gateways, and presenters in Main or an AppBuilder in dependency order, using constructor arguments to connect them. Send plain data across the layer boundaries. A unit test can supply a fake gateway and record results. Test the content of the result and application state after execution so a missed output cannot hide. The existing search and weekly removal flows show much of this pattern in the team code. [Chapter 11](https://github.com/CSC207-UofT/207-course-notes/blob/main/11-clean-architecture.md) · [Team architecture](architecture.md)

### Chapter 12 — Patterns

Patterns describe recurring design choices and when they help. They embody design principles with specific costs.

| Pattern | Notes and possible use |
| --- | --- |
| Simple Factory | A creation method selects among related implementations. Useful if a selected catalogue strategy genuinely varies with input. |
| Abstract Factory | A family of related objects is constructed through a shared interface. Useful when several alternate families must stay consistent. |
| Builder | Stepwise assembly of a complex object. The course AppBuilder is a useful model if Main wiring grows. |
| Strategy | A caller delegates an interchangeable behaviour to an object with a shared interface. An actual alternative ranking policy can use this. One straightforward comparator is enough initially. |
| Observer | A subject reports changes to listeners. `PropertyChangeSupport` can notify Swing views after a presenter writes new view-model state. |
| Façade | A small entry point exposes the common operations of a complicated subsystem. Keep details hidden when callers need only a handful of operations. |
| Adapter | A wrapper makes a foreign interface or data format meet the application's expected contract. The JSON catalogue loader and file/HTTP implementations are good starting points. Prefer delegation unless inheritance actually fits. |

Use a meaningful implemented pattern that fits the target rubric band. The Week 10 lecture gives guidance on choosing outside the six course-notes patterns for full pattern credit. Discuss the problem it solved and show the concrete classes and relationships. Demonstrate the extension point you actually built. [Chapter 12](https://github.com/CSC207-UofT/207-course-notes/blob/main/12-design-patterns.md) · [Project design pattern expectation](project-expectations.md#design-pattern-expectation)

### Chapter 13 — Refactoring

Refactoring changes code structure while preserving observable behaviour. Make small changes with tests. Examples are extracting a coherent method, changing a method declaration and all callers, encapsulating a field, splitting a loop into clearer tasks, moving statements close to the data they use while preserving order, and replacing awkward construction with a factory or builder where warranted. Timetable examples are extracting shared interval checks, reorganizing a JSON mapper by field group, and preserving behaviour when replacing the boolean Add Section response. If a refactor also changes course policy, identify the changed behaviour and test it explicitly. [Chapter 13](https://github.com/CSC207-UofT/207-course-notes/blob/main/13-refactoring-techniques.md)

### Chapter 14 — Regular expressions

Use a regex for patterns in strings. Anchors constrain where it matches. Character classes describe permitted symbols. Groups and alternatives describe pieces of a pattern and quantifiers describe repetition. Escaping can be needed both at the regex level and inside a Java string. Learn the difference between whole-string validation and finding/extracting matches with `Pattern` and `Matcher`. Test boundary cases and unusual real codes.

For prerequisites, regex can recognize known tokens and published code forms. Meaning depends on the complete sentence, logical grouping, scope, and student facts. Use a typed operator tree with an explicit review state for rules the publisher cannot interpret unambiguously. [Chapter 14](https://github.com/CSC207-UofT/207-course-notes/blob/main/14-regular-expressions.md)

## Supplemental notes

### Quickstart, exercises, and environments

The course Quickstart covers Git, Java, Maven, and IntelliJ installation across platforms, source layout, `javac` and `java`, Maven lifecycle commands, running tests, opening/importing a Maven project, determining package and working paths, ignoring IDE/build artefacts, and selecting Maven profiles and modules for the exercises. Its example code targets Java 11. The team project declares Java 17 and has its own Maven wrapper and run scripts. Use the wrapper in the team repo and leave course exercise commands to the course checkout. The notes' exercise index gives practice, executable checks where applicable, and written design exercises. A sample class size or convention there can give a starting point. [Quickstart](https://github.com/CSC207-UofT/207-course-notes/blob/main/QUICKSTART.md) · [Exercises](https://github.com/CSC207-UofT/207-course-notes/tree/main/exercises)

### Code Style and Documentation

The course uses Google Java Style, Checkstyle 10.26.1 in the course-notes build, and the CheckStyle-IDEA plugin with the same course checks and version. The course-notes Maven build runs Checkstyle at `validate`. It uses Spotless and google-java-format for predictable formatting. Checkstyle examines style and documentation conventions. Formatting can fix whitespace and layout, and human work still addresses names, bracing choices, documentation and design. Course exercise or module commands need their own profile/module. The team should retain local evidence, agree which code to format and review, and keep its own build reproducible. [Course style guide](https://github.com/CSC207-UofT/207-course-notes/blob/main/code-style-and-documentation.md) · [Team style audit](testing.md#style-and-coverage-work)

Write class Javadoc above the declaration, including the responsibility and externally important invariants. Document the behaviour and constraints of a public method or constructor with a concise summary and relevant `@param`, `@return` and `@throws` tags. Add explanatory comments where a decision, ambiguity, or source-data constraint would be hard to infer. The separate academic-code or timeslot identifiers are useful examples of values that deserve documented meaning.

### APIs, JSON, and files

Try API calls with a real, documented endpoint. Java's `HttpClient` can send a GET, read the HTTP status and response body and apply a timeout. Check input and status before parsing. Follow service limits and keep credentials outside app source. Parse JSON as data, validate shape and types, and keep the external JSON mapping at the edge of the app. For files, learn UTF-8 reading/writing using `Path` and `Files`, decide which data should persist, handle absent or unreadable files, validate loaded data, and keep errors distinct from an empty result. Use a local fixture and the real data adapter to test the two sides of the boundary. [APIs, JSON, and Files](https://github.com/CSC207-UofT/207-course-notes/blob/main/apis-json-and-files.md)

### Developer roles

Frontend builds the view and handles interaction. Data access handles HTTP, files and persistence. Use-case engineering expresses user goals and domain rules. Integration creates and connects the objects. Testing, code review, and build and release work cut across those roles. A team member can perform several roles in their own vertical slice. Coordinate when you share a ViewModel or gateway. [Developer Roles](https://github.com/CSC207-UofT/207-course-notes/blob/main/developer-roles.md)

### Embedded ethics

The module asks who gets included when we picture a typical user. It presents medical and social models of disability, different interventions, and distinctions between material impacts and harms to people's relationships or standing. Apply the user perspective to navigation, search labels, reliance on colour, bad-data messaging, and how the app communicates limitations. For example, clear text alongside overlap colours and support for keyboard interaction belong in ordinary Swing design. A walking route estimate describes an official public walking route at building level. [Embedded Ethics Module 1](https://github.com/CSC207-UofT/207-course-notes/blob/main/embedded-ethics.md)
