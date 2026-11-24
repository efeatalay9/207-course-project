package usecase.overlaps;

import java.util.List;

/** Delivers warnings or an unavailable check to a replaceable presenter. */
public interface CheckSectionOverlapsOutputBoundary {
  /** Replaces the previous warnings with the current results. */
  void present(List<SectionOverlapResult> results);

  /** Reports a failed check without implying that the timetable is conflict-free. */
  void presentUnavailable();
}
