package interfaceadapter.checkconflicts;

import interfaceadapter.checkconflicts.ConflictsViewModel.Level;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import usecase.checkconflicts.CheckConflictsOutputBoundary;
import usecase.checkconflicts.CheckConflictsResponse;
import usecase.checkconflicts.CheckConflictsResponse.Conflict;
import usecase.checkconflicts.CheckConflictsResponse.Reason;
import usecase.checkconflicts.CheckConflictsResponse.SectionLabel;
import usecase.checkconflicts.CheckConflictsResponse.UncheckedSection;

/** Turns a conflict check result into short lines a student can read at a glance. */
public final class CheckConflictsPresenter implements CheckConflictsOutputBoundary {
  private static final DateTimeFormatter TIME =
      DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);

  private final ConflictsViewModel viewModel;

  /**
   * Creates the presenter.
   *
   * @param viewModel where the formatted result is stored for the view
   */
  public CheckConflictsPresenter(ConflictsViewModel viewModel) {
    this.viewModel = Objects.requireNonNull(viewModel, "viewModel");
  }

  @Override
  public void present(CheckConflictsResponse response) {
    List<String> conflicts = new ArrayList<>();
    int confirmed = 0;
    for (Conflict conflict : response.conflicts()) {
      conflicts.add(line(conflict));
      if (conflict.confirmed()) {
        confirmed++;
      }
    }
    int possible = response.conflicts().size() - confirmed;

    List<String> notes = new ArrayList<>();
    if (possible > 0) {
      notes.add("Lines marked \"check\" overlap in time, but their repeat pattern is unusual. "
          + "Confirm the weeks on ACORN.");
    }
    for (UncheckedSection section : response.unchecked()) {
      notes.add(label(section.section()) + (section.reason() == Reason.TIME_TBA
          ? ": times not announced yet, so it was not checked."
          : ": no longer in the catalogue, so it was not checked."));
    }

    String session = sessionLabel(response.session());
    Level level;
    String heading;
    if (confirmed > 0) {
      level = Level.WARNING;
      heading = count(confirmed, "overlap") + " found in " + session + ".";
    } else if (possible > 0 || !notes.isEmpty()) {
      level = Level.REVIEW;
      heading = possible > 0
          ? count(possible, "possible overlap") + " in " + session + "."
          : "No overlaps found in " + session + ", but some sections were not checked.";
    } else {
      level = Level.CLEAR;
      heading = "No overlaps in " + session + ". Back-to-back classes are fine.";
    }
    viewModel.update(new ConflictsViewModel.State(level, heading, conflicts, notes));
  }

  @Override
  public void presentNothingToCheck() {
    viewModel.update(new ConflictsViewModel.State(Level.NONE,
        "Add sections to your timetable before checking for overlaps.", List.of(), List.of()));
  }

  @Override
  public void presentFailure(String message) {
    viewModel.update(new ConflictsViewModel.State(Level.ERROR,
        "Could not check for overlaps. " + message, List.of(), List.of()));
  }

  // For example: "Mon 10:00–10:30 · CSC207H1 LEC0101 and MAT137Y1 TUT0102 (30 min)".
  private static String line(Conflict conflict) {
    long minutes = Duration.between(conflict.start(), conflict.end()).toMinutes();
    return shortDay(conflict.day()) + " " + TIME.format(conflict.start()) + "–"
        + TIME.format(conflict.end()) + " · " + label(conflict.first()) + " and "
        + label(conflict.second()) + " (" + minutes + " min)"
        + (conflict.confirmed() ? "" : " — check");
  }

  private static String label(SectionLabel section) {
    return section.courseCode().isEmpty()
        ? section.sectionName()
        : section.courseCode() + " " + section.sectionName();
  }

  private static String shortDay(DayOfWeek day) {
    return day.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
  }

  private static String count(int number, String noun) {
    return number + " " + noun + (number == 1 ? "" : "s");
  }

  // Session codes are the year followed by the start month, for example 20269 is Fall 2026.
  static String sessionLabel(String code) {
    if (code.matches("[0-9]{5,6}")) {
      String year = code.substring(0, 4);
      switch (Integer.parseInt(code.substring(4))) {
        case 9:
          return "Fall " + year;
        case 1:
          return "Winter " + year;
        case 5:
          return "Summer " + year;
        default:
          break;
      }
    }
    return code.isEmpty() ? "your timetable" : "session " + code;
  }
}
