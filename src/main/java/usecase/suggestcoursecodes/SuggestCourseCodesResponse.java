package usecase.suggestcoursecodes;

import java.util.List;

// Immutable autocomplete output; failure must not be confused with an unknown prefix.
public record SuggestCourseCodesResponse(List<String> codes, boolean unavailable) {
    // Protect the published list from subsequent changes by an adapter or caller.
    public SuggestCourseCodesResponse {
        codes = List.copyOf(codes);
        if (unavailable && !codes.isEmpty()) {
            throw new IllegalArgumentException("Unavailable suggestions cannot contain codes.");
        }
    }
}
