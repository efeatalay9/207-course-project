package usecase.addsection;

/** Defines the action of adding a section to a timetable. */
public interface AddSectionInputBoundary {
    /**
     * Adds the selected section to the timetable.
     *
     * @param offeringId the course offering ID
     * @param sectionName the section name within that offering
     * @return true if the section was added, or false if it was already selected
     */
    boolean execute(String offeringId, String sectionName);
}
