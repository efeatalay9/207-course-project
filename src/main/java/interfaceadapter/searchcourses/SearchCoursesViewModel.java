package interfaceadapter.searchcourses;

import java.util.List;
import java.util.Objects;

/** Display-only state shared by the presenter and view; it contains no domain objects. */
public final class SearchCoursesViewModel {
  private State state =
      new State(
          "Ready to search",
          "Start typing a course code, for example CSC, then choose a suggestion.\n"
              + "You can also enter a full code such as CSC108H1 and click Search.");

  /** Replaces the complete state so previous results cannot leak into errors. */
  public void update(State state) {
    this.state = Objects.requireNonNull(state);
  }

  /** Returns the last state published by a completed search or warning check. */
  public State state() {
    return state;
  }

  /** Holds the search heading, result text, and selectable sections. */
  public static final class State {
    private final String heading;
    private final String details;
    private final List<SectionChoice> sectionChoices;

    /** Creates display text and a read-only list of selectable sections. */
    public State(String heading, String details, List<SectionChoice> sectionChoices) {
      this.heading = heading;
      this.details = details;
      this.sectionChoices = List.copyOf(sectionChoices);
    }

    /** Creates a message with no selectable results. */
    public State(String heading, String details) {
      this(heading, details, List.of());
    }

    /** Returns the result heading. */
    public String heading() {
      return heading;
    }

    /** Returns the formatted offering details. */
    public String details() {
      return details;
    }

    /** Returns the selectable sections, including their independent warning decorations. */
    public List<SectionChoice> sectionChoices() {
      return sectionChoices;
    }
  }

  /** One selectable section with its stable identity, display text, and optional warning. */
  public static final class SectionChoice {
    private final String offeringId;
    private final String sectionName;
    private final String label;
    private final String meetingDetails;
    private final String warningLabel;
    private final String warning;

    /** Creates an unflagged section choice; the warning presenter decorates it later. */
    public SectionChoice(
        String offeringId, String sectionName, String label, String meetingDetails) {
      this(offeringId, sectionName, label, meetingDetails, "", "");
    }

    private SectionChoice(
        String offeringId,
        String sectionName,
        String label,
        String meetingDetails,
        String warningLabel,
        String warning) {
      this.warningLabel = warningLabel;
      this.warning = warning;
      this.offeringId = offeringId;
      this.sectionName = sectionName;
      this.label = label;
      this.meetingDetails = meetingDetails;
    }

    /** Returns the source offering identity. */
    public String offeringId() {
      return offeringId;
    }

    /** Returns the exact section name within its offering. */
    public String sectionName() {
      return sectionName;
    }

    /** Returns the visible label including any warning prefix. */
    public String label() {
      return warningLabel + label;
    }

    /** Returns the meeting descriptions for this section. */
    public String meetingDetails() {
      return meetingDetails;
    }

    /** Replaces warnings without accumulating prefixes after a timetable refresh. */
    public SectionChoice withWarning(String warningLabel, String warning) {
      return new SectionChoice(
          offeringId, sectionName, label, meetingDetails, warningLabel, warning);
    }

    /** Returns hover text explaining the overlap or uncertainty, or an empty string. */
    public String warning() {
      return warning;
    }

    /** Returns the label shown for this section in the list. */
    @Override
    public String toString() {
      return warningLabel + label;
    }
  }
}
