package usecase.saveplan;

/** Receives the outcome of a save request. */
public interface SavePlanOutputBoundary {
  /**
   * Called when the plan was written.
   *
   * @param response the saved plan's details
   */
  void presentSaved(SavePlanResponse response);

  /** Called when the timetable is empty, so there is nothing worth saving. */
  void presentNothingToSave();

  /** Called when no location was given. */
  void presentNoLocation();

  /**
   * Called when the plan could not be written. The active timetable is unchanged.
   *
   * @param location the requested location
   * @param reason a short explanation for the student
   */
  void presentSaveFailed(String location, String reason);
}
