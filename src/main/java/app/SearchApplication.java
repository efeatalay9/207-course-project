package app;

import usecase.searchcourses.CourseCatalog;
import usecase.searchcourses.SearchCoursesInputBoundary;
import usecase.searchcourses.SearchCoursesInteractor;
import usecase.searchcourses.SearchCoursesOutputBoundary;
import usecase.suggestcoursecodes.CourseCodeIndex;
import usecase.suggestcoursecodes.SuggestCourseCodesInputBoundary;
import usecase.suggestcoursecodes.SuggestCourseCodesInteractor;
import usecase.suggestcoursecodes.SuggestCourseCodesOutputBoundary;
import java.util.Objects;

// Connects the application through ports, without choosing a storage format or visualizer.
public record SearchApplication(SearchCoursesInputBoundary search,
                                SuggestCourseCodesInputBoundary suggestions) {
    // Both use cases must be supplied; consumers only receive their input interfaces.
    public SearchApplication {
        Objects.requireNonNull(search, "search");
        Objects.requireNonNull(suggestions, "suggestions");
    }

    // Swap catalogue implementations or output presenters here; domain/use-case code stays unchanged.
    // Outputs receive structured results, so a graphical renderer need not parse our text presenter.
    public static SearchApplication connect(CourseCatalog catalog, CourseCodeIndex index,
                                            SearchCoursesOutputBoundary searchOutput,
                                            SuggestCourseCodesOutputBoundary suggestionOutput) {
        return new SearchApplication(new SearchCoursesInteractor(catalog, searchOutput),
                new SuggestCourseCodesInteractor(index, suggestionOutput));
    }
}
