package usecase.overlaps;

import domain.MeetingTime;
import domain.SelectedSection;
import java.util.List;

/** Read-only warnings for a candidate section; these never grant or deny an add operation. */
public record SectionOverlapResult(
    SelectedSection candidate, List<Conflict> conflicts, boolean incomplete) {
  /** Copies conflicts so callers cannot change the result. */
  public SectionOverlapResult {
    conflicts = List.copyOf(conflicts);
  }

  /** Identifies both meetings so any presenter can explain or highlight the affected interval. */
  public record Conflict(
      MeetingTime candidateMeeting,
      String existingCourseCode,
      SelectedSection existingSection,
      MeetingTime existingMeeting,
      boolean confirmed) {}
}
