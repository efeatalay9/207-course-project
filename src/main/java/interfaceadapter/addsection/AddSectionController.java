package interfaceadapter.addsection;

import java.util.Objects;
import usecase.addsection.AddSectionInputBoundary;
import usecase.addsection.AddSectionRequest;

/** Passes a section selection from the view to the add-section use case. */
public final class AddSectionController {
  private final AddSectionInputBoundary useCase;

  /**
   * Creates a controller for the add-section use case.
   *
   * @param useCase the input boundary that handles the request
   */
  public AddSectionController(AddSectionInputBoundary useCase) {
    this.useCase = Objects.requireNonNull(useCase, "useCase");
  }

  /**
   * Forwards an add request. Null values represent a click with no selection.
   *
   * @param offeringId the course offering ID
   * @param sectionName the section name within that offering
   */
  public void addSection(String offeringId, String sectionName) {
    useCase.execute(new AddSectionRequest(offeringId, sectionName));
  }
}
