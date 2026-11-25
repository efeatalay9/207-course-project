package interfaceadapter.saveplan;

import java.util.Objects;
import usecase.saveplan.SavePlanInputBoundary;
import usecase.saveplan.SavePlanRequest;

/** Turns a Save Plan action in the view into a save-plan request. */
public final class SavePlanController {
  private final SavePlanInputBoundary useCase;

  /**
   * Creates the controller.
   *
   * @param useCase the save-plan input boundary
   */
  public SavePlanController(SavePlanInputBoundary useCase) {
    this.useCase = Objects.requireNonNull(useCase, "useCase");
  }

  /**
   * Requests that the active timetable be saved.
   *
   * @param location the file the student chose
   */
  public void save(String location) {
    useCase.execute(new SavePlanRequest(location));
  }
}
