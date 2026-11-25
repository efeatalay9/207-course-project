package usecase.overlaps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import dataaccess.catalog.InMemoryOfferingLookup;
import domain.CourseOffering;
import domain.MeetingTime;
import domain.Section;
import domain.SelectedSection;
import domain.StatusFlag;
import domain.Timetable;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import usecase.common.CatalogAccessException;

class SectionOverlapCheckerTest {
  @Test
  void comparesEveryComponentAndEveryMeetingWithoutChangingSelections() throws Exception {
    CourseOffering existing =
        offering(
            "existing",
            List.of(
                section("LEC0101", "LEC", List.of(meeting(9, 10, "WEEKLY"))),
                section("TUT0101", "TUT", List.of(meeting(10, 11, "WEEKLY"))),
                section("PRA0101", "PRA", List.of(meeting(11, 12, "WEEKLY")))));
    CourseOffering candidate =
        offering(
            "candidate",
            List.of(
                section(
                    "LEC0201",
                    "LEC",
                    List.of(meeting(13, 14, "WEEKLY"), meeting(9, 12, "WEEKLY")))));
    Timetable timetable = new Timetable();
    for (Section section : existing.sections()) {
      timetable.addSection(existing.id(), section.name());
    }
    final List<SelectedSection> snapshot = timetable.selections();
    AtomicReference<List<SectionOverlapResult>> captured = new AtomicReference<>();
    CheckSectionOverlapsOutputBoundary output = output(captured);
    new CheckSectionOverlapsInteractor(timetable, checker(existing, candidate), output)
        .execute(List.of(new SelectedSection("candidate", "LEC0201")));
    assertEquals(3, captured.get().get(0).conflicts().size());
    assertTrue(
        captured.get().get(0).conflicts().stream()
            .allMatch(SectionOverlapResult.Conflict::confirmed));
    assertEquals(snapshot, timetable.selections());
  }

  @Test
  void excludesSelfButChecksOtherSectionsOfSameOffering() throws Exception {
    CourseOffering offering =
        offering(
            "one",
            List.of(
                section("LEC0101", "LEC", List.of(meeting(10, 11, "WEEKLY"))),
                section("TUT0101", "TUT", List.of(meeting(10, 11, "WEEKLY")))));
    SelectedSection lecture = new SelectedSection("one", "LEC0101");
    SelectedSection tutorial = new SelectedSection("one", "TUT0101");
    SectionOverlapResult result =
        checker(offering).check(List.of(lecture), List.of(lecture, tutorial)).get(0);
    assertEquals(1, result.conflicts().size());
    assertEquals(tutorial, result.conflicts().get(0).existingSection());
    assertTrue(
        checker(offering).check(List.of(lecture), List.of(lecture)).get(0).conflicts().isEmpty());
  }

  @Test
  void unknownAndMissingSectionsRemainUncertain() throws Exception {
    CourseOffering offering = offering("one", List.of(section("TUT0101", "TUT", List.of())));
    SelectedSection unknown = new SelectedSection("one", "TUT0101");
    SelectedSection missing = new SelectedSection("gone", "LEC0101");
    List<SectionOverlapResult> results =
        checker(offering).check(List.of(unknown, missing), List.of(missing));
    assertTrue(results.stream().allMatch(SectionOverlapResult::incomplete));
    assertTrue(results.stream().allMatch(result -> result.conflicts().isEmpty()));
  }

  @Test
  void irregularRecurrenceIsPossibleRatherThanConfirmed() throws Exception {
    CourseOffering candidate =
        offering(
            "candidate", List.of(section("TUT0101", "TUT", List.of(meeting(10, 11, "BI_WEEKLY")))));
    CourseOffering existing =
        offering(
            "existing", List.of(section("LEC0101", "LEC", List.of(meeting(10, 11, "WEEKLY")))));
    SectionOverlapResult result =
        checker(candidate, existing)
            .check(
                List.of(new SelectedSection("candidate", "TUT0101")),
                List.of(new SelectedSection("existing", "LEC0101")))
            .get(0);
    assertFalse(result.conflicts().get(0).confirmed());
  }

