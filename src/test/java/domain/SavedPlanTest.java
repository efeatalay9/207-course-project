package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SavedPlanTest {
  private static final Instant SAVED = Instant.parse("2026-11-24T23:00:00Z");

  @Test
  void captureCopiesSelectionsAndIgnoresLaterChanges() {
    Timetable timetable = new Timetable();
    timetable.addSection("csc207-fall", "LEC0101");
    timetable.addSection("csc207-fall", "TUT0201");

    SavedPlan plan = SavedPlan.capture(timetable, SAVED);
    timetable.removeSection("csc207-fall", "TUT0201");

    assertEquals(SavedPlan.CURRENT_VERSION, plan.version());
    assertEquals(SAVED, plan.savedAt());
    assertEquals(List.of(new SelectedSection("csc207-fall", "LEC0101"),
        new SelectedSection("csc207-fall", "TUT0201")), plan.selections());
    assertTrue(plan.isSupported());
    assertFalse(plan.isEmpty());
  }

  @Test
  void restoreReplacesEverySelectionInOrder() {
    Timetable timetable = new Timetable();
    timetable.addSection("mat137-year", "LEC0101");
    SavedPlan plan = new SavedPlan(1, SAVED, List.of(
        new SelectedSection("csc207-fall", "TUT0201"),
        new SelectedSection("csc207-fall", "LEC0101")));

    plan.restoreInto(timetable);

    assertEquals(plan.selections(), timetable.selections());
    assertFalse(timetable.contains("mat137-year", "LEC0101"));
  }

  @Test
  void restoringAnEmptyPlanClearsTheTimetable() {
    Timetable timetable = new Timetable();
    timetable.addSection("csc207-fall", "LEC0101");
    SavedPlan empty = new SavedPlan(1, SAVED, List.of());

    empty.restoreInto(timetable);

    assertTrue(empty.isEmpty());
    assertTrue(timetable.selections().isEmpty());
  }

  @Test
  void selectionsCannotBeChangedFromOutside() {
    List<SelectedSection> source = new ArrayList<>();
    source.add(new SelectedSection("csc207-fall", "LEC0101"));
    SavedPlan plan = new SavedPlan(1, SAVED, source);
    source.clear();

    assertEquals(1, plan.selections().size());
    assertThrows(UnsupportedOperationException.class, () -> plan.selections().clear());
  }

  @Test
  void otherVersionsAreKeptButNotSupported() {
    SavedPlan future = new SavedPlan(2, SAVED, List.of());
    assertEquals(2, future.version());
    assertFalse(future.isSupported());
  }

  @Test
  void rejectsInvalidSnapshots() {
    assertThrows(IllegalArgumentException.class, () -> new SavedPlan(0, SAVED, List.of()));
    assertThrows(NullPointerException.class, () -> new SavedPlan(1, null, List.of()));
    assertThrows(NullPointerException.class, () -> new SavedPlan(1, SAVED, null));
    assertThrows(NullPointerException.class, () -> SavedPlan.capture(null, SAVED));
    SavedPlan plan = new SavedPlan(1, SAVED, List.of());
    assertThrows(NullPointerException.class, () -> plan.restoreInto(null));
  }
}
