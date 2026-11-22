package app;

import dataaccess.catalog.InMemoryCourseCatalog;
import domain.CourseOffering;
import interfaceadapter.searchcourses.SearchCoursesPresenter;
import interfaceadapter.searchcourses.SearchCoursesViewModel;
import org.junit.jupiter.api.Test;
import usecase.searchcourses.CourseCatalog;
import usecase.searchcourses.SearchCoursesOutputBoundary;
import usecase.searchcourses.SearchCoursesRequest;
import usecase.searchcourses.SearchCoursesResponse;
import usecase.suggestcoursecodes.CourseCodeIndex;
import usecase.suggestcoursecodes.SuggestCourseCodesResponse;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;
import static support.CatalogFixtures.sample;

// Demonstrates actual replacement of catalogue and presentation modules through their contracts.
class SearchApplicationTest {
    @Test
    void visualizerCanConsumeStructuredResultsWithoutTheTextPresenter() throws Exception {
        InMemoryCourseCatalog catalog = new InMemoryCourseCatalog(sample());
        SearchCoursesViewModel textModel = new SearchCoursesViewModel();
        SearchApplication textApplication = SearchApplication.connect(catalog, catalog,
                new SearchCoursesPresenter(textModel), response -> { });
        RecordingVisualizer alternateVisualizer = new RecordingVisualizer();
        AtomicReference<SuggestCourseCodesResponse> suggestions = new AtomicReference<>();
        SearchApplication alternateApplication = SearchApplication.connect(catalog, catalog,
                alternateVisualizer, suggestions::set);

        textApplication.search().execute(new SearchCoursesRequest(" csc108h1 "));
        alternateApplication.search().execute(new SearchCoursesRequest(" csc108h1 "));
        alternateApplication.suggestions().execute("csc");

        assertEquals("2 offerings for CSC108H1", textModel.state().heading());
        assertEquals(SearchCoursesResponse.Outcome.SUCCESS, alternateVisualizer.response.outcome());
        assertEquals(2, alternateVisualizer.response.offerings().size());
        assertEquals(java.time.LocalTime.of(9, 0), alternateVisualizer.response.offerings().get(0)
                .sections().get(0).meetings().get(0).start());
        assertEquals(List.of("CSC108H1", "CSC207H1"), suggestions.get().codes());
    }

    @Test
    void independentCatalogueAdaptersPreserveSearchResults() throws Exception {
        List<CourseOffering> offerings = sample();
        InMemoryCourseCatalog indexedCatalog = new InMemoryCourseCatalog(offerings);
        // This intentionally different implementation exercises substitutability, not index internals.
        CourseCatalog linearCatalog = code -> offerings.stream()
                .filter(offering -> offering.code().equals(code)).toList();
        CourseCodeIndex linearIndex = prefix -> offerings.stream().map(CourseOffering::code)
                .filter(code -> !prefix.isEmpty() && code.startsWith(prefix)).distinct().sorted().toList();
        RecordingVisualizer indexedOutput = new RecordingVisualizer();
        RecordingVisualizer linearOutput = new RecordingVisualizer();
        AtomicReference<SuggestCourseCodesResponse> indexedSuggestions = new AtomicReference<>();
        AtomicReference<SuggestCourseCodesResponse> linearSuggestions = new AtomicReference<>();
        SearchApplication indexed = SearchApplication.connect(indexedCatalog, indexedCatalog,
                indexedOutput, indexedSuggestions::set);
        SearchApplication linear = SearchApplication.connect(linearCatalog, linearIndex,
                linearOutput, linearSuggestions::set);

        for (String query : List.of(" csc108h1 ", "CSC207H1", "UNKNOWN", "")) {
            indexed.search().execute(new SearchCoursesRequest(query));
            linear.search().execute(new SearchCoursesRequest(query));
            assertEquals(indexedOutput.response, linearOutput.response);
        }
        for (String prefix : List.of("csc", " csc1 ", "ZZZ", "")) {
            indexed.suggestions().execute(prefix);
            linear.suggestions().execute(prefix);
            assertEquals(indexedSuggestions.get(), linearSuggestions.get());
        }
    }

    // Represents a new renderer that stores typed results, without Swing or formatted text.
    private static final class RecordingVisualizer implements SearchCoursesOutputBoundary {
        private SearchCoursesResponse response;

        @Override
        public void present(SearchCoursesResponse response) {
            this.response = response;
        }
    }
}
