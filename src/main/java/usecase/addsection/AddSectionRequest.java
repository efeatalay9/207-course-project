package usecase.addsection;

/**
 * Identifies the section the student wants to add.
 *
 * @param offeringId the offering that owns the section, or null if nothing was selected
 * @param sectionName the name within that offering, or null if nothing was selected
 */
public record AddSectionRequest(String offeringId, String sectionName) {}
