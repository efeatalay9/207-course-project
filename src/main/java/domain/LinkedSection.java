package domain;

import java.util.Objects;

// A section reference within the same offering, such as LEC + 0101.
public record LinkedSection(String teachingMethod, String sectionNumber) {
    // Both parts are required to identify the linked section.
    public LinkedSection {
        Objects.requireNonNull(teachingMethod, "teachingMethod");
        Objects.requireNonNull(sectionNumber, "sectionNumber");
        if (teachingMethod.isBlank() || sectionNumber.isBlank()) {
            throw new IllegalArgumentException("A linked section needs a method and number.");
        }
    }
}
