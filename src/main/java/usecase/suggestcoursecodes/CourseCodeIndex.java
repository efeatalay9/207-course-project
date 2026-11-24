package usecase.suggestcoursecodes;

import java.util.List;
import usecase.common.CatalogAccessException;
import usecase.common.CatalogueScope;

/** Reads course codes for prefix suggestions. */
public interface CourseCodeIndex {
  /**
   * Looks up codes beginning with a normalized prefix.
   *
   * @param normalizedPrefix uppercase prefix, or an empty string for no suggestions
   * @return distinct matching codes in natural order
   * @throws CatalogAccessException if the index cannot fulfill the lookup
   */
  List<String> findCodesByPrefix(String normalizedPrefix) throws CatalogAccessException;

  /**
   * Looks up suggested codes within the given catalogue scope.
   *
   * @param normalizedPrefix the normalized prefix
   * @param scope the selected term and division, or null for an unscoped lookup
   * @return distinct codes in natural order
   * @throws CatalogAccessException if the lookup fails or the scope is unsupported
   */
  default List<String> findCodesByPrefix(String normalizedPrefix, CatalogueScope scope)
      throws CatalogAccessException {
    if (scope != null) {
      throw new CatalogAccessException("This catalogue does not support scoped suggestions.", null);
    }
    return findCodesByPrefix(normalizedPrefix);
  }
}
