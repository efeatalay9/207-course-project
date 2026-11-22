package domain;

import java.util.List;
import java.util.Objects;

// One scheduled offering. A course code may belong to several distinct offerings.
public record CourseOffering(String id, String code, String title, String campus,
                             List<String> sessions, String termDesignation,
                             StatusFlag cancelled, List<Section> sections) {
    // Required identity fields are validated; optional display text may be empty.
    public CourseOffering {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(campus, "campus");
        Objects.requireNonNull(termDesignation, "termDesignation");
        Objects.requireNonNull(cancelled, "cancelled");
        if (id.isBlank() || code.isBlank()) {
            throw new IllegalArgumentException("An offering needs an ID and course code.");
        }
        sessions = List.copyOf(sessions);
        sections = List.copyOf(sections);
    }
}
