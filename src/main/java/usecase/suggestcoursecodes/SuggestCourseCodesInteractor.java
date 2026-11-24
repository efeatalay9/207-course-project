package usecase.suggestcoursecodes;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import usecase.common.CatalogAccessException;
import usecase.common.CatalogueScope;

/** Normalizes course prefixes and publishes the matching suggestions or lookup failure. */
public final class SuggestCourseCodesInteractor implements SuggestCourseCodesInputBoundary {
  private final CourseCodeIndex index;
  private final SuggestCourseCodesOutputBoundary output;

  /**
   * Creates the autocomplete use case.
   *
   * @param index supplies course codes for the normalized prefix and scope
   * @param output receives the suggestions or failure
   */
  public SuggestCourseCodesInteractor(
      CourseCodeIndex index, SuggestCourseCodesOutputBoundary output) {
    this.index = Objects.requireNonNull(index);
    this.output = Objects.requireNonNull(output);
  }

  // Use the same normalization as exact search, without querying storage for blank input.
  @Override
  public void execute(String prefix) {
    execute(prefix, null);
  }

  @Override
  public void execute(String prefix, CatalogueScope scope) {
    String normalized = prefix == null ? "" : prefix.strip().toUpperCase(Locale.ROOT);
    if (normalized.isEmpty()) {
      output.present(new SuggestCourseCodesResponse(List.of(), false));
      return;
    }
    List<String> codes;
    try {
      codes = index.findCodesByPrefix(normalized, scope);
    } catch (CatalogAccessException exception) {
      output.present(new SuggestCourseCodesResponse(List.of(), true));
      return;
    }
    output.present(new SuggestCourseCodesResponse(codes, false));
  }
}
