package domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tracks the sections selected for a timetable.
 *
 * <p>Sections are identified by their offering ID and their name within that offering,
 * because a section name such as LEC0101 is only unique inside one offering.
 * Selections keep the order in which they were added so the timetable displays them
 * predictably.
 */
public final class Timetable {
    private final Map<String, Set<String>> sectionsByOffering = new LinkedHashMap<>();

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
            sections = new LinkedHashSet<>();
            sectionsByOffering.put(offeringId, sections);
        }

        return sections.add(sectionName);
    }

    /**
     * Removes a section from the timetable.
     *
     * @param offeringId the ID of the course offering
     * @param sectionName the section name within that offering
     * @return true if the section was removed, or false if it was not selected
     */
    public boolean removeSection(String offeringId, String sectionName) {
        Set<String> sections = sectionsByOffering.get(offeringId);

        if (sections == null || !sections.remove(sectionName)) {
            return false;
        }

        if (sections.isEmpty()) {
            sectionsByOffering.remove(offeringId);
        }

        return true;
    }

    /**
     * Reports whether a section is currently selected.
     *
     * @param offeringId the ID of the course offering
     * @param sectionName the section name within that offering
     * @return true if the section is in the timetable
     */
    public boolean contains(String offeringId, String sectionName) {
        Set<String> sections = sectionsByOffering.get(offeringId);
        return sections != null && sections.contains(sectionName);
    }

    /**
     * Returns every selected section in the order it was added.
     *
     * @return a read-only list of selections; empty when nothing is selected
     */
    public List<SelectedSection> selections() {
        List<SelectedSection> result = new ArrayList<>();

        for (Map.Entry<String, Set<String>> entry : sectionsByOffering.entrySet()) {
            for (String sectionName : entry.getValue()) {
                result.add(new SelectedSection(entry.getKey(), sectionName));
            }
        }

        return Collections.unmodifiableList(result);
    }
}
