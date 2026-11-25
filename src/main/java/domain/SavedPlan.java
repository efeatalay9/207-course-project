package domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * An immutable snapshot of the sections chosen in a timetable, used to save and reopen a plan.
 *
 * <p>The snapshot only keeps stable identifiers (offering ID and section name), so a saved plan
 * still makes sense after the app restarts and the catalogue is loaded again. Restoring a
 * snapshot replaces every selection in the target timetable.
 */
public final class SavedPlan {
  /** The plan format version this build writes and knows how to open. */
  public static final int CURRENT_VERSION = 1;

  private final int version;
  private final Instant savedAt;
  private final List<SelectedSection> selections;

  /**
   * Creates a snapshot.
   *
   * @param version the plan format version, at least 1
   * @param savedAt when the plan was saved
   * @param selections the chosen sections in display order
   */
  public SavedPlan(int version, Instant savedAt, List<SelectedSection> selections) {
    if (version < 1) {
      throw new IllegalArgumentException("Plan version must be at least 1.");
    }
    this.version = version;
    this.savedAt = Objects.requireNonNull(savedAt, "savedAt");
    this.selections = List.copyOf(Objects.requireNonNull(selections, "selections"));
  }

  /**
   * Takes a snapshot of a timetable's current selections in the current format.
   *
   * @param timetable the timetable to copy
   * @param savedAt when the snapshot is taken
   * @return a snapshot that is not affected by later timetable changes
   */
  public static SavedPlan capture(Timetable timetable, Instant savedAt) {
    Objects.requireNonNull(timetable, "timetable");
    return new SavedPlan(CURRENT_VERSION, savedAt, timetable.selections());
  }

  /**
   * Replaces every selection in the timetable with the sections in this snapshot.
   *
   * @param timetable the timetable to update
   */
  public void restoreInto(Timetable timetable) {
    Objects.requireNonNull(timetable, "timetable");
    for (SelectedSection current : timetable.selections()) {
      timetable.removeSection(current.offeringId(), current.sectionName());
    }
    for (SelectedSection saved : selections) {
      timetable.addSection(saved.offeringId(), saved.sectionName());
    }
  }

  /**
   * Reports whether this build can open the snapshot.
   *
   * @return true when the version matches {@link #CURRENT_VERSION}
   */
  public boolean isSupported() {
    return version == CURRENT_VERSION;
  }

  /**
   * Reports whether the snapshot holds no sections.
   *
   * @return true when nothing was selected
   */
  public boolean isEmpty() {
    return selections.isEmpty();
  }

  /**
   * Returns the plan format version.
   *
   * @return the version number
   */
  public int version() {
    return version;
  }

  /**
   * Returns when the plan was saved.
   *
   * @return the save time
   */
  public Instant savedAt() {
    return savedAt;
  }

  /**
   * Returns the saved sections.
   *
   * @return a read-only list in display order
   */
  public List<SelectedSection> selections() {
    return selections;
  }
}
