package usecase.suggestcoursecodes;

import usecase.common.CatalogAccessException;
import usecase.common.CatalogueScope;
import java.util.List;

// A narrow read port for autocomplete; it does not retrieve offering details.
public interface CourseCodeIndex {
    // Accepts a normalized prefix; returns every matching distinct code in natural String order.
    // Empty prefixes return no suggestions. Implementations never return null or truncate matches.
    List<String> findCodesByPrefix(String normalizedPrefix) throws CatalogAccessException;

    /** Returns the matching codes available for a scoped catalogue lookup. */
    default List<String> findCodesByPrefix(String normalizedPrefix, CatalogueScope scope)
            throws CatalogAccessException {
        if (scope != null) {
            throw new CatalogAccessException("This catalogue does not support scoped suggestions.", null);
        }
        return findCodesByPrefix(normalizedPrefix);
    }
}
