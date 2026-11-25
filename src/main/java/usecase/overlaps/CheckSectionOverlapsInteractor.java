package usecase.overlaps;

import domain.SelectedSection;
import domain.Timetable;
import java.util.List;
import usecase.common.CatalogAccessException;

/** Reads a selection snapshot and reports overlap warnings without changing selections. */
public final class CheckSectionOverlapsInteractor implements CheckSectionOverlapsInputBoundary {
  private final Timetable timetable;
  private final SectionOverlapCheck checker;
  private final CheckSectionOverlapsOutputBoundary output;

  /** Connects the active plan, substitutable overlap policy, and output presenter. */
  public CheckSectionOverlapsInteractor(
      Timetable timetable, SectionOverlapCheck checker, CheckSectionOverlapsOutputBoundary output) {
    this.timetable = timetable;
    this.checker = checker;
    this.output = output;
  }

  @Override
  public void execute(List<SelectedSection> candidates) {
    try {
      output.present(checker.check(List.copyOf(candidates), timetable.selections()));
    } catch (CatalogAccessException exception) {
      output.presentUnavailable();
    }
  }
}
