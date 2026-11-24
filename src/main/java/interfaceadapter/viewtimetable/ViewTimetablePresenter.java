package interfaceadapter.viewtimetable;

import interfaceadapter.overlaps.OverlapWarningFormatter;
import interfaceadapter.viewtimetable.TimetableViewModel.Block;
import interfaceadapter.viewtimetable.TimetableViewModel.SectionItem;
import interfaceadapter.viewtimetable.TimetableViewModel.SessionOption;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import usecase.overlaps.SectionOverlapResult;
import usecase.overlaps.SectionOverlapResult.Conflict;
import usecase.viewtimetable.ViewTimetableOutputBoundary;
import usecase.viewtimetable.ViewTimetableResponse;
import usecase.viewtimetable.ViewTimetableResponse.ScheduledMeeting;
import usecase.viewtimetable.ViewTimetableResponse.SectionStatus;
import usecase.viewtimetable.ViewTimetableResponse.SelectedSectionResult;

/**
 * Formats the view-timetable result into grid positions and display text.
 *
 * <p>The presenter decides which day columns and hour rows to show, so the Swing view only has to
 * draw rectangles where it is told.
 */
public final class ViewTimetablePresenter implements ViewTimetableOutputBoundary {
  private static final DateTimeFormatter TIME =
      DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
  private static final int DEFAULT_FIRST_HOUR = 9;
  private static final int DEFAULT_LAST_HOUR = 18;

  private final TimetableViewModel viewModel;

  /**
   * Creates the presenter.
   *
   * @param viewModel the view model shared with the timetable view
   */
  public ViewTimetablePresenter(TimetableViewModel viewModel) {
    this.viewModel = Objects.requireNonNull(viewModel, "viewModel");
  }

  @Override
  public void present(ViewTimetableResponse response) {
    List<DayOfWeek> days = days(response.meetings());
    List<String> dayLabels = new ArrayList<>();
    for (DayOfWeek day : days) {
      dayLabels.add(shortDay(day));
    }

    int firstHour = DEFAULT_FIRST_HOUR;
    int lastHour = DEFAULT_LAST_HOUR;
    List<Block> blocks = new ArrayList<>();
    for (ScheduledMeeting meeting : response.meetings()) {
      firstHour = Math.min(firstHour, meeting.start().getHour());
      lastHour = Math.max(lastHour, endHour(meeting.end()));
      blocks.add(
          block(meeting, days.indexOf(meeting.day()), warnings(meeting, response.overlaps())));
    }

    List<SessionOption> sessions = new ArrayList<>();
    for (String code : response.sessions()) {
      sessions.add(new SessionOption(code, sessionLabel(code)));
    }

    List<SectionItem> items = new ArrayList<>();
    List<String> notes = new ArrayList<>();
    for (SelectedSectionResult section : response.sections()) {
      items.add(
          new SectionItem(section.offeringId(), section.sectionName(), sectionLabel(section)));
      if (section.status() == SectionStatus.TIME_TBA) {
        notes.add(name(section) + " — Time TBA (not shown on the grid)");
      } else if (section.status() == SectionStatus.NOT_FOUND) {
        notes.add(name(section) + " — no longer in the catalogue");
      }
    }

    viewModel.update(
        new TimetableViewModel.State(
            sessions,
            response.selectedSession(),
            dayLabels,
            firstHour,
            lastHour,
            blocks,
            items,
            notes,
            summary(response)));
  }

  @Override
  public void presentFailure(String message) {
    viewModel.update(TimetableViewModel.State.empty("Could not load your timetable: " + message));
  }

  // Weekdays are always shown; weekend columns appear only when a meeting needs them.
  private List<DayOfWeek> days(List<ScheduledMeeting> meetings) {
    List<DayOfWeek> days =
        new ArrayList<>(
            List.of(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY));
    for (DayOfWeek weekend : List.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) {
      for (ScheduledMeeting meeting : meetings) {
        if (meeting.day() == weekend) {
          days.add(weekend);
          break;
        }
      }
    }
    return days;
  }

