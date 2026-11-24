package usecase.common;

/** The term and division chosen before searching the live timetable catalogue. */
public record CatalogueScope(String session, String division) {
    public CatalogueScope {
        if (session == null || session.isBlank() || division == null || division.isBlank()) {
            throw new IllegalArgumentException("Choose a session and division.");
        }
    }
}
