package usecase.checkconflicts;

/**
 * Says which academic session the student wants checked.
 *
 * @param sessionCode the session code, or empty to check the earliest session with meetings
 */
public record CheckConflictsRequest(String sessionCode) {}
