package interfaceadapter.searchcourses;

import dataaccess.catalog.InMemoryCourseCatalog;
import org.junit.jupiter.api.Test;
import usecase.searchcourses.SearchCoursesInteractor;
import usecase.searchcourses.SearchCoursesResponse;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;
import static support.CatalogFixtures.sample;

// Verify meaningful display behaviour rather than exact whitespace or the whole rendered string.
class SearchCoursesPresenterTest {
    @Test
    void displaysBothOfferingsMeetingDetailsAndUnknownTimes() throws Exception {
        SearchCoursesViewModel model = new SearchCoursesViewModel();
        controller(model).search(" csc108h1 ");
        assertEquals("2 offerings for CSC108H1", model.state().heading());
        String details = model.state().details();
        for (String expected : List.of("Sep 2026", "Jan 2027", "St. George", "LEC0101", "TUT0101",
                "Monday 09:00–10:00", "Wednesday", "BA 1200", "Location unknown", "Weekly",
                "BI_WEEKLY / FIRST_AND_THIRD_WEEK", "Linked sections: LEC0101", "Times to be announced",
                "Meeting times unknown", "Section CANCELLED")) {
            assertTrue(details.contains(expected), "Missing display detail: " + expected);
        }
    }

    @Test
    void showsManualRecurrenceAndOfferingCancellationWithoutInventingDates() throws Exception {
        SearchCoursesViewModel model = new SearchCoursesViewModel();
        controller(model).search("CSC207H1");
        assertTrue(model.state().details().contains("Offering CANCELLED"));
        assertTrue(model.state().details().contains("Manual schedule; exact dates unavailable"));
        assertTrue(model.state().details().contains("TBA status unknown"));
        assertTrue(model.state().details().contains("cancellation status unknown"));
    }

    @Test
    void emptyAndInvalidSearchesReplacePreviousResults() throws Exception {
        SearchCoursesViewModel model = new SearchCoursesViewModel();
        SearchCoursesController controller = controller(model);
        controller.search("CSC108H1");
        controller.search("UNKNOWN");
        assertEquals("No matching offerings", model.state().heading());
        assertFalse(model.state().details().contains("LEC0101"));
        controller.search("   ");
        assertEquals("Enter a course code", model.state().heading());
        assertFalse(model.state().details().contains("UNKNOWN"));
    }

    @Test
    void catalogueFailureHasItsOwnMessageAndClearsPreviousResults() throws Exception {
        SearchCoursesViewModel model = new SearchCoursesViewModel();
        controller(model).search("CSC108H1");
        new SearchCoursesPresenter(model).present(new SearchCoursesResponse(
                SearchCoursesResponse.Outcome.CATALOGUE_FAILURE, "CSC108H1", List.of()));
        assertEquals("Catalogue unavailable", model.state().heading());
        assertFalse(model.state().details().contains("LEC0101"));
    }

    @Test
    void controllerPassesOriginalInputToUseCase() {
        AtomicReference<String> received = new AtomicReference<>();
        new SearchCoursesController(request -> received.set(request.courseCode())).search("  csc108h1 ");
        assertEquals("  csc108h1 ", received.get());
    }

    // Connect the real adapters around the small sample catalogue.
    private SearchCoursesController controller(SearchCoursesViewModel model) throws Exception {
        return new SearchCoursesController(new SearchCoursesInteractor(new InMemoryCourseCatalog(sample()),
                new SearchCoursesPresenter(model)));
    }
}
