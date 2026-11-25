package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.DayOfWeek;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MeetingOverlapTest {
  @ParameterizedTest
  @CsvSource({
    "9,10,NONE",
    "11,12,NONE",
    "10,11,CONFIRMED",
    "9,12,CONFIRMED",
    "10,12,CONFIRMED",
    "9,11,CONFIRMED",
    "12,13,NONE"
  })
  void checksPositiveIntersectionAndAllowsAdjacentMeetings(
      int start, int end, MeetingOverlap.Kind expected) {
    assertEquals(
        expected,
        MeetingOverlap.compare(
            meeting(10, 11, "20269", DayOfWeek.MONDAY, "WEEKLY"),
            meeting(start, end, "20269", DayOfWeek.MONDAY, "WEEKLY")));
  }

  @Test
  void differentDaysAndSessionsDoNotConflict() {
    MeetingTime first = meeting(10, 11, "20269", DayOfWeek.MONDAY, "WEEKLY");
    assertEquals(
        MeetingOverlap.Kind.NONE,
        MeetingOverlap.compare(first, meeting(10, 11, "20271", DayOfWeek.MONDAY, "WEEKLY")));
    assertEquals(
        MeetingOverlap.Kind.NONE,
        MeetingOverlap.compare(first, meeting(10, 11, "20269", DayOfWeek.TUESDAY, "WEEKLY")));
  }

  @Test
  void unverifiedRecurrenceOrSessionOnlyProducesPossibleOverlap() {
    MeetingTime first = meeting(10, 11, "20269", DayOfWeek.MONDAY, "WEEKLY");
    for (String repetition : new String[] {"BI_WEEKLY", "MANUAL", ""}) {
      assertEquals(
          MeetingOverlap.Kind.POSSIBLE,
          MeetingOverlap.compare(first, meeting(10, 11, "20269", DayOfWeek.MONDAY, repetition)));
    }
    assertEquals(
        MeetingOverlap.Kind.POSSIBLE,
        MeetingOverlap.compare(first, meeting(10, 11, "", DayOfWeek.MONDAY, "WEEKLY")));
  }

  private MeetingTime meeting(
      int start, int end, String session, DayOfWeek day, String recurrence) {
    return new MeetingTime(
        day,
        LocalTime.of(start, 0),
        LocalTime.of(end, 0),
        session,
        "BA",
        recurrence,
        "ONCE_A_WEEK");
  }
}
