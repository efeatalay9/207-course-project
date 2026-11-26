package domain;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;

/**
 * The time two meetings share, and how sure we are that they really clash.
 *
 * <p>Meetings only clash when they are in the same session, on the same day, and their times
 * overlap. A meeting that ends exactly when another starts is not a clash. Biweekly meetings on
 * opposite weeks never meet together, and unusual or missing repetition data is flagged for
 * review instead of being reported as a definite clash.
 *
 * @param day the day both meetings take place
 * @param start when the shared time begins
 * @param end when the shared time ends
 * @param certainty whether the clash is confirmed or needs review
 */
public record MeetingConflict(DayOfWeek day, LocalTime start, LocalTime end, Certainty certainty) {
  static final String WEEKLY = "WEEKLY";
  static final String BI_WEEKLY = "BI_WEEKLY";
  static final String ODD_WEEKS = "FIRST_AND_THIRD_WEEK";
  static final String EVEN_WEEKS = "SECOND_AND_FOURTH_WEEK";

  /** How sure the app is that two overlapping meetings happen in the same week. */
  public enum Certainty {
    /** Both meetings are known to happen in the same weeks. */
    CONFIRMED,
    /** The times overlap, but the repetition data cannot confirm they meet together. */
    NEEDS_REVIEW
  }

  /** Requires a positive shared interval. */
  public MeetingConflict {
    Objects.requireNonNull(day, "day");
    Objects.requireNonNull(start, "start");
    Objects.requireNonNull(end, "end");
    Objects.requireNonNull(certainty, "certainty");
    if (!end.isAfter(start)) {
      throw new IllegalArgumentException("A conflict must end after it starts.");
    }
  }

  /**
   * Finds the clash between two meetings, if there is one.
   *
   * @param first one meeting
   * @param second another meeting
   * @return the shared time and certainty, or empty when the meetings cannot clash
   */
  public static Optional<MeetingConflict> between(MeetingTime first, MeetingTime second) {
    Objects.requireNonNull(first, "first");
    Objects.requireNonNull(second, "second");
    if (!first.session().equals(second.session()) || first.day() != second.day()) {
      return Optional.empty();
    }
    LocalTime start = latest(first.start(), second.start());
    LocalTime end = earliest(first.end(), second.end());
    if (!end.isAfter(start) || onOppositeWeeks(first, second)) {
      return Optional.empty();
    }
    Certainty certainty = isKnown(first) && isKnown(second)
        ? Certainty.CONFIRMED : Certainty.NEEDS_REVIEW;
    return Optional.of(new MeetingConflict(first.day(), start, end, certainty));
  }

  /**
   * Returns how long the meetings overlap.
   *
   * @return the shared time in minutes
   */
  public long minutes() {
    return Duration.between(start, end).toMinutes();
  }

  // Weekly, or biweekly on a known set of weeks, can be compared with confidence.
  private static boolean isKnown(MeetingTime meeting) {
    return WEEKLY.equals(meeting.repetition())
        || BI_WEEKLY.equals(meeting.repetition())
            && (ODD_WEEKS.equals(meeting.repetitionPattern())
                || EVEN_WEEKS.equals(meeting.repetitionPattern()));
  }

  private static boolean onOppositeWeeks(MeetingTime first, MeetingTime second) {
    return isKnown(first) && isKnown(second)
        && BI_WEEKLY.equals(first.repetition()) && BI_WEEKLY.equals(second.repetition())
        && !first.repetitionPattern().equals(second.repetitionPattern());
  }

  private static LocalTime latest(LocalTime first, LocalTime second) {
    return first.isAfter(second) ? first : second;
  }

  private static LocalTime earliest(LocalTime first, LocalTime second) {
    return first.isBefore(second) ? first : second;
  }
}
