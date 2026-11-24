package usecase.common;

/**
 * The filter choices required for a live timetable query.
 *
 * @param session the term code provided by the catalogue
 * @param division the faculty or division code provided by the catalogue
 */
public record CatalogueScope(String session, String division) {
  /** Rejects missing or blank filter codes. */
  public CatalogueScope {
    if (session == null || session.isBlank() || division == null || division.isBlank()) {
      throw new IllegalArgumentException("Choose a session and division.");
    }
  }
}
