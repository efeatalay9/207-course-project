package usecase.overlaps;

import domain.SelectedSection;
import java.util.List;
import usecase.common.CatalogAccessException;

/** Read-only overlap policy shared by search and timetable projection. */
@FunctionalInterface
public interface SectionOverlapCheck {
  /**
   * Reports one result per candidate without mutating either selection list.
   *
   * @param candidates section identities to inspect
   * @param selected the fixed snapshot to compare against
   * @return immutable results, including uncertainty when catalogue facts are missing
   * @throws CatalogAccessException when the catalogue cannot be read
   */
  List<SectionOverlapResult> check(List<SelectedSection> candidates, List<SelectedSection> selected)
      throws CatalogAccessException;
}
