package usecase.addsection;

import domain.CourseOffering;
import domain.Section;
import domain.StatusFlag;
import domain.Timetable;
import java.util.Objects;
import usecase.addsection.AddSectionResponse.Outcome;
import usecase.common.CatalogAccessException;
import usecase.viewtimetable.OfferingLookup;

/** Verifies a section against the catalogue before adding it to the active timetable. */
public final class AddSectionInteractor implements AddSectionInputBoundary {
  private final Timetable timetable;
  private final OfferingLookup offerings;
  private final AddSectionOutputBoundary output;

  /**
   * Creates the interactor for the active timetable and catalogue.
   *
   * @param timetable the timetable to update
   * @param offerings the catalogue lookup by offering ID
   * @param output where to send the result of every request
   */
  public AddSectionInteractor(
      Timetable timetable, OfferingLookup offerings, AddSectionOutputBoundary output) {
    this.timetable = Objects.requireNonNull(timetable, "timetable");
    this.offerings = Objects.requireNonNull(offerings, "offerings");
    this.output = Objects.requireNonNull(output, "output");
  }

  @Override
  public void execute(AddSectionRequest request) {
    output.present(add(request));
  }

  private AddSectionResponse add(AddSectionRequest request) {
    if (request == null || isBlank(request.offeringId()) || isBlank(request.sectionName())) {
      return new AddSectionResponse(Outcome.NO_SELECTION);
    }

    CourseOffering offering;
    try {
      offering = offerings.findById(request.offeringId()).orElse(null);
    } catch (CatalogAccessException exception) {
      return new AddSectionResponse(Outcome.CATALOGUE_UNAVAILABLE);
    }
    if (offering == null) {
      return new AddSectionResponse(Outcome.NOT_FOUND);
    }

    Section section = offering.findSection(request.sectionName()).orElse(null);
    if (section == null) {
      return new AddSectionResponse(Outcome.NOT_FOUND, offering.code(), request.sectionName());
    }
    if (offering.cancelled() == StatusFlag.YES || section.cancelled() == StatusFlag.YES) {
      return new AddSectionResponse(Outcome.CANCELLED, offering.code(), section.name());
    }

    boolean added = timetable.addSection(request.offeringId(), section.name());
    Outcome outcome = added ? Outcome.ADDED : Outcome.ALREADY_SELECTED;
    return new AddSectionResponse(outcome, offering.code(), section.name());
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
