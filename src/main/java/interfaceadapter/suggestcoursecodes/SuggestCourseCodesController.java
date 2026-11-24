package interfaceadapter.suggestcoursecodes;

import usecase.common.CatalogueScope;
import usecase.suggestcoursecodes.SuggestCourseCodesInputBoundary;
import java.util.Objects;

// Forwards typing events through the autocomplete input boundary.
public final class SuggestCourseCodesController {
    private final SuggestCourseCodesInputBoundary input;

    // The concrete interactor is selected in the composition root.
    public SuggestCourseCodesController(SuggestCourseCodesInputBoundary input) {
        this.input = Objects.requireNonNull(input);
    }

    // Preserve raw text; normalization remains a use-case responsibility.
    public void suggest(String prefix) {
        input.execute(prefix);
    }

    /** Finds live suggestions within the selected term and division. */
    public void suggest(String prefix, String session, String division) {
        input.execute(prefix, new CatalogueScope(session, division));
    }
}
