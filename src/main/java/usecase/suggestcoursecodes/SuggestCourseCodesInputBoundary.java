package usecase.suggestcoursecodes;

import usecase.common.CatalogueScope;

// Lets input adapters request suggestions without depending on the implementation.
public interface SuggestCourseCodesInputBoundary {
    // Accepts raw text and publishes one response; blank text clears suggestions.
    void execute(String prefix);

    default void execute(String prefix, CatalogueScope scope) {
        execute(prefix);
    }
}
