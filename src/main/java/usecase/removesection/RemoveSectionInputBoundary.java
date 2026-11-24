package usecase.removesection;

/** Defines the action of removing a section from the timetable. */
public interface RemoveSectionInputBoundary {
    /**
     * Removes the requested section and reports the outcome to the output boundary.
     *
     * @param request the section to remove
     */
    void execute(RemoveSectionRequest request);
}
