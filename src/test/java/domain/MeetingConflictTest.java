package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.MeetingConflict.Certainty;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class MeetingConflictTest {
  private static MeetingTime meeting(DayOfWeek day, int start, int end, String session,
      String repetition, String pattern) {
    return new MeetingTime(day, LocalTime.of(start / 100, start % 100),
        LocalTime.of(end / 100, end % 100), session, "", repetition, pattern);
  }

  private static MeetingTime weekly(DayOfWeek day, int start, int end) {
    return meeting(day, start, end, "20269", "WEEKLY", "ONCE_A_WEEK");
  }

  @Test
  void overlappingWeeklyMeetingsShareTheMiddleTime() {
    MeetingConflict conflict = MeetingConflict.between(
        weekly(DayOfWeek.MONDAY, 1000, 1100), weekly(DayOfWeek.MONDAY, 1030, 1200)).orElseThrow();

    assertEquals(new MeetingConflict(DayOfWeek.MONDAY, LocalTime.of(10, 30), LocalTime.of(11, 0),
        Certainty.CONFIRMED), conflict);
    assertEquals(30, conflict.minutes());
  }

  @Test
  void containedMeetingClashesForItsWholeLength() {
    MeetingConflict conflict = MeetingConflict.between(
        weekly(DayOfWeek.TUESDAY, 1400, 1500), weekly(DayOfWeek.TUESDAY, 1300, 1600)).orElseThrow();
    assertEquals(LocalTime.of(14, 0), conflict.start());
    assertEquals(LocalTime.of(15, 0), conflict.end());
  }

  @Test
  void backToBackMeetingsDoNotClash() {
    assertEquals(Optional.empty(), MeetingConflict.between(
        weekly(DayOfWeek.MONDAY, 1000, 1100), weekly(DayOfWeek.MONDAY, 1100, 1200)));
    assertEquals(Optional.empty(), MeetingConflict.between(
        weekly(DayOfWeek.MONDAY, 1100, 1200), weekly(DayOfWeek.MONDAY, 1000, 1100)));
  }

  @Test
  void differentDaysOrSessionsDoNotClash() {
    assertEquals(Optional.empty(), MeetingConflict.between(
        weekly(DayOfWeek.MONDAY, 1000, 1100), weekly(DayOfWeek.WEDNESDAY, 1000, 1100)));
    assertEquals(Optional.empty(), MeetingConflict.between(
        weekly(DayOfWeek.MONDAY, 1000, 1100),
        meeting(DayOfWeek.MONDAY, 1000, 1100, "20271", "WEEKLY", "ONCE_A_WEEK")));
  }

  @Test
  void biweeklyMeetingsOnOppositeWeeksDoNotClash() {
    MeetingTime odd = meeting(DayOfWeek.FRIDAY, 1100, 1200, "20269", "BI_WEEKLY",
        "FIRST_AND_THIRD_WEEK");
    MeetingTime even = meeting(DayOfWeek.FRIDAY, 1100, 1200, "20269", "BI_WEEKLY",
        "SECOND_AND_FOURTH_WEEK");

    assertEquals(Optional.empty(), MeetingConflict.between(odd, even));
    assertEquals(Certainty.CONFIRMED, MeetingConflict.between(odd, odd).orElseThrow().certainty());
    assertEquals(Certainty.CONFIRMED,
        MeetingConflict.between(odd, weekly(DayOfWeek.FRIDAY, 1130, 1230))
            .orElseThrow().certainty());
  }

  @Test
  void unusualRepetitionNeedsReview() {
    MeetingTime manual = meeting(DayOfWeek.THURSDAY, 1300, 1500, "20269", "MANUAL", "MANUAL");
    MeetingTime unknownWeeks = meeting(DayOfWeek.THURSDAY, 1300, 1500, "20269", "BI_WEEKLY", "");
    MeetingTime weekly = weekly(DayOfWeek.THURSDAY, 1400, 1500);

    assertEquals(Certainty.NEEDS_REVIEW,
        MeetingConflict.between(manual, weekly).orElseThrow().certainty());
    assertEquals(Certainty.NEEDS_REVIEW,
        MeetingConflict.between(weekly, unknownWeeks).orElseThrow().certainty());
    assertTrue(MeetingConflict.between(unknownWeeks, meeting(DayOfWeek.THURSDAY, 1300, 1500,
        "20269", "BI_WEEKLY", "SECOND_AND_FOURTH_WEEK")).isPresent());
  }

  @Test
  void rejectsInvalidValues() {
    LocalTime ten = LocalTime.of(10, 0);
    assertThrows(IllegalArgumentException.class,
        () -> new MeetingConflict(DayOfWeek.MONDAY, ten, ten, Certainty.CONFIRMED));
    assertThrows(NullPointerException.class,
        () -> new MeetingConflict(null, ten, ten.plusHours(1), Certainty.CONFIRMED));
    assertThrows(NullPointerException.class,
        () -> new MeetingConflict(DayOfWeek.MONDAY, null, ten, Certainty.CONFIRMED));
    assertThrows(NullPointerException.class,
        () -> new MeetingConflict(DayOfWeek.MONDAY, ten, null, Certainty.CONFIRMED));
    assertThrows(NullPointerException.class,
        () -> new MeetingConflict(DayOfWeek.MONDAY, ten, ten.plusHours(1), null));
    MeetingTime monday = weekly(DayOfWeek.MONDAY, 1000, 1100);
    assertThrows(NullPointerException.class, () -> MeetingConflict.between(null, monday));
    assertThrows(NullPointerException.class, () -> MeetingConflict.between(monday, null));
  }
}
