package usecase.suggestcoursecodes;

// Keeps autocomplete independent of Swing and any particular dropdown widget.
public interface SuggestCourseCodesOutputBoundary {
    // Receives the complete matching set, or an explicit access failure.
    void present(SuggestCourseCodesResponse response);
}
