package usecase.suggestcoursecodes;

import usecase.common.CatalogAccessException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

// Coordinates prefix lookup. The index owns the algorithm and its ordering invariant.
public final class SuggestCourseCodesInteractor implements SuggestCourseCodesInputBoundary {
    private final CourseCodeIndex index;
    private final SuggestCourseCodesOutputBoundary output;

    // Tests can supply a fake index; production supplies the prebuilt catalogue index.
    public SuggestCourseCodesInteractor(CourseCodeIndex index, SuggestCourseCodesOutputBoundary output) {
        this.index = Objects.requireNonNull(index);
        this.output = Objects.requireNonNull(output);
    }

    // Use the same normalization as exact search, without querying storage for blank input.
    @Override
    public void execute(String prefix) {
        String normalized = prefix == null ? "" : prefix.strip().toUpperCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            output.present(new SuggestCourseCodesResponse(List.of(), false));
            return;
        }
        List<String> codes;
        try {
            codes = index.findCodesByPrefix(normalized);
        } catch (CatalogAccessException exception) {
            output.present(new SuggestCourseCodesResponse(List.of(), true));
            return;
        }
        output.present(new SuggestCourseCodesResponse(codes, false));
    }
}
