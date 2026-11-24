package usecase.searchcourses;

import domain.CourseOffering;
import java.util.List;
import usecase.common.CatalogAccessException;
import usecase.common.CatalogueScope;

/** Reads course offerings for exact-code searches. */
public interface CourseCatalog {
  /**
   * Looks up a course code in an unscoped catalogue.
   *
   * @param normalizedCode the trimmed, uppercase course code
   * @return all matching offerings, or an empty list when the course is absent
   * @throws CatalogAccessException if the catalogue cannot fulfill the lookup
   */
  List<CourseOffering> findByCode(String normalizedCode) throws CatalogAccessException;

  /**
   * Looks up offerings within the selected term and division.
   *
   * @param normalizedCode the trimmed, uppercase course code
   * @param scope the selected term and division, or null for an unscoped lookup
   * @return all offerings matching the code and scope
   * @throws CatalogAccessException if the lookup fails or the scope is unsupported
   */
  default List<CourseOffering> findByCode(String normalizedCode, CatalogueScope scope)
      throws CatalogAccessException {
    if (scope != null) {
      throw new CatalogAccessException("This catalogue does not support scoped searches.", null);
    }
    return findByCode(normalizedCode);
  }
}
