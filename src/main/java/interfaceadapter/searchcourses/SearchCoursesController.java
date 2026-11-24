package interfaceadapter.searchcourses;

import java.util.Objects;
import usecase.common.CatalogueScope;
import usecase.searchcourses.SearchCoursesInputBoundary;
import usecase.searchcourses.SearchCoursesRequest;

/** Converts course-search actions into input-boundary requests. */
public final class SearchCoursesController {
  private final SearchCoursesInputBoundary input;

  /**
   * Creates the search controller.
   *
   * @param input the boundary that accepts search requests
   */
  public SearchCoursesController(SearchCoursesInputBoundary input) {
    this.input = Objects.requireNonNull(input);
  }

  /**
   * Forwards the text for an offline search.
   *
   * @param code the course code as entered by the student
   */
  public void search(String code) {
    input.execute(new SearchCoursesRequest(code));
  }

  /**
   * Requests a live search within the selected term and division.
   *
   * @param code the course code as entered by the student
   * @param session the selected term code
   * @param division the selected faculty or division code
   */
  public void search(String code, String session, String division) {
    input.execute(new SearchCoursesRequest(code, new CatalogueScope(session, division)));
  }
}
