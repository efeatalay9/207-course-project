# Architecture plan

## First feature and acceptance criteria

As a student, I want to search by course code and see the matching offerings,
their sections, and their meeting times, so I can choose sections that fit my
schedule.

The initial flow is: enter a course code, search the catalogue, and display matching
offerings with their sections and meeting times.

- Ignore surrounding whitespace and capitalization when matching a full code.
- Return every matching offering, labelled with its term and campus.
- Display each section's teaching type, known meeting times, and cancellation or
  TBA information. Preserve academic-session and recurrence details.
- Show a clear no-results message for an unknown code.
- Treat blank input as invalid and do not query the catalogue.
- Distinguish a catalogue failure from a successful search with no results.
- Do not interpret missing meeting times as proof of an asynchronous section or
  as proof that a section fits any schedule.

Timetable editing and automatic conflict detection are later features. This first
feature presents information so that students can assess their options themselves.

## Approach and dependencies

Use a small Clean Architecture design in Java. Keep domain objects and application
rules independent of storage formats and UI libraries. Interfaces belong to the
use case that needs them, and outer adapters implement those interfaces.

| Package | Components | Responsibility |
| --- | --- | --- |
| `domain` | `CourseOffering`, `Section`, `MeetingTime` | Represent catalogue concepts and basic validity rules |
| `usecase.searchcourses` | `SearchCoursesInteractor`, request/response data, boundaries, `CourseCatalog` | Validate and coordinate search |
| `dataaccess.catalog` | `JsonCatalogLoader`, `InMemoryCourseCatalog` | Map JSON into domain objects and index offerings for lookup |
| `interfaceadapter.searchcourses` | Controller, presenter, view model | Translate UI actions into requests and responses into display state |
| `view` | Search view | Accept input and display results |
| `app` | `Main` | Load data, construct implementations, and connect dependencies |

Domain code depends on no outer layer. Use cases depend on domain objects. Data
and UI adapters depend on the relevant use-case contracts. Only startup wiring
needs to know all the concrete implementations.

```mermaid
classDiagram
    class SearchCoursesInputBoundary {
        <<interface>>
        +execute(SearchCoursesRequest)
    }
    class CourseCatalog {
        <<interface>>
        +findByCode(code) List~CourseOffering~
    }
    class SearchCoursesOutputBoundary {
        <<interface>>
        +present(SearchCoursesResponse)
    }

    SearchCoursesController --> SearchCoursesInputBoundary
    SearchCoursesInteractor ..|> SearchCoursesInputBoundary
    SearchCoursesInteractor --> CourseCatalog
    SearchCoursesInteractor --> SearchCoursesOutputBoundary
    InMemoryCourseCatalog ..|> CourseCatalog
    JsonCatalogLoader --> CourseOffering : constructs
    InMemoryCourseCatalog --> CourseOffering : indexes
    SearchCoursesPresenter ..|> SearchCoursesOutputBoundary
    SearchCoursesPresenter --> SearchCoursesViewModel
    SearchView --> SearchCoursesController
    SearchView --> SearchCoursesViewModel : displays
    CourseOffering "1" *-- "0..*" Section
    Section "1" *-- "0..*" MeetingTime
```

`Main` injects a catalogue and output boundary into the interactor through its
constructor. The interactor does not construct a loader, presenter, or view.
Load and index the catalogue once at startup rather than parsing the full file
for every search. Report a startup load failure clearly instead of substituting
an apparently valid empty catalogue.

## Domain model

- `CourseOffering`: offering ID, code, title, campus, academic sessions, term
  designation, cancellation status, and sections. Use the offering ID for identity;
  a code alone is not unique.
- `Section`: identity within an offering, name, teaching type, cancellation/TBA
  status, meeting times, and linked-section references.
- `MeetingTime`: day, start/end time, academic session, optional location, and
  recurrence information.

Prefer immutable objects and read-only collections. Represent absent information
explicitly. Lectures, tutorials, and practicals are section types, not subclasses
of course offerings. Preserve recurrence and section-link information for later
scheduling features without implementing those features now.

The supplied snapshot contains multiple offerings for 1,028 course codes, sections
without meeting times, cancelled sections, and weekly, alternate-week, and manual
recurrence values. Confirm source encodings, including day numbering, before
mapping them into display labels; do not silently invent missing dates or times.

## Shared contracts

- `SearchCoursesRequest` contains the entered code. The interactor owns trimming,
  case normalization, and input validation.
- `CourseCatalog.findByCode` accepts a normalized full code and returns all matching
  offerings. An empty list means no matches; implementations never return `null`.
  If access fails, use a catalogue-specific failure defined at the use-case
  boundary, not a JSON-library or filesystem exception.
- `SearchCoursesResponse` distinguishes success, invalid input, and catalogue
  failure. Success may contain zero results. Response data is independent of UI
  widgets and JSON structures.
- The presenter owns display formatting and messages. The view receives display
  state rather than raw JSON or mutable domain objects.
- Search results have a documented, deterministic ordering. Specify the exact
  ordering and concrete data types together when writing the contracts.

## SOLID rules in practice

| Principle | Application |
| --- | --- |
| Single responsibility | Loader parses, catalogue searches, interactor coordinates, presenter formats, and view renders. |
| Open/closed | An alternative catalogue implements `CourseCatalog` without requiring changes to the search interactor. |
| Liskov substitution | Real and fake catalogues obey the same matching, empty-result, and failure contracts. |
| Interface segregation | Search needs a narrow read interface; future timetable saving gets a separate interface. |
| Dependency inversion | The use case defines the abstractions; data and presentation implementations are injected. |

Create interfaces at meaningful boundaries. Do not add an interface for every
class or introduce inheritance without a concrete substitutability requirement.

## Working together and code readability

- Agree on shared method signatures and request/response types before dividing
  implementation work. Coordinate changes to these contracts with their consumers.
- Split work along domain, search, catalogue, and UI boundaries. Use feature
  branches and small pull requests; do not push directly to `main`.
- Use descriptive names, small focused methods, and a consistent package layout.
- Add plain comments documenting each class's purpose and each method's role,
  inputs, outputs, and failure behaviour where relevant. Javadoc is not required
  yet. Explain non-obvious decisions and data assumptions rather than narrating
  every statement.
- Keep parsing details, filesystem paths, and UI-specific dependencies out of the
  domain and use-case packages.
- Explain the change and its validation in each pull request so teammates can
  understand and extend the implementation.

## Implementation order and validation

1. Establish the build setup, shared contracts, and small domain model.
2. Implement search using a fake catalogue and a capturing output boundary. Test
   normalization, blank input, multiple offerings, no matches, and access failure.
3. Implement JSON mapping and indexed lookup with small representative fixtures.
   Cover missing times, cancellation/TBA flags, recurrence, and linked sections.
4. Connect the controller, presenter, and minimal view through startup wiring.
5. Smoke-test the full search flow using the supplied catalogue.

Routine tests should use small fixtures rather than loading the entire 130 MB
catalogue. Add timetable selection and conflict detection as separate use cases
after the first search flow works.
