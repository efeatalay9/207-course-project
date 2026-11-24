package interfaceadapter.overlaps;

import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import usecase.overlaps.SectionOverlapResult;
import usecase.overlaps.SectionOverlapResult.Conflict;

/** Formats the same warning for search rows and timetable blocks without depending on Swing. */
public final class OverlapWarningFormatter {
  private static final DateTimeFormatter TIME =
      DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);

  private OverlapWarningFormatter() {}

  /** Names the other meeting and distinguishes confirmed and possible overlap. */
  public static String conflict(Conflict conflict) {
    String certainty = conflict.confirmed() ? "Overlaps with " : "Possible overlap with ";
    return certainty
        + conflict.existingCourseCode()
        + " "
        + conflict.existingSection().sectionName()
        + " on "
        + conflict.existingMeeting().day().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        + " "
        + TIME.format(conflict.existingMeeting().start())
        + "–"
        + TIME.format(conflict.existingMeeting().end())
        + " (session "
        + conflict.existingMeeting().session()
        + ")"
        + (conflict.confirmed() ? "." : "; recurrence or timing needs verification.");
  }

  /** Combines all conflicts and any missing-data notice for one section. */
  public static String section(SectionOverlapResult result) {
    String warnings =
        result.conflicts().stream()
            .map(OverlapWarningFormatter::conflict)
            .distinct()
            .collect(java.util.stream.Collectors.joining("\n"));
    if (result.incomplete()) {
      warnings +=
          (warnings.isEmpty() ? "" : "\n")
              + "Overlap check incomplete: a section is missing or has unknown meeting times.";
    }
    return warnings;
  }

  /** Produces a visible text flag independent of the view colour. */
  public static String label(SectionOverlapResult result) {
    if (result.conflicts().stream().anyMatch(Conflict::confirmed)) {
      return "[OVERLAP] ";
    }
    if (!result.conflicts().isEmpty()) {
      return "[POSSIBLE OVERLAP] ";
    }
    return result.incomplete() ? "[CHECK TIMES] " : "";
  }
}
