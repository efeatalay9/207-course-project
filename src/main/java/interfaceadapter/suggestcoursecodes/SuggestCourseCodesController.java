package interfaceadapter.suggestcoursecodes;

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
}
