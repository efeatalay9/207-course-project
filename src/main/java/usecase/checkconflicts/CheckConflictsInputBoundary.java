package usecase.checkconflicts;

/** Defines the action of checking the timetable for overlapping meetings. */
public interface CheckConflictsInputBoundary {
  /**
   * Checks one session of the timetable and reports the result to the output boundary.
   *
   * @param request the session to check
   */
  void execute(CheckConflictsRequest request);
}
