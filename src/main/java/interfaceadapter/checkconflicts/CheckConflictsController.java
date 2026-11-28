package interfaceadapter.checkconflicts;

import java.util.Objects;
import usecase.checkconflicts.CheckConflictsInputBoundary;
import usecase.checkconflicts.CheckConflictsRequest;

/** Turns a Check Conflicts action in the view into a use-case request. */
public final class CheckConflictsController {
  private final CheckConflictsInputBoundary useCase;

  /**
   * Creates the controller.
   *
   * @param useCase the check-conflicts input boundary
   */
  public CheckConflictsController(CheckConflictsInputBoundary useCase) {
    this.useCase = Objects.requireNonNull(useCase, "useCase");
  }

  /**
   * Requests a conflict check.
   *
   * @param sessionCode the session shown in the timetable, or empty for the earliest one
   */
  public void check(String sessionCode) {
    useCase.execute(new CheckConflictsRequest(sessionCode == null ? "" : sessionCode));
  }
}
