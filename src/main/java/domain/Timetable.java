package domain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Tracks the sections selected for a timetable. */
public final class Timetable {
    private final Map<String, Set<String>> sectionsByOffering = new HashMap<>();

    /**
     * Adds a section to the timetable.
     *
     * @param offeringId the ID of the course offering
     * @param sectionName the section name within that offering
     * @return true if the section was added, or false if it was already selected
     */
    public boolean addSection(String offeringId, String sectionName) {
        Set<String> sections = sectionsByOffering.get(offeringId);

        if (sections == null) {
            sections = new HashSet<>();
            sectionsByOffering.put(offeringId, sections);
        }

        return sections.add(sectionName);
    }
}