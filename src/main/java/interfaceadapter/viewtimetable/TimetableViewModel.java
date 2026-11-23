package interfaceadapter.viewtimetable;

import java.util.List;
import java.util.Objects;

/**
 * Display-only state for the weekly timetable panel. It contains no domain objects.
 *
 * <p>The view-timetable presenter replaces the grid state; the remove-section presenter sets a
 * short status message. Both are read by the Swing view after a controller call returns.
 */
public final class TimetableViewModel {
    private State state = State.empty("No sections selected yet. Search for a course and click Add to Timetable.");
    private String status = "";

    /**
     * Replaces the whole grid state.
     *
     * @param state the new state
     */
    public void update(State state) {
        this.state = Objects.requireNonNull(state);
    }

    /** @return the current grid state */
    public State state() {
        return state;
    }

    /**
     * Sets the one-line message shown below the grid.
     *
     * @param status the message, or an empty string for none
     */
    public void setStatus(String status) {
        this.status = Objects.requireNonNull(status);
    }

    /** @return the current status message, possibly empty */
    public String status() {
        return status;
    }

    /**
     * Everything needed to draw the grid and the list of selected sections.
     *
     * @param sessions the sessions the student can switch between
     * @param selectedSession the session code currently displayed, or empty
     * @param dayLabels column headings, for example Mon to Fri
     * @param firstHour the first hour row shown (24-hour clock)
     * @param lastHour the hour at which the grid ends (exclusive)
     * @param blocks meeting blocks positioned by column and minutes after midnight
     * @param sections every selected section, for the remove list
     * @param notes sections that cannot be drawn, such as Time TBA
     * @param summary a heading describing the timetable
     */
    public record State(List<SessionOption> sessions, String selectedSession, List<String> dayLabels,
                        int firstHour, int lastHour, List<Block> blocks, List<SectionItem> sections,
                        List<String> notes, String summary) {
        /** Copies lists so the view cannot change the presenter's output. */
        public State {
            sessions = List.copyOf(sessions);
            dayLabels = List.copyOf(dayLabels);
            blocks = List.copyOf(blocks);
            sections = List.copyOf(sections);
            notes = List.copyOf(notes);
        }

        /**
         * Creates an empty Monday-to-Friday grid with a summary message.
         *
         * @param summary the message to display
         * @return an empty state
         */
        public static State empty(String summary) {
            return new State(List.of(), "", List.of("Mon", "Tue", "Wed", "Thu", "Fri"),
                    9, 18, List.of(), List.of(), List.of(), summary);
        }
    }

    /**
     * One entry in the session chooser.
     *
     * @param code the session code passed back to the controller
     * @param label the text shown to the student
     */
    public record SessionOption(String code, String label) {
        @Override
        public String toString() {
            return label;
        }
    }

    /**
     * One meeting drawn on the grid.
     *
     * @param offeringId identifies the section together with sectionName
     * @param sectionName the section name
     * @param courseCode used to colour blocks of the same course consistently
     * @param column index into dayLabels
     * @param startMinute minutes after midnight when the meeting starts
     * @param endMinute minutes after midnight when the meeting ends
     * @param title the first line of text, for example "CSC207H1 LEC0101"
     * @param detail the second line, for example "10:00–11:00 · BA1160"
     */
    public record Block(String offeringId, String sectionName, String courseCode, int column,
                        int startMinute, int endMinute, String title, String detail) { }

    /**
     * One selected section in the list beside the grid.
     *
     * @param offeringId the offering that owns the section
     * @param sectionName the section name
     * @param label the text shown to the student
     */
    public record SectionItem(String offeringId, String sectionName, String label) {
        @Override
        public String toString() {
            return label;
        }
    }
}
