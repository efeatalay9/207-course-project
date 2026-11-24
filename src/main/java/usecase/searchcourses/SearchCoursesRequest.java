package usecase.searchcourses;

import usecase.common.CatalogueScope;

// Scope is absent for offline searches. The interactor handles blank course codes.
public record SearchCoursesRequest(String courseCode, CatalogueScope scope) {
    public SearchCoursesRequest(String courseCode) {
        this(courseCode, null);
    }
}
