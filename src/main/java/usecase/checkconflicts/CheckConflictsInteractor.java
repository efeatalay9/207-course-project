package usecase.checkconflicts;

import domain.CourseOffering;
import domain.MeetingConflict;
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
import usecase.checkconflicts.CheckConflictsResponse.Conflict;
import usecase.checkconflicts.CheckConflictsResponse.Reason;
import usecase.checkconflicts.CheckConflictsResponse.SectionLabel;
import usecase.checkconflicts.CheckConflictsResponse.UncheckedSection;
import usecase.common.CatalogAccessException;
import usecase.viewtimetable.OfferingLookup;

/**
 * Finds every pair of selected meetings that overlap in one academic session.
 *
 * <p>The overlap rule itself lives in the {@link MeetingConflict} entity. This interactor only
 * gathers the meetings of the selected sections, compares each pair once, and reports sections
 * it could not check. It never changes the timetable.
 */
public final class CheckConflictsInteractor implements CheckConflictsInputBoundary {
  private static final Comparator<Conflict> ORDER =
      Comparator.comparing(Conflict::day)
          .thenComparing(Conflict::start)
          .thenComparing(conflict -> conflict.first().courseCode())
          .thenComparing(conflict -> conflict.first().sectionName());

  private final Timetable timetable;
  private final OfferingLookup offerings;
  private final CheckConflictsOutputBoundary output;

  /**
   * Creates the interactor.
   *
   * @param timetable the timetable shared with the other timetable use cases
   * @param offerings the catalogue port used to recover meeting times
   * @param output where the result is sent
   */
  public CheckConflictsInteractor(
      Timetable timetable, OfferingLookup offerings, CheckConflictsOutputBoundary output) {
    this.timetable = Objects.requireNonNull(timetable, "timetable");
    this.offerings = Objects.requireNonNull(offerings, "offerings");
    this.output = Objects.requireNonNull(output, "output");
  }

  @Override
  public void execute(CheckConflictsRequest request) {
    if (timetable.selections().isEmpty()) {
      output.presentNothingToCheck();
      return;
    }

    List<PlacedMeeting> meetings = new ArrayList<>();
    List<UncheckedSection> unchecked = new ArrayList<>();
    TreeSet<String> sessions = new TreeSet<>();
    try {
      for (SelectedSection selection : timetable.selections()) {
        collect(selection, meetings, unchecked, sessions);
      }
    } catch (CatalogAccessException exception) {
      output.presentFailure(exception.getMessage());
      return;
    }

    String requested = request == null ? null : request.sessionCode();
    String session = chooseSession(requested, sessions);
    output.present(new CheckConflictsResponse(session, findConflicts(meetings, session),
        unchecked));
  }

  // Compare each pair of meetings once; meetings of the same section never conflict.
  private static List<Conflict> findConflicts(List<PlacedMeeting> meetings, String session) {
    List<Conflict> conflicts = new ArrayList<>();
    for (int i = 0; i < meetings.size(); i++) {
      PlacedMeeting first = meetings.get(i);
      for (int j = i + 1; j < meetings.size(); j++) {
        PlacedMeeting second = meetings.get(j);
        if (first.time().session().equals(session) && !first.label().equals(second.label())) {
          Optional<MeetingConflict> clash = MeetingConflict.between(first.time(), second.time());
          clash.ifPresent(found -> conflicts.add(new Conflict(first.label(), second.label(),
              found.day(), found.start(), found.end(),
              found.certainty() == MeetingConflict.Certainty.CONFIRMED)));
        }
      }
    }
    conflicts.sort(ORDER);
    return conflicts;
  }

  private void collect(
      SelectedSection selection,
      List<PlacedMeeting> meetings,
      List<UncheckedSection> unchecked,
      TreeSet<String> sessions)
      throws CatalogAccessException {
    Optional<CourseOffering> offering = offerings.findById(selection.offeringId());
    Optional<Section> section =
        offering.flatMap(found -> found.findSection(selection.sectionName()));
    String code = offering.map(CourseOffering::code).orElse("");
    SectionLabel label = new SectionLabel(selection.offeringId(), code, selection.sectionName());

    if (section.isEmpty()) {
      unchecked.add(new UncheckedSection(label, Reason.NOT_FOUND));
      return;
    }
    if (section.get().meetings().isEmpty()) {
      unchecked.add(new UncheckedSection(label, Reason.TIME_TBA));
      return;
    }
    for (MeetingTime meeting : section.get().meetings()) {
      sessions.add(meeting.session());
      meetings.add(new PlacedMeeting(label, meeting));
    }
  }

  // Same rule as the weekly view: keep the requested session, else use the earliest one.
  private static String chooseSession(String requested, TreeSet<String> sessions) {
    if (requested != null && sessions.contains(requested)) {
      return requested;
    }
    return sessions.isEmpty() ? "" : sessions.first();
  }

  // A meeting together with the selected section it belongs to.
  private record PlacedMeeting(SectionLabel label, MeetingTime time) {}
}
