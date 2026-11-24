package usecase.suggestcoursecodes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import usecase.common.CatalogAccessException;
import usecase.common.CatalogueScope;

// The autocomplete use case can run without Swing or the concrete sorted index.
class SuggestCourseCodesInteractorTest {
  @Test
  void normalizesPartialInputAndPublishesTheFullIndexResult() {
    AtomicReference<String> query = new AtomicReference<String>();
    AtomicReference<SuggestCourseCodesResponse> result =
        new AtomicReference<SuggestCourseCodesResponse>();
    List<String> codes = new ArrayList<>(List.of("CSC108H1", "CSC148H1", "CSC207H1"));
    CourseCodeIndex index =
        prefix -> {
          query.set(prefix);
          return codes;
        };
    new SuggestCourseCodesInteractor(index, result::set).execute("  csc \n");
    assertEquals("CSC", query.get());
    assertEquals(codes, result.get().codes());
    assertFalse(result.get().unavailable());
    codes.clear();
    assertEquals(3, result.get().codes().size());
    assertThrows(UnsupportedOperationException.class, () -> result.get().codes().clear());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"  ", "\t\n", "\u2003"})
  void blankInputClearsSuggestionsWithoutQueryingStorage(String prefix) {
    AtomicReference<SuggestCourseCodesResponse> result =
        new AtomicReference<SuggestCourseCodesResponse>();
    new SuggestCourseCodesInteractor(
            query -> {
              throw new AssertionError("Unexpected lookup");
            },
            result::set)
        .execute(prefix);
    assertTrue(result.get().codes().isEmpty());
    assertFalse(result.get().unavailable());
  }

  @Test
  void distinguishesUnknownPrefixFromStorageFailure() {
    AtomicReference<SuggestCourseCodesResponse> result =
        new AtomicReference<SuggestCourseCodesResponse>();
    new SuggestCourseCodesInteractor(prefix -> List.of(), result::set).execute("ZZZ");
    assertTrue(result.get().codes().isEmpty());
    assertFalse(result.get().unavailable());
    new SuggestCourseCodesInteractor(
            prefix -> {
              throw new CatalogAccessException("Offline", new Exception());
            },
            result::set)
        .execute("CSC");
    assertTrue(result.get().codes().isEmpty());
    assertTrue(result.get().unavailable());
  }

  @Test
  void scopedRequestCannotUseAnOfflineIndex() {
    AtomicReference<SuggestCourseCodesResponse> result = new AtomicReference<>();
    CourseCodeIndex offline =
        prefix -> {
          throw new AssertionError("A scoped request should never reach an unscoped index.");
        };

    new SuggestCourseCodesInteractor(offline, result::set)
        .execute("CSC", new CatalogueScope("20269", "ARTSC"));

    assertTrue(result.get().unavailable());
    assertTrue(result.get().codes().isEmpty());
  }

  @Test
  void simpleInputBoundaryStillReceivesThePrefix() {
    List<String> requested = new ArrayList<>();
    SuggestCourseCodesInputBoundary boundary = requested::add;

    boundary.execute("csc", new CatalogueScope("20269", "ARTSC"));

    assertEquals(List.of("csc"), requested);
  }

  @Test
  void rejectsAnUnavailableResponseThatContainsSuggestions() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new SuggestCourseCodesResponse(List.of("CSC108H1"), true));
  }
}
