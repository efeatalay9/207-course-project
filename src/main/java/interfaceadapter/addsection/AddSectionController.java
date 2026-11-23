package interfaceadapter.addsection;

import usecase.addsection.AddSectionInputBoundary;

/** Passes a section selection from the view to the add-section use case. */
public final class AddSectionController {
    private final AddSectionInputBoundary useCase;

    /**
     * Creates a controller for the add-section use case.
     *
     * @param useCase the input boundary that handles the request
     */
    public AddSectionController(AddSectionInputBoundary useCase) {
        this.useCase = useCase;
    }

    /**
     * Requests that a section be added to the timetable.
     *
     * @param offeringId the course offering ID
     * @param sectionName the section name within that offering
     * @return true if the section was added
     */
    public boolean addSection(String offeringId, String sectionName) {
        return useCase.execute(offeringId, sectionName);
    }
}