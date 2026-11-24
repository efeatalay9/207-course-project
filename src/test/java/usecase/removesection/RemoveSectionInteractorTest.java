package usecase.removesection;

import domain.Timetable;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class RemoveSectionInteractorTest {
    private final List<String> events = new ArrayList<>();
    private final RemoveSectionOutputBoundary output = new RemoveSectionOutputBoundary() {
        @Override
        public void presentRemoved(RemoveSectionResponse response) {
            events.add("removed " + response.offeringId() + "/" + response.sectionName());
        }

        @Override
        public void presentNotSelected(RemoveSectionResponse response) {
            events.add("not selected " + response.offeringId() + "/" + response.sectionName());
        }

        @Override
        public void presentNoSelection() {
            events.add("no selection");
        }
    };

    @Test
    void removesASelectedSection() {
        Timetable timetable = new Timetable();
        timetable.addSection("fall", "LEC0101");
        timetable.addSection("fall", "TUT0101");
        new RemoveSectionInteractor(timetable, output).execute(new RemoveSectionRequest("fall", "LEC0101"));
        assertEquals(List.of("removed fall/LEC0101"), events);
        assertFalse(timetable.contains("fall", "LEC0101"));
        assertTrue(timetable.contains("fall", "TUT0101"));
    }

    @Test
    void reportsASectionThatIsNotSelected() {
        Timetable timetable = new Timetable();
        timetable.addSection("fall", "LEC0101");
        new RemoveSectionInteractor(timetable, output).execute(new RemoveSectionRequest("fall", "TUT0101"));
        assertEquals(List.of("not selected fall/TUT0101"), events);
        assertTrue(timetable.contains("fall", "LEC0101"));
    }

    @Test
    void missingSelectionDoesNotTouchTheTimetable() {
        Timetable timetable = new Timetable();
        timetable.addSection("fall", "LEC0101");
        RemoveSectionInteractor interactor = new RemoveSectionInteractor(timetable, output);
        interactor.execute(new RemoveSectionRequest(null, null));
        interactor.execute(new RemoveSectionRequest("fall", "  "));
        interactor.execute(null);
        assertEquals(List.of("no selection", "no selection", "no selection"), events);
        assertEquals(1, timetable.selections().size());
    }

    @Test
    void removingTwiceReportsTheSecondAttemptAsNotSelected() {
        Timetable timetable = new Timetable();
        timetable.addSection("fall", "LEC0101");
        RemoveSectionInteractor interactor = new RemoveSectionInteractor(timetable, output);
        interactor.execute(new RemoveSectionRequest("fall", "LEC0101"));
        interactor.execute(new RemoveSectionRequest("fall", "LEC0101"));
        assertEquals(List.of("removed fall/LEC0101", "not selected fall/LEC0101"), events);
    }
}
