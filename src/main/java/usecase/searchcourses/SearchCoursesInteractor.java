package usecase.searchcourses;

import domain.CourseOffering;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import usecase.common.CatalogAccessException;

/** Normalizes the requested course code and publishes the search outcome. */
public final class SearchCoursesInteractor implements SearchCoursesInputBoundary {
  private final CourseCatalog catalog;
  private final SearchCoursesOutputBoundary output;

  /**
   * Creates the course-search use case.
   *
   * @param catalog supplies offerings for a normalized code and scope
   * @param output receives the result of each search
   */
  public SearchCoursesInteractor(CourseCatalog catalog, SearchCoursesOutputBoundary output) {
    this.catalog = Objects.requireNonNull(catalog);
    this.output = Objects.requireNonNull(output);
  }

  // A blank request never reaches storage. Access failure is distinct from no matches.
  @Override
  public void execute(SearchCoursesRequest request) {
    String code =
        request == null || request.courseCode() == null
            ? ""
            : request.courseCode().strip().toUpperCase(Locale.ROOT);
    if (code.isEmpty()) {
      output.present(
          new SearchCoursesResponse(SearchCoursesResponse.Outcome.INVALID_INPUT, code, List.of()));
      return;
    }

    List<CourseOffering> matches;
    try {
      matches = catalog.findByCode(code, request.scope());
    } catch (CatalogAccessException exception) {
      output.present(
          new SearchCoursesResponse(
              SearchCoursesResponse.Outcome.CATALOGUE_FAILURE, code, List.of()));
      return;
    }

    // Session, campus, term, and ID give consistent results even if storage order changes.
    List<OfferingResult> results =
        matches.stream()
            .map(OfferingResult::from)
            .sorted(
                Comparator.comparing(
                        (OfferingResult offering) -> String.join(",", offering.sessions()))
                    .thenComparing(OfferingResult::campus)
                    .thenComparing(OfferingResult::termDesignation)
                    .thenComparing(OfferingResult::id))
            .toList();
    output.present(new SearchCoursesResponse(SearchCoursesResponse.Outcome.SUCCESS, code, results));
  }
}
