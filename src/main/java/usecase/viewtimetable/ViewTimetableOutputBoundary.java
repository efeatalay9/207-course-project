package usecase.viewtimetable;

/** Receives the result of the view-timetable use case. */
public interface ViewTimetableOutputBoundary {
    /**
     * Presents the timetable for the chosen session.
     *
     * @param response the selected sections, available sessions, and timed meetings
     */
    void present(ViewTimetableResponse response);

    /**
     * Presents a failure to read the catalogue. The previous display should be replaced.
     *
     * @param message a short explanation of what failed
     */
    void presentFailure(String message);
}
