package domain;

import java.util.List;
import java.util.Objects;

// A lecture, tutorial, or practical. Its name is unique within its offering.
public record Section(String name, String teachingMethod, StatusFlag cancelled,
                      StatusFlag timeToBeAnnounced, List<MeetingTime> meetings,
                      List<LinkedSection> linkedSections) {
    // Defensive copies prevent callers from changing a section after construction.
    public Section {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(teachingMethod, "teachingMethod");
        Objects.requireNonNull(cancelled, "cancelled");
        Objects.requireNonNull(timeToBeAnnounced, "timeToBeAnnounced");
        if (name.isBlank() || teachingMethod.isBlank()) {
            throw new IllegalArgumentException("A section needs a name and teaching method.");
        }
        meetings = List.copyOf(meetings);
        linkedSections = List.copyOf(linkedSections);
    }
}
