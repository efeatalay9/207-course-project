package usecase.addsection;

/** Receives the result of adding a section to the timetable. */
public interface AddSectionOutputBoundary {
  /**
   * Publishes the outcome of one Add to Timetable action.
   *
   * @param response the outcome and the section's display fields when known
   */
  void present(AddSectionResponse response);
}
