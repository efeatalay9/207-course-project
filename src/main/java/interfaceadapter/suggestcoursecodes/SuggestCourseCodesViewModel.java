package interfaceadapter.suggestcoursecodes;

import java.util.List;
import java.util.Objects;

// UI-independent dropdown state. Replacing it clears stale matches on each keystroke.
public final class SuggestCourseCodesViewModel {
    private State state = new State(List.of(), "No matching course codes");

    // Presenter replaces codes and status together.
    public void update(State state) {
        this.state = Objects.requireNonNull(state);
    }

    // The view reads state after a synchronous index lookup.
    public State state() {
        return state;
    }

    // All matches are retained; the dropdown scrolls instead of truncating the list.
    public record State(List<String> codes, String message) {
        public State {
            codes = List.copyOf(codes);
        }
    }
}
