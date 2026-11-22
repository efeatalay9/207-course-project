package interfaceadapter.suggestcoursecodes;

import usecase.suggestcoursecodes.SuggestCourseCodesOutputBoundary;
import usecase.suggestcoursecodes.SuggestCourseCodesResponse;
import java.util.Objects;

// Chooses autocomplete labels without knowledge of the Swing popup implementation.
public final class SuggestCourseCodesPresenter implements SuggestCourseCodesOutputBoundary {
    private final SuggestCourseCodesViewModel viewModel;

    // Shares the display state with whichever view is used.
    public SuggestCourseCodesPresenter(SuggestCourseCodesViewModel viewModel) {
        this.viewModel = Objects.requireNonNull(viewModel);
    }

    // Differentiate unavailable suggestions from a prefix with no matching codes.
    @Override
    public void present(SuggestCourseCodesResponse response) {
        int count = response.codes().size();
        String message = response.unavailable() ? "Suggestions unavailable; you can still try an exact search"
                : count == 0 ? "No matching course codes"
                : count + (count == 1 ? " course code" : " course codes") + " — ↑/↓ to choose, Enter to search";
        viewModel.update(new SuggestCourseCodesViewModel.State(response.codes(), message));
    }
}
