package usecase.viewtimetable;

import dataaccess.catalog.InMemoryOfferingLookup;
import domain.Timetable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import support.CatalogFixtures;
import usecase.common.CatalogAccessException;
import usecase.viewtimetable.ViewTimetableResponse.ScheduledMeeting;
import usecase.viewtimetable.ViewTimetableResponse.SectionStatus;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

// Uses the small fictional sample: CSC108 fall (LEC0101 Mon/Wed 9-10, TUT0101 Fri 11-12),
// CSC108 winter (LEC0201 TBA, LEC9901 Tue 14-15), and CSC207 fall (PRA0101 Thu 13-15).
class ViewTimetableInteractorTest {
    private final AtomicReference<ViewTimetableResponse> result = new AtomicReference<>();
    private final AtomicReference<String> failure = new AtomicReference<>();
    private final ViewTimetableOutputBoundary output = new ViewTimetableOutputBoundary() {
        @Override
        public void present(ViewTimetableResponse response) {
            result.set(response);
        }

        @Override
        public void presentFailure(String message) {
            failure.set(message);
        }
    };
    private Timetable timetable;
    private OfferingLookup lookup;

    @BeforeEach
    void setUp() throws Exception {
        timetable = new Timetable();
        lookup = new InMemoryOfferingLookup(CatalogFixtures.sample());
    }

    private ViewTimetableResponse view(String session) {
        new ViewTimetableInteractor(timetable, lookup, output).execute(new ViewTimetableRequest(session));
        assertNull(failure.get());
        return result.get();
    }

    @Test
    void emptyTimetableGivesAnEmptyGrid() {
        ViewTimetableResponse response = view("");
        assertTrue(response.sections().isEmpty());
        assertTrue(response.meetings().isEmpty());
        assertTrue(response.sessions().isEmpty());
        assertEquals("", response.selectedSession());
    }

    @Test
    void everyMeetingOfASelectedSectionAppearsInDayAndTimeOrder() {
        timetable.addSection("sample-csc207-fall", "PRA0101");
        timetable.addSection("sample-csc108-fall", "LEC0101");
        ViewTimetableResponse response = view("");
        assertEquals("20269", response.selectedSession());
        List<ScheduledMeeting> meetings = response.meetings();
        assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY),
                meetings.stream().map(ScheduledMeeting::day).toList());
        assertEquals(LocalTime.of(9, 0), meetings.get(0).start());
        assertEquals(LocalTime.of(10, 0), meetings.get(0).end());
        assertEquals("CSC108H1", meetings.get(0).courseCode());
        assertEquals("CSC207H1", meetings.get(2).courseCode());
    }

    @Test
    void onlyTheRequestedSessionIsShownButAllSectionsAreListed() {
        timetable.addSection("sample-csc108-fall", "LEC0101");
        timetable.addSection("sample-csc108-winter", "LEC9901");
        ViewTimetableResponse winter = view("20271");
        assertEquals(List.of("20269", "20271"), winter.sessions());
        assertEquals("20271", winter.selectedSession());
        assertEquals(1, winter.meetings().size());
        assertEquals(DayOfWeek.TUESDAY, winter.meetings().get(0).day());
        assertEquals(2, winter.sections().size());
    }

    @Test
    void unknownRequestedSessionFallsBackToTheEarliest() {
        timetable.addSection("sample-csc108-winter", "LEC9901");
        timetable.addSection("sample-csc108-fall", "TUT0101");
        assertEquals("20269", view("19991").selectedSession());
    }

    @Test
    void sectionWithoutMeetingTimesIsListedAsTbaAndKeptOffTheGrid() {
        timetable.addSection("sample-csc108-winter", "LEC0201");
        ViewTimetableResponse response = view("");
        assertEquals(SectionStatus.TIME_TBA, response.sections().get(0).status());
        assertTrue(response.meetings().isEmpty());
        assertEquals("", response.selectedSession());
    }

    @Test
    void sectionMissingFromTheCatalogueIsReportedNotDropped() {
        timetable.addSection("gone", "LEC0101");
        timetable.addSection("sample-csc108-fall", "LEC9999");
        ViewTimetableResponse response = view("");
        assertEquals(List.of(SectionStatus.NOT_FOUND, SectionStatus.NOT_FOUND),
                response.sections().stream().map(ViewTimetableResponse.SelectedSectionResult::status).toList());
        assertEquals("CSC108H1", response.sections().get(1).courseCode());
    }

    @Test
    void catalogueFailureIsReportedSeparately() {
        timetable.addSection("sample-csc108-fall", "LEC0101");
        OfferingLookup broken = id -> {
            throw new CatalogAccessException("Catalogue unavailable", new IOException("disk"));
        };
        new ViewTimetableInteractor(timetable, broken, output).execute(new ViewTimetableRequest(""));
        assertEquals("Catalogue unavailable", failure.get());
        assertNull(result.get());
    }
}
