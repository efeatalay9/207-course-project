package dataaccess.plan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.SavedPlan;
import domain.SelectedSection;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import usecase.plan.PlanAccessException;

class JsonPlanFileDataAccessTest {
  private static final Instant SAVED = Instant.parse("2026-11-24T23:00:00Z");
  private final JsonPlanFileDataAccess files = new JsonPlanFileDataAccess();

  @TempDir
  Path folder;

  @Test
  void savedPlanOpensWithTheSameSections() throws Exception {
    SavedPlan plan = new SavedPlan(1, SAVED, List.of(
        new SelectedSection("csc207-fall", "LEC0101"),
        new SelectedSection("csc207-fall", "TUT0201")));
    Path file = folder.resolve("fall.json");

    files.save(file.toString(), plan);
    SavedPlan reopened = files.load(file.toString());

    assertEquals(plan.version(), reopened.version());
    assertEquals(plan.savedAt(), reopened.savedAt());
    assertEquals(plan.selections(), reopened.selections());
    assertTrue(Files.readString(file).contains("\"format\": \"uoft-timetable-plan\""));
  }

  @Test
  void savingAgainReplacesTheFileAndLeavesNoTemporaryFiles() throws Exception {
    Path file = folder.resolve("fall.json");
    files.save(file.toString(), new SavedPlan(1, SAVED,
        List.of(new SelectedSection("a", "LEC0101"))));
    files.save(file.toString(), new SavedPlan(1, SAVED, List.of()));

    assertTrue(files.load(file.toString()).isEmpty());
    try (Stream<Path> entries = Files.list(folder)) {
      assertEquals(List.of(file), entries.toList());
    }
  }

  @Test
  void futureVersionIsReadSoTheUseCaseCanRejectIt() throws Exception {
    Path file = write("{\"format\":\"uoft-timetable-plan\",\"version\":2,"
        + "\"savedAt\":\"2026-11-24T23:00:00Z\",\"selections\":[]}");

    assertEquals(2, files.load(file.toString()).version());
  }

  @Test
  void missingFileIsReported() {
    PlanAccessException error = assertThrows(PlanAccessException.class,
        () -> files.load(folder.resolve("missing.json").toString()));
    assertEquals("The plan file does not exist.", error.getMessage());
  }

  @Test
  void folderCannotBeReadAsPlan() {
    PlanAccessException error = assertThrows(PlanAccessException.class,
        () -> files.load(folder.toString()));
    assertEquals("Could not read the plan file.", error.getMessage());
  }

  @Test
  void brokenFilesAreRejected() throws Exception {
    List<String> broken = List.of(
        "not json at all {",
        "[]",
        "{\"format\":\"something-else\",\"version\":1,\"savedAt\":\"2026-11-24T23:00:00Z\","
            + "\"selections\":[]}",
        "{\"version\":1,\"savedAt\":\"2026-11-24T23:00:00Z\",\"selections\":[]}",
        "{\"format\":\"uoft-timetable-plan\",\"version\":\"1\","
            + "\"savedAt\":\"2026-11-24T23:00:00Z\",\"selections\":[]}",
        "{\"format\":\"uoft-timetable-plan\",\"version\":1.5,"
            + "\"savedAt\":\"2026-11-24T23:00:00Z\",\"selections\":[]}",
        "{\"format\":\"uoft-timetable-plan\",\"version\":0,"
            + "\"savedAt\":\"2026-11-24T23:00:00Z\",\"selections\":[]}",
        "{\"format\":\"uoft-timetable-plan\",\"version\":1,\"savedAt\":\"yesterday\","
            + "\"selections\":[]}",
        "{\"format\":\"uoft-timetable-plan\",\"version\":1,\"savedAt\":\"2026-11-24T23:00:00Z\"}",
        "{\"format\":\"uoft-timetable-plan\",\"version\":1,\"savedAt\":\"2026-11-24T23:00:00Z\","
            + "\"selections\":[\"LEC0101\"]}",
        "{\"format\":\"uoft-timetable-plan\",\"version\":1,\"savedAt\":\"2026-11-24T23:00:00Z\","
            + "\"selections\":[{\"offeringId\":\"a\",\"sectionName\":\" \"}]}",
        "{\"format\":\"uoft-timetable-plan\",\"version\":1,\"savedAt\":\"2026-11-24T23:00:00Z\","
            + "\"selections\":[{\"offeringId\":\"a\",\"sectionName\":7}]}");
    for (String text : broken) {
      Path file = write(text);
      PlanAccessException error = assertThrows(PlanAccessException.class,
          () -> files.load(file.toString()), text);
      assertEquals("The file is not a valid timetable plan.", error.getMessage());
    }
  }

  @Test
  void writeFailureIsReportedAndLeavesNothingBehind() throws Exception {
    Path blocked = folder.resolve("taken");
    Files.createDirectory(blocked);
    Files.writeString(blocked.resolve("inside.txt"), "keeps the folder non-empty");

    PlanAccessException error = assertThrows(PlanAccessException.class,
        () -> files.save(blocked.toString(), new SavedPlan(1, SAVED, List.of())));

    assertEquals("Could not write the plan file.", error.getMessage());
    try (Stream<Path> entries = Files.list(folder)) {
      assertEquals(List.of(blocked), entries.toList());
    }
    assertFalse(Files.isRegularFile(blocked));
  }

  @Test
  void invalidPathIsReported() {
    PlanAccessException error = assertThrows(PlanAccessException.class,
        () -> files.load("bad\0name.json"));
    assertEquals("The plan location is not a valid file path.", error.getMessage());
  }

  private Path write(String text) throws IOException {
    Path file = Files.createTempFile(folder, "plan", ".json");
    Files.writeString(file, text);
    return file;
  }
}
