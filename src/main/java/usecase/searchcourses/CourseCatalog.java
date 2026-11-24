package usecase.searchcourses;

import usecase.common.CatalogAccessException;
import usecase.common.CatalogueScope;

import domain.CourseOffering;
import java.util.List;

// The read-only catalogue port owned by the search use case.
public interface CourseCatalog {
    // Accepts a trimmed, uppercase full code. Returns all matches, never null.
    // No matches means an empty list; access failure must throw CatalogAccessException.
    List<CourseOffering> findByCode(String normalizedCode) throws CatalogAccessException;

    /** Searches within a selected scope when the catalogue supports remote filtering. */
    default List<CourseOffering> findByCode(String normalizedCode, CatalogueScope scope)
            throws CatalogAccessException {
        if (scope != null) {
            throw new CatalogAccessException("This catalogue does not support scoped searches.", null);
        }
        return findByCode(normalizedCode);
    }
}
