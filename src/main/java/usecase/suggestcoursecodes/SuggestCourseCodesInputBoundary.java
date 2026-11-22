package usecase.suggestcoursecodes;

// Lets input adapters request suggestions without depending on the implementation.
public interface SuggestCourseCodesInputBoundary {
    // Accepts raw text and publishes one response; blank text clears suggestions.
    void execute(String prefix);
}
