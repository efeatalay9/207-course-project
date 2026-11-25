package usecase.openplan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import domain.SavedPlan;
import domain.SelectedSection;
import domain.Timetable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import support.InMemoryPlanDataAccess;
import usecase.plan.PlanAccessException;

class OpenPlanInteractorTest {
  private static final Instant SAVED = Instant.parse("2026-11-20T15:30:00Z");
  private static final List<SelectedSection> SAVED_SECTIONS = List.of(
      new SelectedSection("csc207-fall", "LEC0101"),
      new SelectedSection("csc207-fall", "TUT0201"));

  private final InMemoryPlanDataAccess plans = new InMemoryPlanDataAccess();
  private final Timetable timetable = new Timetable();
  private final List<String> events = new ArrayList<>();
  private final OpenPlanOutputBoundary output = new OpenPlanOutputBoundary() {
    @Override
    public void presentOpened(OpenPlanResponse response) {
      events.add("opened " + response.location() + " " + response.sectionCount() + " "
          + response.savedAt());
    }

    @Override
    public void presentNoLocation() {
      events.add("no location");
    }

    @Override
    public void presentUnsupportedVersion(String location, int version) {
      events.add("unsupported " + location + " v" + version);
    }

    @Override
    public void presentOpenFailed(String location, String reason) {
      events.add("failed " + location + ": " + reason);
    }
  };
  private final OpenPlanInteractor interactor = new OpenPlanInteractor(timetable, plans, output);

  @Test
  void openingSavedPlanReplacesTheActiveTimetable() {
    timetable.addSection("mat137-year", "LEC0101");
    plans.put("fall.json", new SavedPlan(1, SAVED, SAVED_SECTIONS));

    interactor.execute(new OpenPlanRequest("fall.json"));

    assertEquals(List.of("opened fall.json 2 2026-11-20T15:30:00Z"), events);
    assertEquals(SAVED_SECTIONS, timetable.selections());
  }

  @Test
  void missingOrBrokenFileKeepsTheCurrentPlan() {
    timetable.addSection("mat137-year", "LEC0101");

    interactor.execute(new OpenPlanRequest("missing.json"));
    plans.failWith(new PlanAccessException("The file is not a valid timetable plan.",
        new IllegalStateException("not json")));
    interactor.execute(new OpenPlanRequest("broken.json"));

    assertEquals(List.of("failed missing.json: The plan file does not exist.",
        "failed broken.json: The file is not a valid timetable plan."), events);
    assertEquals(List.of(new SelectedSection("mat137-year", "LEC0101")), timetable.selections());
  }

  @Test
  void unsupportedVersionKeepsTheCurrentPlan() {
    timetable.addSection("mat137-year", "LEC0101");
    plans.put("future.json", new SavedPlan(2, SAVED, SAVED_SECTIONS));

    interactor.execute(new OpenPlanRequest("future.json"));

    assertEquals(List.of("unsupported future.json v2"), events);
    assertEquals(List.of(new SelectedSection("mat137-year", "LEC0101")), timetable.selections());
  }

  @Test
  void missingLocationIsReported() {
    interactor.execute(null);
    interactor.execute(new OpenPlanRequest(null));
    interactor.execute(new OpenPlanRequest(""));

    assertEquals(List.of("no location", "no location", "no location"), events);
  }

  @Test
  void requiresItsCollaborators() {
    assertThrows(NullPointerException.class, () -> new OpenPlanInteractor(null, plans, output));
    assertThrows(NullPointerException.class,
        () -> new OpenPlanInteractor(timetable, null, output));
    assertThrows(NullPointerException.class,
        () -> new OpenPlanInteractor(timetable, plans, null));
  }
}
