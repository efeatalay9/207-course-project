package interfaceadapter.suggestcoursecodes;

import dataaccess.catalog.InMemoryCourseCatalog;
import org.junit.jupiter.api.Test;
import usecase.suggestcoursecodes.SuggestCourseCodesInteractor;
import usecase.suggestcoursecodes.SuggestCourseCodesResponse;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static support.CatalogFixtures.sample;

// Exercise the real autocomplete path and presentation without requiring a desktop display.
class SuggestCourseCodesAdaptersTest {
    @Test
    void partialTypingAndDeletionReplaceTheDropdownContents() throws Exception {
        SuggestCourseCodesViewModel model = new SuggestCourseCodesViewModel();
        SuggestCourseCodesController controller = new SuggestCourseCodesController(new SuggestCourseCodesInteractor(
                new InMemoryCourseCatalog(sample()), new SuggestCourseCodesPresenter(model)));
        controller.suggest(" csc ");
        assertEquals(List.of("CSC108H1", "CSC207H1"), model.state().codes());
        assertTrue(model.state().message().contains("2 course codes"));
        controller.suggest("csc1");
        assertEquals(List.of("CSC108H1"), model.state().codes());
        controller.suggest("csc");
        assertEquals(2, model.state().codes().size());
        controller.suggest("ZZZ");
        assertTrue(model.state().codes().isEmpty());
        assertEquals("No matching course codes", model.state().message());
        controller.suggest("");
        assertTrue(model.state().codes().isEmpty());
    }

    @Test
    void failureClearsOldSuggestionsAndExplainsTheDifference() {
        SuggestCourseCodesViewModel model = new SuggestCourseCodesViewModel();
        SuggestCourseCodesPresenter presenter = new SuggestCourseCodesPresenter(model);
        presenter.present(new SuggestCourseCodesResponse(List.of("CSC108H1"), false));
        presenter.present(new SuggestCourseCodesResponse(List.of(), true));
        assertTrue(model.state().codes().isEmpty());
        assertTrue(model.state().message().contains("Suggestions unavailable"));
    }
}
