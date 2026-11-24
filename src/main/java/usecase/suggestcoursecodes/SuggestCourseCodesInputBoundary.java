package usecase.suggestcoursecodes;

import usecase.common.CatalogueScope;

/** Accepts student input for autocomplete suggestions. */
public interface SuggestCourseCodesInputBoundary {
  /**
   * Requests suggestions for a course-code prefix.
   *
   * @param prefix the raw text, with blank text clearing suggestions
   */
  void execute(String prefix);

  /**
   * Requests suggestions for a selected term and division. The default forwards only the prefix.
   *
   * @param prefix the raw text
   * @param scope the selected term and division
   */
  default void execute(String prefix, CatalogueScope scope) {
    execute(prefix);
  }
}
