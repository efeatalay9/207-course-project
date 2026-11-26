package usecase.checkconflicts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.CourseOffering;
import domain.MeetingTime;
import domain.Section;
import domain.StatusFlag;
import domain.Timetable;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import usecase.checkconflicts.CheckConflictsResponse.Conflict;
import usecase.checkconflicts.CheckConflictsResponse.Reason;
import usecase.checkconflicts.CheckConflictsResponse.SectionLabel;
import usecase.checkconflicts.CheckConflictsResponse.UncheckedSection;
import usecase.common.CatalogAccessException;
import usecase.viewtimetable.OfferingLookup;

class CheckConflictsInteractorTest {
  private static final String FALL = "20269";
  private static final String WINTER = "20271";

  private final Timetable timetable = new Timetable();
  private final List<Object> events = new ArrayList<>();
  private final CheckConflictsOutputBoundary output = new CheckConflictsOutputBoundary() {
    @Override
    public void present(CheckConflictsResponse response) {
      events.add(response);
    }

    @Override
    public void presentNothingToCheck() {
      events.add("nothing");
    }

    @Override
    public void presentFailure(String message) {
      events.add("failed: " + message);
    }
  };

  private static MeetingTime meeting(DayOfWeek day, int startHour, int startMinute, int endHour,
      String session, String repetition) {
    return new MeetingTime(day, LocalTime.of(startHour, startMinute), LocalTime.of(endHour, 0),
        session, "BA1160", repetition, repetition.equals("WEEKLY") ? "ONCE_A_WEEK" : "MANUAL");
  }

  private static Section section(String name, MeetingTime... meetings) {
    return new Section(name, name.substring(0, 3), StatusFlag.NO, StatusFlag.NO,
        List.of(meetings), List.of());
  }

  private static CourseOffering offering(String id, String code, Section... sections) {
    return new CourseOffering(id, code, code + " title", "St. George", List.of(FALL, WINTER), "F",
        StatusFlag.NO, List.of(sections));
  }

  // CSC207 LEC Mon 10-11 overlaps MAT137 TUT Mon 10:30-12. CSC207 TUT Mon 11-12 is back to back.
  private static final Map<String, CourseOffering> CATALOGUE = Map.of(
      "csc207", offering("csc207", "CSC207H1",
          section("LEC0101", meeting(DayOfWeek.MONDAY, 10, 0, 11, FALL, "WEEKLY"),
              meeting(DayOfWeek.MONDAY, 10, 0, 11, WINTER, "WEEKLY")),
          section("TUT0101", meeting(DayOfWeek.MONDAY, 11, 0, 12, FALL, "WEEKLY")),
          section("LEC5101")),
      "mat137", offering("mat137", "MAT137Y1",
          section("TUT0102", meeting(DayOfWeek.MONDAY, 10, 30, 12, FALL, "WEEKLY"),
              meeting(DayOfWeek.MONDAY, 10, 30, 12, WINTER, "MANUAL"))));

  private final OfferingLookup lookup = id -> Optional.ofNullable(CATALOGUE.get(id));

  private CheckConflictsResponse check(String session) {
    new CheckConflictsInteractor(timetable, lookup, output)
        .execute(new CheckConflictsRequest(session));
    return (CheckConflictsResponse) events.get(events.size() - 1);
  }

  @Test
  void findsTheOverlapAndIgnoresBackToBackClasses() {
    timetable.addSection("csc207", "LEC0101");
    timetable.addSection("csc207", "TUT0101");
    timetable.addSection("mat137", "TUT0102");

    CheckConflictsResponse response = check(FALL);

    assertEquals(FALL, response.session());
    SectionLabel lecture = new SectionLabel("csc207", "CSC207H1", "LEC0101");
    SectionLabel tutorial = new SectionLabel("csc207", "CSC207H1", "TUT0101");
    SectionLabel mat = new SectionLabel("mat137", "MAT137Y1", "TUT0102");
    assertEquals(List.of(
        new Conflict(lecture, mat, DayOfWeek.MONDAY, LocalTime.of(10, 30), LocalTime.of(11, 0),
            true),
        new Conflict(tutorial, mat, DayOfWeek.MONDAY, LocalTime.of(11, 0), LocalTime.of(12, 0),
            true)),
        response.conflicts());
    assertTrue(response.hasConfirmedConflict());
    assertTrue(response.unchecked().isEmpty());
  }

  @Test
  void onlyTheRequestedSessionIsChecked() {
    timetable.addSection("csc207", "LEC0101");
    timetable.addSection("mat137", "TUT0102");

    CheckConflictsResponse response = check(WINTER);

    assertEquals(WINTER, response.session());
    assertEquals(1, response.conflicts().size());
    assertFalse(response.conflicts().get(0).confirmed());
    assertFalse(response.hasConfirmedConflict());
  }

  @Test
  void unknownOrEmptySessionFallsBackToTheEarliest() {
    timetable.addSection("csc207", "LEC0101");

    assertEquals(FALL, check("").session());
    assertEquals(FALL, check("20305").session());
    new CheckConflictsInteractor(timetable, lookup, output).execute(null);
    assertEquals(FALL, ((CheckConflictsResponse) events.get(2)).session());
    assertTrue(((CheckConflictsResponse) events.get(2)).conflicts().isEmpty());
  }

  @Test
  void sectionsWithoutTimesAreReportedAsUnchecked() {
    timetable.addSection("csc207", "LEC5101");
    timetable.addSection("csc207", "PRA9999");
    timetable.addSection("gone", "LEC0101");

    CheckConflictsResponse response = check(FALL);

    assertEquals("", response.session());
    assertEquals(List.of(
        new UncheckedSection(new SectionLabel("csc207", "CSC207H1", "LEC5101"), Reason.TIME_TBA),
        new UncheckedSection(new SectionLabel("csc207", "CSC207H1", "PRA9999"), Reason.NOT_FOUND),
        new UncheckedSection(new SectionLabel("gone", "", "LEC0101"), Reason.NOT_FOUND)),
        response.unchecked());
  }

  @Test
  void anEmptyTimetableHasNothingToCheck() {
    new CheckConflictsInteractor(timetable, lookup, output).execute(new CheckConflictsRequest(""));
    assertEquals(List.of("nothing"), events);
  }

  @Test
  void catalogueFailureIsReported() {
    timetable.addSection("csc207", "LEC0101");
    OfferingLookup broken = id -> {
      throw new CatalogAccessException("Timetable Builder is unavailable.", null);
    };

    new CheckConflictsInteractor(timetable, broken, output).execute(new CheckConflictsRequest(""));

    assertEquals(List.of("failed: Timetable Builder is unavailable."), events);
  }

  @Test
  void requiresItsCollaborators() {
    assertThrows(NullPointerException.class,
        () -> new CheckConflictsInteractor(null, lookup, output));
    assertThrows(NullPointerException.class,
        () -> new CheckConflictsInteractor(timetable, null, output));
    assertThrows(NullPointerException.class,
        () -> new CheckConflictsInteractor(timetable, lookup, null));
    assertThrows(NullPointerException.class,
        () -> new CheckConflictsResponse(null, List.of(), List.of()));
  }
}
