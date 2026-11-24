package usecase.addsection;

/** Defines the action of adding a section to a timetable. */
public interface AddSectionInputBoundary {
  /**
   * Validates the selection, updates the timetable when it can be added, and reports the result.
   *
   * @param request the section the student selected
   */
  void execute(AddSectionRequest request);
}
