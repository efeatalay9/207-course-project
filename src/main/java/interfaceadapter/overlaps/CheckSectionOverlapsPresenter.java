package interfaceadapter.overlaps;

import domain.SelectedSection;
import interfaceadapter.searchcourses.SearchCoursesViewModel;
import interfaceadapter.searchcourses.SearchCoursesViewModel.SectionChoice;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import usecase.overlaps.CheckSectionOverlapsOutputBoundary;
import usecase.overlaps.SectionOverlapResult;

/** Replaces warning decorations while retaining the original searchable section labels. */
public final class CheckSectionOverlapsPresenter implements CheckSectionOverlapsOutputBoundary {
  private final SearchCoursesViewModel model;

  /** Connects the presenter to the search display state. */
  public CheckSectionOverlapsPresenter(SearchCoursesViewModel model) {
    this.model = model;
  }

  @Override
  public void present(List<SectionOverlapResult> results) {
    Map<SelectedSection, SectionOverlapResult> bySection = new HashMap<>();
    for (SectionOverlapResult result : results) {
      bySection.put(result.candidate(), result);
    }
    List<SectionChoice> choices = new ArrayList<>();
    for (SectionChoice choice : model.state().sectionChoices()) {
      SectionOverlapResult result =
          bySection.get(new SelectedSection(choice.offeringId(), choice.sectionName()));
      choices.add(
          result == null
              ? choice.withWarning("", "")
              : choice.withWarning(
                  OverlapWarningFormatter.label(result), OverlapWarningFormatter.section(result)));
    }
    update(choices);
  }

  @Override
  public void presentUnavailable() {
    update(
        model.state().sectionChoices().stream()
            .map(choice ->
                choice.withWarning(
                    "[CHECK TIMES] ",
                    "Overlap check unavailable. You can still add this section; verify its times."))
            .toList());
  }

  private void update(List<SectionChoice> choices) {
    model.update(
        new SearchCoursesViewModel.State(
            model.state().heading(), model.state().details(), choices));
  }
}
