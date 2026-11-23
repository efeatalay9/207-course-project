package interfaceadapter.searchcourses;

import java.util.Objects;
import java.util.List;

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

    /** Holds the search heading, result text, and selectable sections. */
    public static final class State {
        private final String heading;
        private final String details;
        private final List<SectionChoice> sectionChoices;

        public State(String heading, String details, List<SectionChoice> sectionChoices) {
            this.heading = heading;
            this.details = details;
            this.sectionChoices = List.copyOf(sectionChoices);
        }

        public State(String heading, String details) {
            this(heading, details, List.of());
        }

        public String heading() {
            return heading;
        }

        public String details() {
            return details;
        }

        public List<SectionChoice> sectionChoices() {
            return sectionChoices;
        }
    }

    public static final class SectionChoice {
        private final String offeringId;
        private final String sectionName;
        private final String label;
        private final String meetingDetails;

        public SectionChoice(String offeringId, String sectionName, String label, String meetingDetails) {
            this.offeringId = offeringId;
            this.sectionName = sectionName;
            this.label = label;
            this.meetingDetails = meetingDetails;
        }

        public String offeringId() {
            return offeringId;
        }

        public String sectionName() {
            return sectionName;
        }

        public String label() {
            return label;
        }

        /** Returns the meeting descriptions for this section. */
        public String meetingDetails() {
            return meetingDetails;
        }

        /** Returns the label shown for this section in the list. */
        @Override
        public String toString() {
            return label;
        }
    }
}
