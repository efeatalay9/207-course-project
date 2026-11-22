package usecase.searchcourses;

import domain.CourseOffering;
import domain.LinkedSection;
import domain.MeetingTime;
import domain.Section;
import domain.StatusFlag;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

// Immutable data crossing the output boundary; no domain behaviour or UI formatting.
public record OfferingResult(String id, String code, String title, String campus,
                             List<String> sessions, String termDesignation,
                             StatusFlag cancelled, List<SectionResult> sections) {
    // Preserve independent read-only snapshots of nested results.
    public OfferingResult {
        sessions = List.copyOf(sessions);
        sections = List.copyOf(sections);
    }

    // Convert domain data and give sections and meetings a stable display order.
    public static OfferingResult from(CourseOffering offering) {
        List<SectionResult> sections = offering.sections().stream()
                .sorted(Comparator.comparing(Section::name))
                .map(OfferingResult::sectionResult).toList();
        return new OfferingResult(offering.id(), offering.code(), offering.title(), offering.campus(),
                offering.sessions().stream().sorted().toList(), offering.termDesignation(),
                offering.cancelled(), sections);
    }

    // Copy one section, keeping its meeting order independent of the storage adapter's order.
    private static SectionResult sectionResult(Section section) {
        List<MeetingResult> meetings = section.meetings().stream()
                .sorted(Comparator.comparing(MeetingTime::session)
                        .thenComparing(MeetingTime::day)
                        .thenComparing(MeetingTime::start)
                        .thenComparing(MeetingTime::end)
                        .thenComparing(MeetingTime::repetition)
                        .thenComparing(MeetingTime::repetitionPattern)
                        .thenComparing(MeetingTime::location))
                .map(OfferingResult::meetingResult).toList();
        List<LinkResult> links = section.linkedSections().stream()
                .map((LinkedSection link) -> new LinkResult(link.teachingMethod(), link.sectionNumber())).toList();
        return new SectionResult(section.name(), section.teachingMethod(), section.cancelled(),
                section.timeToBeAnnounced(), meetings, links);
    }

    // Preserve typed time and recurrence data for any future graphical or textual renderer.
    private static MeetingResult meetingResult(MeetingTime meeting) {
        return new MeetingResult(meeting.day(), meeting.start(), meeting.end(), meeting.session(),
                meeting.location(), meeting.repetition(), meeting.repetitionPattern());
    }

    // Section data, including links that future timetable selection will need.
    public record SectionResult(String name, String teachingMethod, StatusFlag cancelled,
                                StatusFlag timeToBeAnnounced, List<MeetingResult> meetings,
                                List<LinkResult> links) {
        public SectionResult {
            meetings = List.copyOf(meetings);
            links = List.copyOf(links);
        }
    }

    // Raw time and recurrence values; the presenter chooses human-readable labels.
    public record MeetingResult(DayOfWeek day, LocalTime start, LocalTime end, String session,
                                String location, String repetition, String repetitionPattern) { }

    // A link remains a reference, not a recursively expanded section.
    public record LinkResult(String teachingMethod, String sectionNumber) { }
}
