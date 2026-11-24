package usecase.addsection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static usecase.addsection.AddSectionResponse.Outcome.ADDED;
import static usecase.addsection.AddSectionResponse.Outcome.ALREADY_SELECTED;
import static usecase.addsection.AddSectionResponse.Outcome.CANCELLED;
import static usecase.addsection.AddSectionResponse.Outcome.CATALOGUE_UNAVAILABLE;
import static usecase.addsection.AddSectionResponse.Outcome.NOT_FOUND;
import static usecase.addsection.AddSectionResponse.Outcome.NO_SELECTION;

import domain.CourseOffering;
import domain.Section;
import domain.SelectedSection;
import domain.StatusFlag;
import domain.Timetable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import usecase.common.CatalogAccessException;
import usecase.viewtimetable.OfferingLookup;

class AddSectionInteractorTest {
  private static final String SECTION_NAME = "LEC0101";
  private static final String COURSE_CODE = "CSC108H1";
  private final Timetable timetable = new Timetable();
  private final List<AddSectionResponse> responses = new ArrayList<>();

  @Test
  void addsSectionsWithTheSameNameFromDifferentOfferings() {
    CourseOffering fall = offering("fall", "20269", StatusFlag.NO, StatusFlag.NO);
    CourseOffering winter = offering("winter", "20271", StatusFlag.NO, StatusFlag.NO);
    AddSectionInteractor interactor = interactorWith(fall, winter);

    interactor.execute(new AddSectionRequest("fall", SECTION_NAME));
    interactor.execute(new AddSectionRequest("winter", SECTION_NAME));

    assertEquals(
        List.of(
            new AddSectionResponse(ADDED, COURSE_CODE, SECTION_NAME),
            new AddSectionResponse(ADDED, COURSE_CODE, SECTION_NAME)),
        responses);
    assertEquals(
        List.of(
            new SelectedSection("fall", SECTION_NAME), new SelectedSection("winter", SECTION_NAME)),
        timetable.selections());
  }

  @Test
  void reportsDuplicateWithoutAddingAnotherCopy() {
    AddSectionInteractor interactor =
        interactorWith(offering("fall", "20269", StatusFlag.NO, StatusFlag.NO));
    AddSectionRequest selection = new AddSectionRequest("fall", SECTION_NAME);

    interactor.execute(selection);
    interactor.execute(selection);

    assertEquals(
        List.of(
            new AddSectionResponse(ADDED, COURSE_CODE, SECTION_NAME),
            new AddSectionResponse(ALREADY_SELECTED, COURSE_CODE, SECTION_NAME)),
        responses);
    assertEquals(List.of(new SelectedSection("fall", SECTION_NAME)), timetable.selections());
  }

  @Test
  void incompleteSelectionsLeaveTheTimetableAndCatalogueAlone() {
    timetable.addSection("saved", SECTION_NAME);
    OfferingLookup unused =
        id -> {
          throw new AssertionError("An incomplete selection should not query the catalogue.");
        };
    AddSectionInteractor interactor = new AddSectionInteractor(timetable, unused, responses::add);

    interactor.execute(null);
    interactor.execute(new AddSectionRequest(null, SECTION_NAME));
    interactor.execute(new AddSectionRequest("  ", SECTION_NAME));
    interactor.execute(new AddSectionRequest("fall", null));
    interactor.execute(new AddSectionRequest("fall", "  "));

    assertEquals(
        List.of(NO_SELECTION, NO_SELECTION, NO_SELECTION, NO_SELECTION, NO_SELECTION),
        responses.stream().map(AddSectionResponse::outcome).toList());
    assertEquals(List.of(new SelectedSection("saved", SECTION_NAME)), timetable.selections());
  }

  @Test
  void rejectsAnUnknownOfferingOrSectionWithoutChangingExistingSelections() {
    timetable.addSection("saved", SECTION_NAME);
    AddSectionInteractor interactor =
        interactorWith(offering("fall", "20269", StatusFlag.NO, StatusFlag.NO));

    interactor.execute(new AddSectionRequest("missing", SECTION_NAME));
    interactor.execute(new AddSectionRequest("fall", "TUT9999"));

    assertEquals(
        List.of(
            new AddSectionResponse(NOT_FOUND),
            new AddSectionResponse(NOT_FOUND, COURSE_CODE, "TUT9999")),
        responses);
    assertEquals(List.of(new SelectedSection("saved", SECTION_NAME)), timetable.selections());
  }

  @Test
  void rejectsCancelledOfferingsAndSections() {
    CourseOffering cancelledOffering = offering("fall", "20269", StatusFlag.YES, StatusFlag.NO);
    CourseOffering cancelledSection = offering("winter", "20271", StatusFlag.NO, StatusFlag.YES);
    AddSectionInteractor interactor = interactorWith(cancelledOffering, cancelledSection);

    interactor.execute(new AddSectionRequest("fall", SECTION_NAME));
    interactor.execute(new AddSectionRequest("winter", SECTION_NAME));

    AddSectionResponse cancelled = new AddSectionResponse(CANCELLED, COURSE_CODE, SECTION_NAME);
    assertEquals(List.of(cancelled, cancelled), responses);
    assertEquals(List.of(), timetable.selections());
  }

  @Test
  void allowsAnUnscheduledSectionWhenCancellationIsUnknown() {
    AddSectionInteractor interactor =
        interactorWith(offering("fall", "20269", StatusFlag.UNKNOWN, StatusFlag.UNKNOWN));

    interactor.execute(new AddSectionRequest("fall", SECTION_NAME));

    assertEquals(List.of(new AddSectionResponse(ADDED, COURSE_CODE, SECTION_NAME)), responses);
    assertEquals(List.of(new SelectedSection("fall", SECTION_NAME)), timetable.selections());
  }

  @Test
  void reportsCatalogueFailureWithoutChangingExistingSelections() {
    timetable.addSection("saved", SECTION_NAME);
    OfferingLookup unavailable =
        id -> {
          throw new CatalogAccessException("Catalogue unavailable", null);
        };
    AddSectionInteractor interactor =
        new AddSectionInteractor(timetable, unavailable, responses::add);

    interactor.execute(new AddSectionRequest("fall", SECTION_NAME));

    assertEquals(List.of(new AddSectionResponse(CATALOGUE_UNAVAILABLE)), responses);
    assertEquals(List.of(new SelectedSection("saved", SECTION_NAME)), timetable.selections());
  }

  private AddSectionInteractor interactorWith(CourseOffering... offerings) {
    OfferingLookup lookup =
        id -> {
          for (CourseOffering offering : offerings) {
            if (offering.id().equals(id)) {
              return Optional.of(offering);
            }
          }
          return Optional.empty();
        };
    return new AddSectionInteractor(timetable, lookup, responses::add);
  }

  private static CourseOffering offering(
      String id, String session, StatusFlag offeringCancelled, StatusFlag sectionCancelled) {
    Section section =
        new Section(SECTION_NAME, "LEC", sectionCancelled, StatusFlag.YES, List.of(), List.of());
    return new CourseOffering(
        id,
        COURSE_CODE,
        "Sample course",
        "St. George",
        List.of(session),
        "F",
        offeringCancelled,
        List.of(section));
  }
}
