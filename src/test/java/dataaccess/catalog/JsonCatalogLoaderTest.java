package dataaccess.catalog;

import domain.StatusFlag;
import domain.CourseOffering;
import domain.Section;
import domain.MeetingTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import static org.junit.jupiter.api.Assertions.*;
import static support.CatalogFixtures.sample;

// Small fixtures exercise schema conversion, strict parsing, and both source formats.
class JsonCatalogLoaderTest {
    @TempDir Path temporaryDirectory;
    private final JsonCatalogLoader loader = new JsonCatalogLoader();
    private static final String MINIMAL = "[{\"id\":\"one\",\"code\":\"CSC108H1\"}]";

    @Test
    void mapsTimesFlagsLinksAndRecurrenceWithoutLosingOfferings() throws Exception {
        List<CourseOffering> offerings = sample();
        assertEquals(3, offerings.size());
        Section lecture = offerings.get(0).sections().get(0);
        MeetingTime meeting = lecture.meetings().get(0);
        assertEquals(DayOfWeek.MONDAY, meeting.day());
        assertEquals(LocalTime.of(9, 0), meeting.start());
        assertEquals(LocalTime.of(10, 0), meeting.end());
        assertEquals("BA 1200", meeting.location());
        assertEquals("20269", meeting.session());
        assertEquals("", lecture.meetings().get(1).location());
        Section tutorial = offerings.get(0).sections().get(1);
        assertEquals("FIRST_AND_THIRD_WEEK", tutorial.meetings().get(0).repetitionPattern());
        assertEquals("BI_WEEKLY", tutorial.meetings().get(0).repetition());
        assertEquals("LEC", tutorial.linkedSections().get(0).teachingMethod());
        assertEquals("0101", tutorial.linkedSections().get(0).sectionNumber());
        assertTrue(offerings.get(1).sections().get(0).meetings().isEmpty());
        assertEquals(StatusFlag.YES, offerings.get(1).sections().get(0).timeToBeAnnounced());
        assertEquals(StatusFlag.YES, offerings.get(1).sections().get(1).cancelled());
        assertEquals(StatusFlag.YES, offerings.get(2).cancelled());
        assertEquals("MANUAL", offerings.get(2).sections().get(0).meetings().get(0).repetition());
        assertEquals(StatusFlag.UNKNOWN, offerings.get(2).sections().get(1).timeToBeAnnounced());
    }

    @Test
    void acceptsAbsentOptionalMetadataAndUnknownFields() throws Exception {
        List<CourseOffering> data = loader.load(new StringReader(MINIMAL));
        assertEquals(StatusFlag.UNKNOWN, data.get(0).cancelled());
        assertTrue(data.get(0).sessions().isEmpty());
        assertTrue(data.get(0).sections().isEmpty());
        assertEquals("", data.get(0).title());
        String extended = MINIMAL.replace("\"id\"", "\"futureField\":{\"anything\":[1,2]},\"id\"");
        assertEquals(data, loader.load(new StringReader(extended)));
    }

    @Test
    void plainJsonAndZipProduceTheSameDomainData() throws Exception {
        Path json = temporaryDirectory.resolve("courses.json");
        Files.writeString(json, MINIMAL);
        Path zip = temporaryDirectory.resolve("catalogue.zip");
        writeZip(zip, "courses.json", MINIMAL);
        assertEquals(loader.load(json), loader.load(zip));
    }

    @Test
    void missingFilesAndMissingZipEntriesAreLoadFailures() throws Exception {
        assertThrows(CatalogLoadException.class, () -> loader.load(temporaryDirectory.resolve("missing.json")));
        Path zip = temporaryDirectory.resolve("wrong.zip");
        writeZip(zip, "other.json", MINIMAL);
        assertThrows(CatalogLoadException.class, () -> loader.load(zip));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "{}", "[null]", "[{}]", "[{\"id\":42,\"code\":\"X\"}]",
            "[{\"id\":\"a\",\"code\":\"X\",\"sections\":{}}]", "[", "[] trailing", "[] []",
            "[{id:'one',code:'CSC108H1'}]", "[{},]"})
    void rejectsMalformedOrWronglyTypedCatalogues(String json) {
        assertThrows(CatalogLoadException.class, () -> loader.load(new StringReader(json)));
    }

    @Test
    void rejectsDuplicateOfferingIds() {
        String single = "{\"id\":\"one\",\"code\":\"CSC108H1\"}";
        assertThrows(CatalogLoadException.class, () -> loader.load(new StringReader("[" + single + "," + single + "]")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "8", "1.5"})
    void rejectsInvalidWeekdays(String day) {
        assertThrows(CatalogLoadException.class,
                () -> loader.load(new StringReader(withMeeting(day, "32400000", "36000000"))));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "86400000", "32400000.5", "36000000", "39600000"})
    void rejectsInvalidTimesAndNonpositiveIntervals(String start) {
        assertThrows(CatalogLoadException.class,
                () -> loader.load(new StringReader(withMeeting("1", start, "36000000"))));
    }

    @Test
    void rejectsOvernightMeetingsAndReportsTheRecord() {
        String json = withMeeting("1", "32400000", "36000000").replace("\"end\":{\"day\":1", "\"end\":{\"day\":2");
        CatalogLoadException failure = assertThrows(CatalogLoadException.class, () -> loader.load(new StringReader(json)));
        assertTrue(failure.getMessage().contains("record 1"));
        assertTrue(failure.getMessage().contains("Overnight"));
    }

    @Test
    void acceptsEmptyCatalogueAsDistinctFromMalformedInput() throws Exception {
        assertTrue(loader.load(new StringReader("[]")).isEmpty());
    }

    // Write a minimal ZIP fixture without extracting or reading the production archive.
    private void writeZip(Path path, String entryName, String contents) throws Exception {
        try (ZipOutputStream stream = new ZipOutputStream(Files.newOutputStream(path))) {
            stream.putNextEntry(new ZipEntry(entryName));
            stream.write(contents.getBytes(StandardCharsets.UTF_8));
            stream.closeEntry();
        }
    }

    // Only the varied numeric fields differ between validation cases.
    private String withMeeting(String day, String start, String end) {
        return "[{\"id\":\"one\",\"code\":\"CSC108H1\",\"sections\":[{\"name\":\"LEC0101\","
                + "\"teachMethod\":\"LEC\",\"meetingTimes\":[{\"start\":{\"day\":" + day
                + ",\"millisofday\":" + start + "},\"end\":{\"day\":" + day
                + ",\"millisofday\":" + end + "}}]}]}]";
    }
}
