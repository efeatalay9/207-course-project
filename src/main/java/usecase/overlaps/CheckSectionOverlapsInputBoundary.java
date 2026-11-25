package usecase.overlaps;

import domain.SelectedSection;
import java.util.List;

/** Checks the currently displayed candidate sections against the active timetable. */
public interface CheckSectionOverlapsInputBoundary {
  /** Checks candidates without adding or removing sections. */
  void execute(List<SelectedSection> candidates);
}
