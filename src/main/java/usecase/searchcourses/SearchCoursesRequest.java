package usecase.searchcourses;

import usecase.common.CatalogueScope;

/**
 * The text entered by the student and the optional scope for live search.
 *
 * @param courseCode the raw input, which may be blank
 * @param scope the selected term and division, or null for offline search
 */
public record SearchCoursesRequest(String courseCode, CatalogueScope scope) {
  /**
   * Creates an offline search request.
   *
   * @param courseCode the raw course code entered by the student
   */
  public SearchCoursesRequest(String courseCode) {
    this(courseCode, null);
  }
}
