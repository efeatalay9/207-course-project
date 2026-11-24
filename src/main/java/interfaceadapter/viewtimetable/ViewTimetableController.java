package interfaceadapter.viewtimetable;

import java.util.Objects;
import usecase.viewtimetable.ViewTimetableInputBoundary;
import usecase.viewtimetable.ViewTimetableRequest;

/** Turns "show the timetable" actions from the view into use-case requests. */
public final class ViewTimetableController {
    private final ViewTimetableInputBoundary useCase;

    /**
     * Creates the controller.
     *
     * @param useCase the view-timetable input boundary
     */
    public ViewTimetableController(ViewTimetableInputBoundary useCase) {
        this.useCase = Objects.requireNonNull(useCase, "useCase");
    }

    /**
     * Requests the timetable for a session.
     *
     * @param sessionCode the session to show, or empty to show the earliest available one
     */
    public void view(String sessionCode) {
        useCase.execute(new ViewTimetableRequest(sessionCode));
    }
}
