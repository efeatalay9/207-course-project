package usecase.addsection;

/**
 * Describes the result of an Add to Timetable action.
 *
 * @param outcome whether the timetable changed or why the section was not added
 * @param courseCode the course code, or an empty string when the offering could not be identified
 * @param sectionName the section name, or an empty string when no section could be identified
 */
public record AddSectionResponse(Outcome outcome, String courseCode, String sectionName) {
  /** The outcomes presented after an add request. */
  public enum Outcome {
    ADDED,
    ALREADY_SELECTED,
    NO_SELECTION,
    NOT_FOUND,
    CANCELLED,
    CATALOGUE_UNAVAILABLE
  }

  /**
   * Creates a result for a request whose section could not be identified.
   *
   * @param outcome the reason the section was not added
   */
  public AddSectionResponse(Outcome outcome) {
    this(outcome, "", "");
  }
}
