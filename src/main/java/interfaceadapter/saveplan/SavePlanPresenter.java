package interfaceadapter.saveplan;

import interfaceadapter.plan.PlanLabels;
import interfaceadapter.viewtimetable.TimetableViewModel;
import java.util.Objects;
import usecase.saveplan.SavePlanOutputBoundary;
import usecase.saveplan.SavePlanResponse;

/**
 * Writes the outcome of a save into the timetable's status message.
 *
 * <p>The status line is shared with the add and remove presenters, so every timetable action gives
 * feedback in the same place.
 */
public final class SavePlanPresenter implements SavePlanOutputBoundary {
  private final TimetableViewModel viewModel;

  /**
   * Creates the presenter.
   *
   * @param viewModel the view model shared with the timetable view
   */
  public SavePlanPresenter(TimetableViewModel viewModel) {
    this.viewModel = Objects.requireNonNull(viewModel, "viewModel");
  }

  @Override
  public void presentSaved(SavePlanResponse response) {
    viewModel.setStatus("Saved " + PlanLabels.sections(response.sectionCount()) + " to "
        + PlanLabels.fileName(response.location()) + ".");
  }

  @Override
  public void presentNothingToSave() {
    viewModel.setStatus("Add at least one section before saving a plan.");
  }

  @Override
  public void presentNoLocation() {
    viewModel.setStatus("Choose a file name before saving the plan.");
  }

  @Override
  public void presentSaveFailed(String location, String reason) {
    viewModel.setStatus("Could not save " + PlanLabels.fileName(location) + ". " + reason
        + " Your timetable is unchanged.");
  }
}
