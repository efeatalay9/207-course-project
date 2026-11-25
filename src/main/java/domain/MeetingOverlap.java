package domain;

/** Compares local meeting intervals without deciding whether a student may add a section. */
public final class MeetingOverlap {
  private MeetingOverlap() {}

  /**
   * Returns whether intervals overlap. End-to-start adjacency is allowed. Different known sessions
   * or weekdays cannot clash; missing or irregular recurrence requires review.
   */
  public static Kind compare(MeetingTime first, MeetingTime second) {
    if ((!first.session().isBlank()
            && !second.session().isBlank()
            && !first.session().equals(second.session()))
        || first.day() != second.day()
        || !first.start().isBefore(second.end())
        || !second.start().isBefore(first.end())) {
      return Kind.NONE;
    }
    return weekly(first) && weekly(second) ? Kind.CONFIRMED : Kind.POSSIBLE;
  }

  private static boolean weekly(MeetingTime meeting) {
    return !meeting.session().isBlank()
        && meeting.repetition().equals("WEEKLY")
        && meeting.repetitionPattern().equals("ONCE_A_WEEK");
  }

  /** A possible overlap has matching clock times but unverified occurrence dates. */
  public enum Kind {
    NONE,
    POSSIBLE,
    CONFIRMED
  }
}
