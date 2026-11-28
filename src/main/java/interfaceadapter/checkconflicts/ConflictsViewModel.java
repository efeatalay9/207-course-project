package interfaceadapter.checkconflicts;

import java.util.List;
import java.util.Objects;

/** Display-only result of the latest conflict check. It contains no domain objects. */
public final class ConflictsViewModel {
  private State state = new State(Level.NONE, "", List.of(), List.of());

  /**
   * Replaces the result.
   *
   * @param state the new result
   */
  public void update(State state) {
    this.state = Objects.requireNonNull(state, "state");
  }

  /**
   * Returns the latest result.
   *
   * @return the current state
   */
  public State state() {
    return state;
  }

  /** How the view should style the result. */
  public enum Level {
    /** No check has run yet. */
    NONE,
    /** No overlaps were found. */
    CLEAR,
    /** Only possible overlaps or unchecked sections were found. */
    REVIEW,
    /** At least one definite overlap was found. */
    WARNING,
    /** The check could not run. */
    ERROR
  }

  /**
   * Everything the view needs to show a conflict check.
   *
   * @param level how serious the result is
   * @param heading a one-line summary
   * @param conflicts one line per overlapping pair
   * @param notes sections that need review or could not be checked
   */
  public record State(Level level, String heading, List<String> conflicts, List<String> notes) {
    /** Copies the lists so the view cannot change the presenter's output. */
    public State {
      Objects.requireNonNull(level, "level");
      Objects.requireNonNull(heading, "heading");
      conflicts = List.copyOf(conflicts);
      notes = List.copyOf(notes);
    }
  }
}
