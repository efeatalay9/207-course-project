package domain;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;

// One recurring meeting in an academic session, with local wall-clock times.
// Empty location/recurrence strings mean unknown, not unrestricted availability.
public record MeetingTime(DayOfWeek day, LocalTime start, LocalTime end,
                          String session, String location,
                          String repetition, String repetitionPattern) {
    // Only same-day positive intervals are currently supported by the catalogue adapter.
    public MeetingTime {
        Objects.requireNonNull(day, "day");
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        Objects.requireNonNull(session, "session");
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(repetition, "repetition");
        Objects.requireNonNull(repetitionPattern, "repetitionPattern");
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("A meeting must end after it starts.");
        }
    }
}
