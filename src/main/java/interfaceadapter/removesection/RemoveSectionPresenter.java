package interfaceadapter.removesection;

import interfaceadapter.viewtimetable.TimetableViewModel;
import interfaceadapter.viewtimetable.TimetableViewModel.SectionItem;
import java.util.Objects;
import usecase.removesection.RemoveSectionOutputBoundary;
import usecase.removesection.RemoveSectionResponse;

/**
 * Writes the outcome of a removal into the timetable's status message.
 *
 * <p>It does not redraw the grid itself; the view asks the view-timetable use case to refresh
 * after a removal, so the grid always comes from one place.
 */
public final class RemoveSectionPresenter implements RemoveSectionOutputBoundary {
    private final TimetableViewModel viewModel;

    /**
     * Creates the presenter.
     *
     * @param viewModel the view model shared with the timetable view
     */
    public RemoveSectionPresenter(TimetableViewModel viewModel) {
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel");
    }

    @Override
    public void presentRemoved(RemoveSectionResponse response) {
        viewModel.setStatus("Removed " + displayName(response) + " from your timetable.");
    }

    @Override
    public void presentNotSelected(RemoveSectionResponse response) {
        viewModel.setStatus(displayName(response) + " is not in your timetable.");
    }

    @Override
    public void presentNoSelection() {
        viewModel.setStatus("Choose a section in the list before clicking Remove.");
    }

    // Reuse the label already on screen (course code + section) when the section is listed.
    private String displayName(RemoveSectionResponse response) {
        for (SectionItem item : viewModel.state().sections()) {
            if (item.offeringId().equals(response.offeringId())
                    && item.sectionName().equals(response.sectionName())) {
                String label = item.label();
                int extra = label.indexOf(" (");
                return extra < 0 ? label : label.substring(0, extra);
            }
        }
        return response.sectionName();
    }
}
