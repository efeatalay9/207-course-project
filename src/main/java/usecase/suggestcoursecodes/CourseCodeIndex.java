package usecase.suggestcoursecodes;

import usecase.common.CatalogAccessException;
import java.util.List;

// A narrow read port for autocomplete; it does not retrieve offering details.
public interface CourseCodeIndex {
    // Accepts a normalized prefix; returns every matching distinct code in natural String order.
    // Empty prefixes return no suggestions. Implementations never return null or truncate matches.
    List<String> findCodesByPrefix(String normalizedPrefix) throws CatalogAccessException;
}
