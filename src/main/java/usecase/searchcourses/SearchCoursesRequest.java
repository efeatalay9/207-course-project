package usecase.searchcourses;

// Raw user input. Null and blank codes are handled as invalid input by the use case.
public record SearchCoursesRequest(String courseCode) { }
