package usecase.saveplan;

import domain.SavedPlan;
import domain.Timetable;
import java.time.Clock;
import java.util.Objects;
import usecase.plan.PlanAccessException;
import usecase.plan.PlanDataAccess;

/**
 * Saves a snapshot of the active timetable so the student can reopen it after a restart.
 *
 * <p>The interactor never changes the timetable, so a failed save leaves the active plan exactly
 * as it was.
 */
public final class SavePlanInteractor implements SavePlanInputBoundary {
  private final Timetable timetable;
  private final PlanDataAccess plans;
  private final SavePlanOutputBoundary output;
  private final Clock clock;

  /**
   * Creates the interactor.
   *
   * @param timetable the active timetable shared with the other timetable use cases
   * @param plans where plans are stored
   * @param output where the outcome is sent
   * @param clock supplies the save time
   */
  public SavePlanInteractor(
      Timetable timetable, PlanDataAccess plans, SavePlanOutputBoundary output, Clock clock) {
    this.timetable = Objects.requireNonNull(timetable, "timetable");
    this.plans = Objects.requireNonNull(plans, "plans");
    this.output = Objects.requireNonNull(output, "output");
    this.clock = Objects.requireNonNull(clock, "clock");
  }

  @Override
  public void execute(SavePlanRequest request) {
    if (request == null || request.location() == null || request.location().isBlank()) {
      output.presentNoLocation();
      return;
    }

    SavedPlan plan = SavedPlan.capture(timetable, clock.instant());
    if (plan.isEmpty()) {
      output.presentNothingToSave();
      return;
    }

    try {
      plans.save(request.location(), plan);
    } catch (PlanAccessException exception) {
      output.presentSaveFailed(request.location(), exception.getMessage());
      return;
    }
    output.presentSaved(
        new SavePlanResponse(request.location(), plan.selections().size(), plan.savedAt()));
  }
}
