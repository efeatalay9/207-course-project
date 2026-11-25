package interfaceadapter.openplan;

import interfaceadapter.plan.PlanLabels;
import interfaceadapter.viewtimetable.TimetableViewModel;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import usecase.openplan.OpenPlanOutputBoundary;
import usecase.openplan.OpenPlanResponse;

/**
 * Writes the outcome of opening a plan into the timetable's status message.
 *
 * <p>The grid itself is redrawn by the view-timetable use case after the view refreshes.
 */
public final class OpenPlanPresenter implements OpenPlanOutputBoundary {
  private static final DateTimeFormatter SAVED_AT =
      DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a", Locale.ENGLISH);

  private final TimetableViewModel viewModel;
  private final ZoneId zone;

  /**
   * Creates the presenter.
   *
   * @param viewModel the view model shared with the timetable view
   * @param zone the time zone used to show when the plan was saved
   */
  public OpenPlanPresenter(TimetableViewModel viewModel, ZoneId zone) {
    this.viewModel = Objects.requireNonNull(viewModel, "viewModel");
    this.zone = Objects.requireNonNull(zone, "zone");
  }

  @Override
  public void presentOpened(OpenPlanResponse response) {
    viewModel.setStatus("Opened " + PlanLabels.fileName(response.location()) + " with "
        + PlanLabels.sections(response.sectionCount()) + ", saved "
        + SAVED_AT.format(response.savedAt().atZone(zone)) + ".");
  }

  @Override
  public void presentNoLocation() {
    viewModel.setStatus("Choose a plan file to open.");
  }

  @Override
  public void presentUnsupportedVersion(String location, int version) {
    viewModel.setStatus(PlanLabels.fileName(location) + " uses plan version " + version
        + ", which this app cannot open. Your current timetable is still open.");
  }

  @Override
  public void presentOpenFailed(String location, String reason) {
    viewModel.setStatus("Could not open " + PlanLabels.fileName(location) + ". " + reason
        + " Your current timetable is still open.");
  }
}
