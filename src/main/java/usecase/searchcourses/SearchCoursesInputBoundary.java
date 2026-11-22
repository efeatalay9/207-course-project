package usecase.searchcourses;

// Allows controllers to invoke search without depending on its implementation.
public interface SearchCoursesInputBoundary {
    // Publishes exactly one response for each request through the output boundary.
    void execute(SearchCoursesRequest request);
}
