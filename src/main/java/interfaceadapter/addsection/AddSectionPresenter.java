package interfaceadapter.addsection;

import interfaceadapter.viewtimetable.TimetableViewModel;
import java.util.Objects;
import usecase.addsection.AddSectionOutputBoundary;
import usecase.addsection.AddSectionResponse;

/** Turns add results into feedback shown beside the weekly timetable. */
public final class AddSectionPresenter implements AddSectionOutputBoundary {
  private final TimetableViewModel viewModel;

  /**
   * Creates the presenter for the timetable panel.
   *
   * @param viewModel the display model shared with the timetable panel
   */
  public AddSectionPresenter(TimetableViewModel viewModel) {
    this.viewModel = Objects.requireNonNull(viewModel, "viewModel");
  }

  @Override
  public void present(AddSectionResponse response) {
    String section = response.courseCode() + " " + response.sectionName();
    String message =
        switch (response.outcome()) {
          case ADDED -> "Added " + section + " to your timetable.";
          case ALREADY_SELECTED -> section + " is already in your timetable.";
          case NO_SELECTION -> "Choose a section from the search results first.";
          case NOT_FOUND -> "That section is no longer in the catalogue. Search again to refresh"
              + " it.";
          case CANCELLED -> section + " has been cancelled.";
          case CATALOGUE_UNAVAILABLE -> "The catalogue is unavailable. Try adding the section"
              + " again.";
        };
    viewModel.setStatus(message);
  }
}
