package interfaceadapter.suggestcoursecodes;

import java.util.Objects;
import usecase.common.CatalogueScope;
import usecase.suggestcoursecodes.SuggestCourseCodesInputBoundary;

/** Converts changes to the search field into autocomplete requests. */
public final class SuggestCourseCodesController {
  private final SuggestCourseCodesInputBoundary input;

  /**
   * Creates the autocomplete controller.
   *
   * @param input the boundary that accepts suggestion requests
   */
  public SuggestCourseCodesController(SuggestCourseCodesInputBoundary input) {
    this.input = Objects.requireNonNull(input);
  }

  /**
   * Requests suggestions from the offline catalogue.
   *
   * @param prefix the unmodified text in the search field
   */
  public void suggest(String prefix) {
    input.execute(prefix);
  }

  /**
   * Requests live suggestions within a selected term and division.
   *
   * @param prefix the unmodified text in the search field
   * @param session the selected term code
   * @param division the selected faculty or division code
   */
  public void suggest(String prefix, String session, String division) {
    input.execute(prefix, new CatalogueScope(session, division));
  }
}
