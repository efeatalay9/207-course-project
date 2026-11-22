package dataaccess.catalog;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import domain.CourseOffering;
import domain.LinkedSection;
import domain.MeetingTime;
import domain.Section;
import domain.StatusFlag;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

// Translates the external catalogue schema; JSON types never leave this package.
final class JsonOfferingMapper {
    // Map one offering, ignoring unrelated source fields and retaining scheduling details.
    CourseOffering map(JsonObject source) {
        List<String> sessions = new ArrayList<>();
        for (JsonElement value : array(source, "sessions")) {
            sessions.add(stringValue(value, "session"));
        }
        List<Section> sections = new ArrayList<>();
        for (JsonElement value : array(source, "sections")) {
            sections.add(section(value.getAsJsonObject()));
        }
        String title = text(source, "name");
        if (title.isBlank()) {
            title = text(source, "title");
        }
        return new CourseOffering(requiredText(source, "id"), requiredText(source, "code"),
                title, text(source, "campus"), sessions, text(source, "sectionCode"),
                flag(source, "cancelInd"), sections);
    }

    // Empty or absent meeting arrays are kept empty, never replaced with invented times.
    private Section section(JsonObject source) {
        List<MeetingTime> meetings = new ArrayList<>();
        for (JsonElement value : array(source, "meetingTimes")) {
            meetings.add(meeting(value.getAsJsonObject()));
        }
        List<LinkedSection> links = new ArrayList<>();
        for (JsonElement value : array(source, "linkedMeetingSections")) {
            JsonObject link = value.getAsJsonObject();
            links.add(new LinkedSection(requiredText(link, "teachMethod"),
                    requiredText(link, "sectionNumber")));
        }
        return new Section(requiredText(source, "name"), requiredText(source, "teachMethod"),
                flag(source, "cancelInd"), flag(source, "tbaInd"), meetings, links);
    }

    // Source weekdays use ISO numbering (Monday=1, Sunday=7), verified against U of T's app.
    // Milliseconds are time-of-day values, not timestamps; no timezone conversion applies.
    private MeetingTime meeting(JsonObject source) {
        JsonObject start = source.getAsJsonObject("start");
        JsonObject end = source.getAsJsonObject("end");
        int day = Math.toIntExact(integer(start, "day"));
        if (day != integer(end, "day")) {
            throw new IllegalArgumentException("Overnight meetings are not supported.");
        }
        return new MeetingTime(DayOfWeek.of(day), time(start), time(end),
                text(source, "sessionCode"), location(source), text(source, "repetition"),
                text(source, "repetitionTime"));
    }

    // Reject out-of-range or fractional milliseconds instead of silently rounding them.
    private LocalTime time(JsonObject source) {
        long millis = integer(source, "millisofday");
        if (millis < 0 || millis >= 86_400_000) {
            throw new IllegalArgumentException("Meeting time is outside a single day.");
        }
        return LocalTime.ofNanoOfDay(millis * 1_000_000);
    }

    // A missing building or room is optional data, not a failed catalogue load.
    private String location(JsonObject source) {
        JsonElement value = source.get("building");
        if (value == null || value.isJsonNull()) {
            return "";
        }
        JsonObject building = value.getAsJsonObject();
        String name = text(building, "buildingCode");
        if (name.isBlank()) {
            name = text(building, "buildingName");
        }
        String room = text(building, "buildingRoomNumber") + text(building, "buildingRoomSuffix");
        return (name + " " + room).strip();
    }

    // Only explicit Y/N values establish a known status.
    private StatusFlag flag(JsonObject source, String key) {
        return switch (text(source, key)) {
            case "Y" -> StatusFlag.YES;
            case "N" -> StatusFlag.NO;
            default -> StatusFlag.UNKNOWN;
        };
    }

    // Optional collections accept null/missing values, but reject an incorrect JSON type.
    private JsonArray array(JsonObject source, String key) {
        JsonElement value = source.get(key);
        return value == null || value.isJsonNull() ? new JsonArray() : value.getAsJsonArray();
    }

    // Required fields must be nonblank strings rather than coerced numbers or booleans.
    private String requiredText(JsonObject source, String key) {
        String value = text(source, key);
        if (value.isBlank()) {
            throw new IllegalArgumentException("Missing required field: " + key);
        }
        return value;
    }

    // Empty text is the domain convention for unknown optional text fields.
    private String text(JsonObject source, String key) {
        JsonElement value = source.get(key);
        return value == null || value.isJsonNull() ? "" : stringValue(value, key);
    }

    // Validate text types explicitly to avoid hiding source-schema errors.
    private String stringValue(JsonElement value, String key) {
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException("Expected text for " + key);
        }
        return value.getAsString();
    }

    // Numeric fields must contain exact integers; missing values cause a load failure.
    private long integer(JsonObject source, String key) {
        JsonElement value = source.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException("Expected a number for " + key);
        }
        return value.getAsBigDecimal().longValueExact();
    }
}
