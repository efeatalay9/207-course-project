package domain;

import org.junit.jupiter.api.Test;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

// Domain invariants are independent of storage and presentation.
class DomainTest {
    @Test
    void copiesCollectionsAtEveryMutableBoundary() {
        List<MeetingTime> meetings = new ArrayList<MeetingTime>();
        List<LinkedSection> links = new ArrayList<LinkedSection>();
        Section section = new Section("LEC0101", "LEC", StatusFlag.NO, StatusFlag.UNKNOWN, meetings, links);
        List<Section> sections = new ArrayList<>(List.of(section));
        List<String> sessions = new ArrayList<>(List.of("20269"));
        CourseOffering offering = new CourseOffering("id", "CSC108H1", "Title", "Campus", sessions,
                "F", StatusFlag.NO, sections);
        meetings.add(meeting(LocalTime.of(9, 0), LocalTime.of(10, 0)));
        links.add(new LinkedSection("TUT", "0101"));
        sections.clear();
        sessions.clear();
        assertEquals(1, offering.sections().size());
        assertEquals(List.of("20269"), offering.sessions());
        assertTrue(section.meetings().isEmpty());
        assertTrue(section.linkedSections().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> offering.sections().clear());
        assertThrows(UnsupportedOperationException.class, () -> section.meetings().clear());
    }

    @Test
    void rejectsZeroLengthAndBackwardsMeetings() {
        assertThrows(IllegalArgumentException.class, () -> meeting(LocalTime.NOON, LocalTime.NOON));
        assertThrows(IllegalArgumentException.class, () -> meeting(LocalTime.NOON, LocalTime.of(11, 0)));
    }

    @Test
    void rejectsMissingIdentity() {
        assertThrows(IllegalArgumentException.class, () -> new CourseOffering("", "CSC108H1", "", "",
                List.of(), "", StatusFlag.UNKNOWN, List.of()));
        assertThrows(IllegalArgumentException.class, () -> new Section(" ", "LEC", StatusFlag.NO,
                StatusFlag.NO, List.of(), List.of()));
        assertThrows(IllegalArgumentException.class, () -> new LinkedSection("LEC", ""));
    }

    // Construct a meeting without optional metadata so interval validation is isolated.
    private MeetingTime meeting(LocalTime start, LocalTime end) {
        return new MeetingTime(DayOfWeek.MONDAY, start, end, "20269", "", "", "");
    }
}
