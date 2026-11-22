package usecase.searchcourses;

import usecase.common.CatalogAccessException;

import domain.CourseOffering;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;
import static support.CatalogFixtures.offering;

// Tests substitute both ports: neither JSON nor a UI is required for search logic.
class SearchCoursesInteractorTest {
    @Test
    void normalizesCodeAndReturnsEveryOfferingInStableOrder() {
        AtomicReference<String> query = new AtomicReference<String>();
        AtomicReference<SearchCoursesResponse> result = new AtomicReference<SearchCoursesResponse>();
        CourseCatalog catalog = code -> {
            query.set(code);
            return List.of(offering("winter", "CSC108H1", "20271"), offering("fall", "CSC108H1", "20269"));
        };
        new SearchCoursesInteractor(catalog, result::set).execute(new SearchCoursesRequest("  csc108h1 \n"));
        assertEquals("CSC108H1", query.get());
        assertEquals(SearchCoursesResponse.Outcome.SUCCESS, result.get().outcome());
        assertEquals(List.of("fall", "winter"), result.get().offerings().stream().map(OfferingResult::id).toList());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t\n", "\u2003"})
    void invalidInputDoesNotCallStorage(String code) {
        AtomicReference<SearchCoursesResponse> result = new AtomicReference<SearchCoursesResponse>();
        CourseCatalog unused = query -> { throw new AssertionError("Blank search queried storage"); };
        new SearchCoursesInteractor(unused, result::set).execute(new SearchCoursesRequest(code));
        assertEquals(SearchCoursesResponse.Outcome.INVALID_INPUT, result.get().outcome());
        assertTrue(result.get().offerings().isEmpty());
    }

    @Test
    void nullRequestIsInvalid() {
        AtomicReference<SearchCoursesResponse> result = new AtomicReference<SearchCoursesResponse>();
        new SearchCoursesInteractor(code -> { throw new AssertionError(); }, result::set).execute(null);
        assertEquals(SearchCoursesResponse.Outcome.INVALID_INPUT, result.get().outcome());
    }

    @Test
    void unknownCodeIsSuccessfulEmptySearch() {
        AtomicReference<SearchCoursesResponse> result = new AtomicReference<SearchCoursesResponse>();
        new SearchCoursesInteractor(code -> List.of(), result::set).execute(new SearchCoursesRequest("UNKNOWN"));
        assertEquals(SearchCoursesResponse.Outcome.SUCCESS, result.get().outcome());
        assertTrue(result.get().offerings().isEmpty());
    }

    @Test
    void accessFailureIsNotAnEmptySuccess() {
        AtomicReference<SearchCoursesResponse> result = new AtomicReference<SearchCoursesResponse>();
        CourseCatalog unavailable = code -> { throw new CatalogAccessException("Offline", new Exception()); };
        new SearchCoursesInteractor(unavailable, result::set).execute(new SearchCoursesRequest("CSC108H1"));
        assertEquals(SearchCoursesResponse.Outcome.CATALOGUE_FAILURE, result.get().outcome());
    }

    @Test
    void normalizationDoesNotDependOnComputerLocale() {
        Locale original = Locale.getDefault();
        AtomicReference<String> query = new AtomicReference<String>();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            new SearchCoursesInteractor(code -> { query.set(code); return List.of(); }, response -> { })
                    .execute(new SearchCoursesRequest("his100h1"));
            assertEquals("HIS100H1", query.get());
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    void responseCannotBeChangedByACaller() {
        AtomicReference<SearchCoursesResponse> result = new AtomicReference<SearchCoursesResponse>();
        List<CourseOffering> data = new java.util.ArrayList<>(List.of(offering("one", "CSC108H1", "20269")));
        new SearchCoursesInteractor(code -> data, result::set).execute(new SearchCoursesRequest("CSC108H1"));
        data.clear();
        assertEquals(1, result.get().offerings().size());
        assertThrows(UnsupportedOperationException.class, () -> result.get().offerings().clear());
    }
}