  @Test
  void failureIsReportedWithoutChangingThePlan() {
    Timetable timetable = new Timetable();
    timetable.addSection("one", "LEC0101");
    AtomicReference<Boolean> failed = new AtomicReference<>(false);
    CheckSectionOverlapsOutputBoundary output =
        new CheckSectionOverlapsOutputBoundary() {
          public void present(List<SectionOverlapResult> results) {
            fail("Expected unavailable");
          }

          public void presentUnavailable() {
            failed.set(true);
          }
        };
    SectionOverlapChecker checker =
        new SectionOverlapChecker(id -> {
          throw new CatalogAccessException("Unavailable", null);
        });
    new CheckSectionOverlapsInteractor(timetable, checker, output).execute(timetable.selections());
    assertTrue(failed.get());
    assertEquals(1, timetable.selections().size());
  }

  @Test
  void alternateCheckerCanBeUsedBySearchAndTimetableWithoutChangingSelections() {
    Timetable timetable = new Timetable();
    timetable.addSection("one", "LEC0101");
    final List<SelectedSection> snapshot = timetable.selections();
    SectionOverlapCheck substitute =
        (candidates, selected) -> {
          assertEquals(snapshot, selected);
          return candidates.stream()
              .map(candidate -> new SectionOverlapResult(candidate, List.of(), true))
              .toList();
        };
    AtomicReference<List<SectionOverlapResult>> searchResult = new AtomicReference<>();
    new CheckSectionOverlapsInteractor(timetable, substitute, output(searchResult))
        .execute(snapshot);
    assertTrue(searchResult.get().get(0).incomplete());
    AtomicReference<usecase.viewtimetable.ViewTimetableResponse> gridResult =
        new AtomicReference<>();
    usecase.viewtimetable.ViewTimetableOutputBoundary gridOutput =
        new usecase.viewtimetable.ViewTimetableOutputBoundary() {
          public void present(usecase.viewtimetable.ViewTimetableResponse response) {
            gridResult.set(response);
          }

          public void presentFailure(String message) {
            fail(message);
          }
        };
    new usecase.viewtimetable.ViewTimetableInteractor(
            timetable, id -> java.util.Optional.empty(), substitute, gridOutput)
        .execute(new usecase.viewtimetable.ViewTimetableRequest(""));
    assertEquals(searchResult.get(), gridResult.get().overlaps());
    assertEquals(snapshot, timetable.selections());
  }

  private CheckSectionOverlapsOutputBoundary output(
      AtomicReference<List<SectionOverlapResult>> captured) {
    return new CheckSectionOverlapsOutputBoundary() {
      public void present(List<SectionOverlapResult> results) {
        captured.set(results);
      }

      public void presentUnavailable() {
        fail("Unexpected catalogue failure");
      }
    };
  }

  public static SectionOverlapChecker checker(CourseOffering... offerings) {
    return new SectionOverlapChecker(new InMemoryOfferingLookup(List.of(offerings)));
  }

  public static CourseOffering offering(String id, List<Section> sections) {
    return new CourseOffering(
        id, id.toUpperCase(), "Test", "St. George", List.of("20269"), "F", StatusFlag.NO, sections);
  }

  public static Section section(String name, String method, List<MeetingTime> meetings) {
    return new Section(name, method, StatusFlag.NO, StatusFlag.NO, meetings, List.of());
  }

  public static MeetingTime meeting(int start, int end, String recurrence) {
    return new MeetingTime(
        DayOfWeek.MONDAY,
        LocalTime.of(start, 0),
        LocalTime.of(end, 0),
        "20269",
        "BA",
        recurrence,
        "ONCE_A_WEEK");
  }
}
