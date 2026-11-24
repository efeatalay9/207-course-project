package usecase.viewtimetable;

import domain.CourseOffering;
import domain.MeetingTime;
import domain.Section;
import domain.SelectedSection;
import domain.Timetable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
import usecase.common.CatalogAccessException;
import usecase.viewtimetable.ViewTimetableResponse.ScheduledMeeting;
import usecase.viewtimetable.ViewTimetableResponse.SectionStatus;
import usecase.viewtimetable.ViewTimetableResponse.SelectedSectionResult;

/**
 * Builds the weekly view of the current timetable.
 *
 * <p>For each selected section, the interactor looks up its offering, gathers its meetings, and
 * keeps only those in the requested academic session. Sections without known times are reported
 * separately so the grid never implies that they fit anywhere.
 */
public final class ViewTimetableInteractor implements ViewTimetableInputBoundary {
  private static final Comparator<ScheduledMeeting> MEETING_ORDER =
      Comparator.comparing(ScheduledMeeting::day)
          .thenComparing(ScheduledMeeting::start)
          .thenComparing(ScheduledMeeting::end)
          .thenComparing(ScheduledMeeting::courseCode)
          .thenComparing(ScheduledMeeting::sectionName);

  private final Timetable timetable;
  private final OfferingLookup offerings;
  private final ViewTimetableOutputBoundary output;

  /**
   * Creates the interactor.
   *
   * @param timetable the timetable shared with the add/remove use cases
   * @param offerings the catalogue port used to recover meeting times
   * @param output where the result is sent
   */
  public ViewTimetableInteractor(
      Timetable timetable, OfferingLookup offerings, ViewTimetableOutputBoundary output) {
    this.timetable = Objects.requireNonNull(timetable, "timetable");
    this.offerings = Objects.requireNonNull(offerings, "offerings");
    this.output = Objects.requireNonNull(output, "output");
  }

  @Override
  public void execute(ViewTimetableRequest request) {
    List<SelectedSectionResult> sections = new ArrayList<>();
    List<ScheduledMeeting> allMeetings = new ArrayList<>();
    TreeSet<String> sessions = new TreeSet<>();

    try {
      for (SelectedSection selection : timetable.selections()) {
        collect(selection, sections, allMeetings, sessions);
      }
    } catch (CatalogAccessException exception) {
      output.presentFailure(exception.getMessage());
      return;
    }

    String selectedSession = chooseSession(request.sessionCode(), sessions);
    List<ScheduledMeeting> shown = new ArrayList<>();
    for (ScheduledMeeting meeting : allMeetings) {
      if (meeting.session().equals(selectedSession)) {
        shown.add(meeting);
      }
    }
    shown.sort(MEETING_ORDER);

    output.present(
        new ViewTimetableResponse(List.copyOf(sessions), selectedSession, shown, sections));
  }

  // Looks up one selection and records its status and meetings.
  private void collect(
      SelectedSection selection,
      List<SelectedSectionResult> sections,
      List<ScheduledMeeting> meetings,
      TreeSet<String> sessions)
      throws CatalogAccessException {
    Optional<CourseOffering> offering = offerings.findById(selection.offeringId());
    Optional<Section> section =
        offering.flatMap(found -> found.findSection(selection.sectionName()));

    if (offering.isEmpty() || section.isEmpty()) {
      String code = offering.map(CourseOffering::code).orElse("");
      sections.add(
          new SelectedSectionResult(
              selection.offeringId(), code, selection.sectionName(), "", SectionStatus.NOT_FOUND));
      return;
    }

    String code = offering.get().code();
    Section found = section.get();
    SectionStatus status =
        found.meetings().isEmpty() ? SectionStatus.TIME_TBA : SectionStatus.SCHEDULED;
    sections.add(
        new SelectedSectionResult(
            selection.offeringId(), code, found.name(), found.teachingMethod(), status));

    for (MeetingTime meeting : found.meetings()) {
      sessions.add(meeting.session());
      meetings.add(
          new ScheduledMeeting(
              selection.offeringId(),
              code,
              found.name(),
              meeting.day(),
              meeting.start(),
              meeting.end(),
              meeting.session(),
              meeting.location(),
              meeting.repetition()));
    }
  }

  // Keep the requested session if it has meetings; otherwise fall back to the earliest one.
  private String chooseSession(String requested, TreeSet<String> sessions) {
    if (sessions.contains(requested)) {
      return requested;
    }
    return sessions.isEmpty() ? "" : sessions.first();
  }
}
