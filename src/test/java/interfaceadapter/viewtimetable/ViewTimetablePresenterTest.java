package interfaceadapter.viewtimetable;

import interfaceadapter.viewtimetable.TimetableViewModel.Block;
import org.junit.jupiter.api.Test;
import usecase.viewtimetable.ViewTimetableResponse;
import usecase.viewtimetable.ViewTimetableResponse.ScheduledMeeting;
import usecase.viewtimetable.ViewTimetableResponse.SectionStatus;
import usecase.viewtimetable.ViewTimetableResponse.SelectedSectionResult;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ViewTimetablePresenterTest {
    private final TimetableViewModel model = new TimetableViewModel();
    private final ViewTimetablePresenter presenter = new ViewTimetablePresenter(model);

    private static ScheduledMeeting meeting(DayOfWeek day, int startHour, int startMinute, int endHour,
                                            int endMinute, String repetition) {
        return new ScheduledMeeting("fall", "CSC207H1", "LEC0101", day, LocalTime.of(startHour, startMinute),
                LocalTime.of(endHour, endMinute), "20269", "BA1160", repetition);
    }

    private static SelectedSectionResult section(String name, SectionStatus status) {
        return new SelectedSectionResult("fall", "CSC207H1", name, "LEC", status);
    }

    @Test
    void placesBlocksByDayColumnAndMinutes() {
        presenter.present(new ViewTimetableResponse(List.of("20269"), "20269",
                List.of(meeting(DayOfWeek.WEDNESDAY, 10, 30, 12, 0, "WEEKLY")),
                List.of(section("LEC0101", SectionStatus.SCHEDULED))));
        TimetableViewModel.State state = model.state();
        assertEquals(List.of("Mon", "Tue", "Wed", "Thu", "Fri"), state.dayLabels());
        Block block = state.blocks().get(0);
        assertEquals(2, block.column());
        assertEquals(630, block.startMinute());
        assertEquals(720, block.endMinute());
        assertEquals("CSC207H1 LEC0101", block.title());
        assertEquals("10:30–12:00 · BA1160", block.detail());
        assertEquals("1 section selected · showing Fall 2026 (20269)", state.summary());
    }

    @Test
    void addsWeekendColumnsAndExtendsHoursOnlyWhenNeeded() {
        presenter.present(new ViewTimetableResponse(List.of("20269"), "20269",
                List.of(meeting(DayOfWeek.SATURDAY, 8, 0, 9, 0, "WEEKLY"),
                        meeting(DayOfWeek.MONDAY, 20, 0, 21, 30, "BI_WEEKLY")),
                List.of(section("LEC0101", SectionStatus.SCHEDULED))));
        TimetableViewModel.State state = model.state();
        assertEquals("Sat", state.dayLabels().get(5));
        assertEquals(5, state.blocks().get(0).column());
        assertEquals(8, state.firstHour());
        assertEquals(22, state.lastHour());
        assertTrue(state.blocks().get(1).detail().endsWith("alternate weeks"));
    }

    @Test
    void listsTbaAndMissingSectionsAsNotes() {
        presenter.present(new ViewTimetableResponse(List.of(), "", List.of(),
                List.of(section("LEC0201", SectionStatus.TIME_TBA),
                        new SelectedSectionResult("gone", "", "TUT0101", "", SectionStatus.NOT_FOUND))));
        TimetableViewModel.State state = model.state();
        assertEquals(List.of("CSC207H1 LEC0201 — Time TBA (not shown on the grid)",
                "gone TUT0101 — no longer in the catalogue"), state.notes());
        assertEquals("CSC207H1 LEC0201 (LEC) · Time TBA", state.sections().get(0).label());
        assertEquals("2 sections selected · none have scheduled meeting times", state.summary());
        assertTrue(state.blocks().isEmpty());
    }

    @Test
    void emptyTimetableExplainsHowToStart() {
        presenter.present(new ViewTimetableResponse(List.of(), "", List.of(), List.of()));
        assertTrue(model.state().summary().startsWith("No sections selected yet"));
    }

    @Test
    void failureReplacesTheGrid() {
        presenter.present(new ViewTimetableResponse(List.of("20269"), "20269",
                List.of(meeting(DayOfWeek.MONDAY, 9, 0, 10, 0, "WEEKLY")),
                List.of(section("LEC0101", SectionStatus.SCHEDULED))));
        presenter.presentFailure("disk error");
        assertTrue(model.state().blocks().isEmpty());
        assertEquals("Could not load your timetable: disk error", model.state().summary());
    }

    @Test
    void labelsSessionCodes() {
        assertEquals("Winter 2027 (20271)", ViewTimetablePresenter.sessionLabel("20271"));
        assertEquals("Summer 2026 (20265)", ViewTimetablePresenter.sessionLabel("20265"));
        assertEquals("Session odd", ViewTimetablePresenter.sessionLabel("odd"));
    }
}
