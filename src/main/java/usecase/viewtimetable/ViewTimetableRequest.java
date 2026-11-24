package usecase.viewtimetable;

/**
 * Asks for the weekly timetable of one academic session.
 *
 * @param sessionCode the session to display, such as 20269; blank or null means
 *                    "choose the first session that has meetings"
 */
public record ViewTimetableRequest(String sessionCode) {
    /** Normalizes a missing session to an empty string. */
    public ViewTimetableRequest {
        sessionCode = sessionCode == null ? "" : sessionCode.trim();
    }
}
