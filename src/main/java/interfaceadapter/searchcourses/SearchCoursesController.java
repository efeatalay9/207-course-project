package interfaceadapter.searchcourses;

import usecase.common.CatalogueScope;
import usecase.searchcourses.SearchCoursesInputBoundary;
import usecase.searchcourses.SearchCoursesRequest;
import java.util.Objects;

// Converts UI actions to use-case requests; validation belongs to the interactor.
public final class SearchCoursesController {
    private final SearchCoursesInputBoundary input;

    // Accept any implementation of the input boundary, including test doubles.
    public SearchCoursesController(SearchCoursesInputBoundary input) {
        this.input = Objects.requireNonNull(input);
    }

    // Forward the original input so every UI uses the same normalization rules.
    public void search(String code) {
        input.execute(new SearchCoursesRequest(code));
    }

    /** Searches one selected term and division from the live catalogue. */
    public void search(String code, String session, String division) {
        input.execute(new SearchCoursesRequest(code, new CatalogueScope(session, division)));
    }
}
