package usecase.viewtimetable;

/** Defines the action of displaying the current weekly timetable. */
public interface ViewTimetableInputBoundary {
    /**
     * Collects the selected sections and their meetings, then sends them to the output boundary.
     *
     * @param request which academic session to display
     */
    void execute(ViewTimetableRequest request);
}
