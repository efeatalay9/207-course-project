package usecase.searchcourses;

import java.util.List;
import java.util.Objects;

// UI-independent outcome. A successful search is allowed to have zero offerings.
public record SearchCoursesResponse(Outcome outcome, String normalizedCode,
                                    List<OfferingResult> offerings) {
    public enum Outcome { SUCCESS, INVALID_INPUT, CATALOGUE_FAILURE }

    // Copy results so later changes cannot alter a published response.
    public SearchCoursesResponse {
        Objects.requireNonNull(outcome, "outcome");
        Objects.requireNonNull(normalizedCode, "normalizedCode");
        offerings = List.copyOf(offerings);
        if (outcome != Outcome.SUCCESS && !offerings.isEmpty()) {
            throw new IllegalArgumentException("Failed searches cannot contain results.");
        }
    }
}
