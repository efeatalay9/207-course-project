package usecase.removesection;

/** Receives the outcome of a removal request. */
public interface RemoveSectionOutputBoundary {
    /**
     * Called when the section was in the timetable and has been removed.
     *
     * @param response the removed section
     */
    void presentRemoved(RemoveSectionResponse response);

    /**
     * Called when the request did not identify a selected section. The timetable is unchanged.
     *
     * @param response the section that was requested
     */
    void presentNotSelected(RemoveSectionResponse response);

    /** Called when no section was chosen, for example when Remove is pressed with nothing selected. */
    void presentNoSelection();
}
