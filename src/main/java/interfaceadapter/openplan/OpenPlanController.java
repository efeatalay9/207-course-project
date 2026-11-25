package interfaceadapter.openplan;

import java.util.Objects;
import usecase.openplan.OpenPlanInputBoundary;
import usecase.openplan.OpenPlanRequest;

/** Turns an Open Plan action in the view into an open-plan request. */
public final class OpenPlanController {
  private final OpenPlanInputBoundary useCase;

  /**
   * Creates the controller.
   *
   * @param useCase the open-plan input boundary
   */
  public OpenPlanController(OpenPlanInputBoundary useCase) {
    this.useCase = Objects.requireNonNull(useCase, "useCase");
  }

  /**
   * Requests that a saved plan replace the active timetable.
   *
   * @param location the file the student chose
   */
  public void open(String location) {
    useCase.execute(new OpenPlanRequest(location));
  }
}
