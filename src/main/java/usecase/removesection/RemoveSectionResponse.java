package usecase.removesection;

/**
 * Reports which section a removal request concerned.
 *
 * @param offeringId the offering that owns the section
 * @param sectionName the section name within that offering
 */
public record RemoveSectionResponse(String offeringId, String sectionName) { }
