package domain;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

// The timetable is the shared entity for add, view, and remove; these rules protect all three.
class TimetableTest {
    @Test
    void selectionsKeepTheOrderTheyWereAdded() {
        Timetable timetable = new Timetable();
        timetable.addSection("b", "TUT0101");
        timetable.addSection("a", "LEC0101");
        timetable.addSection("b", "LEC0101");
        assertEquals(List.of(new SelectedSection("b", "TUT0101"), new SelectedSection("b", "LEC0101"),
                new SelectedSection("a", "LEC0101")), timetable.selections());
    }

    @Test
    void removeDeletesOnlyTheChosenSection() {
        Timetable timetable = new Timetable();
        timetable.addSection("fall", "LEC0101");
        timetable.addSection("fall", "TUT0101");
        assertTrue(timetable.removeSection("fall", "LEC0101"));
        assertFalse(timetable.contains("fall", "LEC0101"));
        assertTrue(timetable.contains("fall", "TUT0101"));
        assertEquals(List.of(new SelectedSection("fall", "TUT0101")), timetable.selections());
    }

    @Test
    void sameSectionNameInAnotherOfferingIsADifferentSection() {
        Timetable timetable = new Timetable();
        timetable.addSection("fall", "LEC0101");
        timetable.addSection("winter", "LEC0101");
        assertTrue(timetable.removeSection("winter", "LEC0101"));
        assertEquals(List.of(new SelectedSection("fall", "LEC0101")), timetable.selections());
    }

    @Test
    void removingSomethingNotSelectedChangesNothing() {
        Timetable timetable = new Timetable();
        timetable.addSection("fall", "LEC0101");
        assertFalse(timetable.removeSection("fall", "TUT0101"));
        assertFalse(timetable.removeSection("missing", "LEC0101"));
        assertEquals(1, timetable.selections().size());
    }

    @Test
    void aRemovedSectionCanBeAddedAgain() {
        Timetable timetable = new Timetable();
        timetable.addSection("fall", "LEC0101");
        timetable.removeSection("fall", "LEC0101");
        assertTrue(timetable.selections().isEmpty());
        assertTrue(timetable.addSection("fall", "LEC0101"));
    }

    @Test
    void selectionsCannotBeModifiedByCallers() {
        Timetable timetable = new Timetable();
        timetable.addSection("fall", "LEC0101");
        assertThrows(UnsupportedOperationException.class,
                () -> timetable.selections().add(new SelectedSection("x", "y")));
    }
}
