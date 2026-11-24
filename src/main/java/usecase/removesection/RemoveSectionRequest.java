package usecase.removesection;

/**
 * Identifies the section the student wants to remove.
 *
 * @param offeringId the offering that owns the section
 * @param sectionName the section name within that offering
 */
public record RemoveSectionRequest(String offeringId, String sectionName) { }
