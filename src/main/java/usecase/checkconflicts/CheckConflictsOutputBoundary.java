package usecase.checkconflicts;

/** Receives the outcome of a conflict check. */
public interface CheckConflictsOutputBoundary {
  /**
   * Called with the overlaps and the sections that could not be checked. Both lists may be
   * empty, which means the session has no overlaps.
   *
   * @param response the result of the check
   */
  void present(CheckConflictsResponse response);

  /** Called when the timetable has no sections, so there is nothing to check. */
  void presentNothingToCheck();

  /**
   * Called when the catalogue could not be read.
   *
   * @param message a short explanation for the student
   */
  void presentFailure(String message);
}
