package interfaceadapter.searchcourses;

import java.util.Objects;

// Display-only state shared by the presenter and view. It contains no domain objects.
public final class SearchCoursesViewModel {
    private State state = new State("Ready to search",
            "Start typing a course code, for example CSC, then choose a suggestion.\n"
                    + "You can also enter a full code such as CSC108H1 and click Search.");

    // The presenter replaces the complete state so previous results cannot leak into errors.
    public void update(State state) {
        this.state = Objects.requireNonNull(state);
    }

    // Views read the state after the synchronous, indexed search completes.
    public State state() {
        return state;
    }

    // Plain text renders consistently in a desktop window and a console session.
    public record State(String heading, String details) { }
}
