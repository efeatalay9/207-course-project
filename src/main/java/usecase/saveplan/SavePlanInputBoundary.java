package usecase.saveplan;

/** Defines the action of saving the active timetable as a plan. */
public interface SavePlanInputBoundary {
  /**
   * Saves the active timetable and reports the outcome to the output boundary.
   *
   * @param request where to save the plan
   */
  void execute(SavePlanRequest request);
}
