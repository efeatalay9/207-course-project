package interfaceadapter.removesection;

import java.util.Objects;
import usecase.removesection.RemoveSectionInputBoundary;
import usecase.removesection.RemoveSectionRequest;

/** Turns a Remove click in the view into a remove-section request. */
public final class RemoveSectionController {
    private final RemoveSectionInputBoundary useCase;

    /**
     * Creates the controller.
     *
     * @param useCase the remove-section input boundary
     */
    public RemoveSectionController(RemoveSectionInputBoundary useCase) {
        this.useCase = Objects.requireNonNull(useCase, "useCase");
    }

    /**
     * Requests removal of a section. Null values mean nothing was selected.
     *
     * @param offeringId the offering that owns the section
     * @param sectionName the section name within that offering
     */
    public void remove(String offeringId, String sectionName) {
        useCase.execute(new RemoveSectionRequest(offeringId, sectionName));
    }
}
