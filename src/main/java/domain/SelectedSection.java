package domain;

import java.util.Objects;

/**
 * Identifies one section chosen for a timetable.
 *
 * @param offeringId the ID of the course offering that owns the section
 * @param sectionName the section name, unique within its offering
 */
public record SelectedSection(String offeringId, String sectionName) {
    /** Requires both parts of the identity. */
    public SelectedSection {
        Objects.requireNonNull(offeringId, "offeringId");
        Objects.requireNonNull(sectionName, "sectionName");
    }
}
