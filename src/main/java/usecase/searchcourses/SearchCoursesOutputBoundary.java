package usecase.searchcourses;

// A presentation port; the use case knows nothing about windows or console output.
public interface SearchCoursesOutputBoundary {
    // Receives a completed search outcome, including validation and access failures.
    void present(SearchCoursesResponse response);
}
