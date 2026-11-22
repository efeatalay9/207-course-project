package usecase.searchcourses;

import usecase.common.CatalogAccessException;

import domain.CourseOffering;
import java.util.List;

// The read-only catalogue port owned by the search use case.
public interface CourseCatalog {
    // Accepts a trimmed, uppercase full code. Returns all matches, never null.
    // No matches means an empty list; access failure must throw CatalogAccessException.
    List<CourseOffering> findByCode(String normalizedCode) throws CatalogAccessException;
}
