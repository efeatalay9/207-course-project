package usecase.saveplan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import domain.SelectedSection;
import domain.Timetable;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import support.InMemoryPlanDataAccess;
import usecase.plan.PlanAccessException;

class SavePlanInteractorTest {
  private static final Instant NOW = Instant.parse("2026-11-24T23:00:00Z");
  private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
  private final InMemoryPlanDataAccess plans = new InMemoryPlanDataAccess();
  private final Timetable timetable = new Timetable();
  private final List<String> events = new ArrayList<>();
  private final SavePlanOutputBoundary output = new SavePlanOutputBoundary() {
    @Override
    public void presentSaved(SavePlanResponse response) {
      events.add("saved " + response.location() + " " + response.sectionCount() + " "
          + response.savedAt());
    }

    @Override
    public void presentNothingToSave() {
      events.add("nothing to save");
    }

    @Override
    public void presentNoLocation() {
      events.add("no location");
    }

    @Override
    public void presentSaveFailed(String location, String reason) {
      events.add("failed " + location + ": " + reason);
    }
  };
  private final SavePlanInteractor interactor =
      new SavePlanInteractor(timetable, plans, output, clock);

  @Test
  void savesTheSelectedSectionsWithTheCurrentTime() {
    timetable.addSection("csc207-fall", "LEC0101");
    timetable.addSection("csc207-fall", "TUT0201");

    interactor.execute(new SavePlanRequest("plans/fall.json"));

    assertEquals(List.of("saved plans/fall.json 2 2026-11-24T23:00:00Z"), events);
    assertEquals(List.of(new SelectedSection("csc207-fall", "LEC0101"),
        new SelectedSection("csc207-fall", "TUT0201")), plans.get("plans/fall.json").selections());
    assertEquals(NOW, plans.get("plans/fall.json").savedAt());
  }

  @Test
  void emptyTimetableIsNotSaved() {
    interactor.execute(new SavePlanRequest("plans/empty.json"));

    assertEquals(List.of("nothing to save"), events);
    assertNull(plans.get("plans/empty.json"));
  }

  @Test
  void missingLocationIsReported() {
    timetable.addSection("csc207-fall", "LEC0101");

    interactor.execute(null);
    interactor.execute(new SavePlanRequest(null));
    interactor.execute(new SavePlanRequest("   "));

    assertEquals(List.of("no location", "no location", "no location"), events);
  }

  @Test
  void writeFailureLeavesTheTimetableUnchanged() {
    timetable.addSection("csc207-fall", "LEC0101");
    plans.failWith(new PlanAccessException("Could not write the plan file.",
        new IOException("disk full")));

    interactor.execute(new SavePlanRequest("plans/fall.json"));

    assertEquals(List.of("failed plans/fall.json: Could not write the plan file."), events);
    assertEquals(List.of(new SelectedSection("csc207-fall", "LEC0101")), timetable.selections());
  }

  @Test
  void requiresItsCollaborators() {
    assertThrows(NullPointerException.class,
        () -> new SavePlanInteractor(null, plans, output, clock));
    assertThrows(NullPointerException.class,
        () -> new SavePlanInteractor(timetable, null, output, clock));
    assertThrows(NullPointerException.class,
        () -> new SavePlanInteractor(timetable, plans, null, clock));
    assertThrows(NullPointerException.class,
        () -> new SavePlanInteractor(timetable, plans, output, null));
  }
}
