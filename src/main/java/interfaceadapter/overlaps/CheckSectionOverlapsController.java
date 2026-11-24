package interfaceadapter.overlaps;

import domain.SelectedSection;
import interfaceadapter.searchcourses.SearchCoursesViewModel.SectionChoice;
import java.util.List;
import usecase.overlaps.CheckSectionOverlapsInputBoundary;

/** Converts visible section identities to a warning-check request. */
public final class CheckSectionOverlapsController {
  private final CheckSectionOverlapsInputBoundary input;

  /** Accepts any implementation of the overlap input boundary. */
  public CheckSectionOverlapsController(CheckSectionOverlapsInputBoundary input) {
    this.input = input;
  }

  /** Passes stable offering and section identities to the use case. */
  public void check(List<SectionChoice> choices) {
    input.execute(
        choices.stream()
            .map(choice -> new SelectedSection(choice.offeringId(), choice.sectionName()))
            .toList());
  }
}
