package usecase.checkconflicts;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

/**
 * The result of checking one session for overlapping meetings.
 *
 * @param session the session that was checked, or empty when no section has known times
 * @param conflicts each overlapping pair of meetings, ordered by day and time
 * @param unchecked selected sections whose times could not be compared
 */
public record CheckConflictsResponse(
    String session, List<Conflict> conflicts, List<UncheckedSection> unchecked) {
  /** Copies the lists so later changes cannot alter the result. */
  public CheckConflictsResponse {
    Objects.requireNonNull(session, "session");
    conflicts = List.copyOf(conflicts);
    unchecked = List.copyOf(unchecked);
  }

  /**
   * Reports whether there is at least one definite clash.
   *
   * @return true when a confirmed conflict was found
   */
  public boolean hasConfirmedConflict() {
    return conflicts.stream().anyMatch(Conflict::confirmed);
  }

  /**
   * One section taking part in a conflict.
   *
   * @param offeringId the offering that owns the section
   * @param courseCode the course code shown to the student
   * @param sectionName the section name
   */
  public record SectionLabel(String offeringId, String courseCode, String sectionName) {}

  /**
   * Two meetings that overlap.
   *
   * @param first the earlier-listed section
   * @param second the other section
   * @param day the day they overlap
   * @param start when the shared time starts
   * @param end when the shared time ends
   * @param confirmed true for a definite clash, false when the student should double-check
   */
  public record Conflict(
      SectionLabel first,
      SectionLabel second,
      DayOfWeek day,
      LocalTime start,
      LocalTime end,
      boolean confirmed) {}

  /**
   * A selected section whose meetings could not be compared.
   *
   * @param section the section
   * @param reason why it could not be checked
   */
  public record UncheckedSection(SectionLabel section, Reason reason) {}

  /** Why a section was left out of the check. */
  public enum Reason {
    /** The section has no announced meeting times. */
    TIME_TBA,
    /** The catalogue no longer has the offering or section. */
    NOT_FOUND
  }
}
