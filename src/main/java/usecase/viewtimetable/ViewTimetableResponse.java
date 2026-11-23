package usecase.viewtimetable;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

/**
 * The data needed to draw a weekly timetable, independent of any UI toolkit.
 *
 * @param sessions every session code that has at least one selected meeting, sorted
 * @param selectedSession the session whose meetings are listed, or empty if there are none
 * @param meetings the timed meetings in the selected session
 * @param sections every selected section, in the order it was added
 */
public record ViewTimetableResponse(List<String> sessions, String selectedSession,
                                    List<ScheduledMeeting> meetings,
                                    List<SelectedSectionResult> sections) {
    /** Copies all lists so callers cannot change the response. */
    public ViewTimetableResponse {
        sessions = List.copyOf(sessions);
        Objects.requireNonNull(selectedSession, "selectedSession");
        meetings = List.copyOf(meetings);
        sections = List.copyOf(sections);
    }

    /**
     * One meeting block to place on the weekly grid.
     *
     * @param offeringId the offering that owns the section
     * @param courseCode the course code, for example CSC207H1
     * @param sectionName the section name, for example LEC0101
     * @param day the weekday of the meeting
     * @param start the local start time
     * @param end the local end time
     * @param session the academic session code of this meeting
     * @param location the room, or empty if unknown
     * @param repetition the source repetition value, for example WEEKLY or BI_WEEKLY
     */
    public record ScheduledMeeting(String offeringId, String courseCode, String sectionName,
                                   DayOfWeek day, LocalTime start, LocalTime end, String session,
                                   String location, String repetition) { }

    /**
     * A section the student has selected, with its availability on the grid.
     *
     * @param offeringId the offering that owns the section
     * @param courseCode the course code, or empty if the offering is no longer in the catalogue
     * @param sectionName the section name
     * @param teachingMethod for example LEC, TUT, or PRA; empty if unknown
     * @param status whether the section has meetings, has none, or could not be found
     */
    public record SelectedSectionResult(String offeringId, String courseCode, String sectionName,
                                        String teachingMethod, SectionStatus status) { }

    /** Describes whether a selected section can be drawn on the grid. */
    public enum SectionStatus {
        /** The section has at least one known meeting time. */
        SCHEDULED,
        /** The section exists but has no known meeting times (time TBA or asynchronous). */
        TIME_TBA,
        /** The offering or section is no longer in the catalogue. */
        NOT_FOUND
    }
}