  private Block block(ScheduledMeeting meeting, int column, String warning) {
    StringBuilder detail =
        new StringBuilder()
            .append(TIME.format(meeting.start()))
            .append("–")
            .append(TIME.format(meeting.end()));
    if (!meeting.location().isBlank()) {
      detail.append(" · ").append(meeting.location());
    }
    String repetition = repetition(meeting.repetition());
    if (!repetition.isEmpty()) {
      detail.append(" · ").append(repetition);
    }
    return new Block(
        meeting.offeringId(),
        meeting.sectionName(),
        meeting.courseCode(),
        column,
        minutes(meeting.start()),
        minutes(meeting.end()),
        (warning.isEmpty() ? "" : "[!] ") + meeting.courseCode() + " " + meeting.sectionName(),
        detail.toString(),
        warning);
  }

  // Only decorate meetings that overlap; other meetings of the same section stay unflagged.
  private String warnings(ScheduledMeeting meeting, List<SectionOverlapResult> overlaps) {
    List<String> messages = new ArrayList<>();
    for (SectionOverlapResult result : overlaps) {
      if (!result.candidate().offeringId().equals(meeting.offeringId())
          || !result.candidate().sectionName().equals(meeting.sectionName())) {
        continue;
      }
      for (Conflict conflict : result.conflicts()) {
        domain.MeetingTime candidate = conflict.candidateMeeting();
        if (candidate.day() == meeting.day()
            && candidate.session().equals(meeting.session())
            && candidate.start().equals(meeting.start())
            && candidate.end().equals(meeting.end())) {
          messages.add(OverlapWarningFormatter.conflict(conflict));
        }
      }
    }
    return messages.stream().distinct().collect(java.util.stream.Collectors.joining(" "));
  }

  // Weekly meetings need no label; anything else is shown so the grid does not overstate
  // attendance.
  private String repetition(String raw) {
    return switch (raw) {
      case "", "WEEKLY" -> "";
      case "BI_WEEKLY" -> "alternate weeks";
      case "MANUAL" -> "irregular dates";
      default -> raw.toLowerCase(Locale.ENGLISH).replace('_', ' ');
    };
  }

  private String summary(ViewTimetableResponse response) {
    int count = response.sections().size();
    if (count == 0) {
      return "No sections selected yet. Search for a course and click Add to Timetable.";
    }
    String sections = count + (count == 1 ? " section selected" : " sections selected");
    if (response.selectedSession().isEmpty()) {
      return sections + " · none have scheduled meeting times";
    }
    return sections + " · showing " + sessionLabel(response.selectedSession());
  }

  private String sectionLabel(SelectedSectionResult section) {
    String label = name(section);
    if (!section.teachingMethod().isBlank()) {
      label += " (" + section.teachingMethod() + ")";
    }
    if (section.status() == SectionStatus.TIME_TBA) {
      label += " · Time TBA";
    } else if (section.status() == SectionStatus.NOT_FOUND) {
      label += " · unavailable";
    }
    return label;
  }

  private String name(SelectedSectionResult section) {
    String code = section.courseCode().isBlank() ? section.offeringId() : section.courseCode();
    return code + " " + section.sectionName();
  }

  // Session codes are year followed by start month, for example 20269 = September 2026.
  static String sessionLabel(String code) {
    if (code.matches("[0-9]{5,6}")) {
      String year = code.substring(0, 4);
      String month = code.substring(4);
      switch (month) {
        case "9":
        case "09":
          return "Fall " + year + " (" + code + ")";
        case "1":
        case "01":
          return "Winter " + year + " (" + code + ")";
        case "5":
        case "05":
          return "Summer " + year + " (" + code + ")";
        default:
          break;
      }
    }
    return "Session " + code;
  }

  private static String shortDay(DayOfWeek day) {
    String name = day.name();
    return name.charAt(0) + name.substring(1, 3).toLowerCase(Locale.ENGLISH);
  }

  private static int minutes(LocalTime time) {
    return time.getHour() * 60 + time.getMinute();
  }

  // A meeting ending at 17:30 needs the 17:00 row, so round up to the next full hour.
  private static int endHour(LocalTime end) {
    return end.getMinute() == 0 ? end.getHour() : end.getHour() + 1;
  }
}
