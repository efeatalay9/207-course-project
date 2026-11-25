package dataaccess.plan;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import domain.SavedPlan;
import domain.SelectedSection;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import usecase.plan.PlanAccessException;
import usecase.plan.PlanDataAccess;

/**
 * Stores plans as small, human-readable JSON files.
 *
 * <pre>
 * {
 *   "format": "uoft-timetable-plan",
 *   "version": 1,
 *   "savedAt": "2026-11-24T23:00:00Z",
 *   "selections": [ { "offeringId": "...", "sectionName": "LEC0101" } ]
 * }
 * </pre>
 *
 * <p>Saving writes a temporary file first and then replaces the target, so a failed write never
 * leaves a half-written plan behind.
 */
public final class JsonPlanFileDataAccess implements PlanDataAccess {
  static final String FORMAT = "uoft-timetable-plan";

  private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  @Override
  public void save(String location, SavedPlan plan) throws PlanAccessException {
    final Path target = toPath(location);
    JsonObject root = new JsonObject();
    root.addProperty("format", FORMAT);
    root.addProperty("version", plan.version());
    root.addProperty("savedAt", plan.savedAt().toString());
    JsonArray selections = new JsonArray();
    for (SelectedSection selection : plan.selections()) {
      JsonObject item = new JsonObject();
      item.addProperty("offeringId", selection.offeringId());
      item.addProperty("sectionName", selection.sectionName());
      selections.add(item);
    }
    root.add("selections", selections);

    Path directory = target.toAbsolutePath().getParent();
    Path temporary = null;
    try {
      temporary = Files.createTempFile(directory, ".plan-", ".tmp");
      Files.writeString(temporary, gson.toJson(root) + System.lineSeparator(),
          StandardCharsets.UTF_8);
      move(temporary, target);
    } catch (IOException exception) {
      deleteQuietly(temporary);
      throw new PlanAccessException("Could not write the plan file.", exception);
    }
  }

  @Override
  public SavedPlan load(String location) throws PlanAccessException {
    Path source = toPath(location);
    String text;
    try {
      text = Files.readString(source, StandardCharsets.UTF_8);
    } catch (NoSuchFileException exception) {
      throw new PlanAccessException("The plan file does not exist.", exception);
    } catch (IOException exception) {
      throw new PlanAccessException("Could not read the plan file.", exception);
    }

    try {
      JsonObject root = JsonParser.parseString(text).getAsJsonObject();
      if (!FORMAT.equals(string(root, "format"))) {
        throw invalid();
      }
      int version = integer(root, "version");
      Instant savedAt = Instant.parse(string(root, "savedAt"));
      List<SelectedSection> selections = new ArrayList<>();
      for (JsonElement element : array(root, "selections")) {
        JsonObject item = element.getAsJsonObject();
        selections.add(
            new SelectedSection(string(item, "offeringId"), string(item, "sectionName")));
      }
      return new SavedPlan(version, savedAt, selections);
    } catch (JsonParseException | IllegalStateException | DateTimeParseException
        | IllegalArgumentException exception) {
      throw new PlanAccessException("The file is not a valid timetable plan.", exception);
    }
  }

  // Required text fields must be present, be strings, and not be blank.
  private static String string(JsonObject object, String name) {
    JsonElement element = object.get(name);
    if (element == null || !element.isJsonPrimitive()
        || !((JsonPrimitive) element).isString() || element.getAsString().isBlank()) {
      throw new IllegalArgumentException("Missing field: " + name);
    }
    return element.getAsString();
  }

  // The version must be a whole number; 1.5 or "1" is rejected instead of rounded.
  private static int integer(JsonObject object, String name) {
    JsonElement element = object.get(name);
    if (element == null || !element.isJsonPrimitive() || !((JsonPrimitive) element).isNumber()) {
      throw new IllegalArgumentException("Missing field: " + name);
    }
    try {
      return element.getAsBigDecimal().intValueExact();
    } catch (ArithmeticException exception) {
      throw new IllegalArgumentException("Not a whole number: " + name, exception);
    }
  }

  private static JsonArray array(JsonObject object, String name) {
    JsonElement element = object.get(name);
    if (element == null || !element.isJsonArray()) {
      throw new IllegalArgumentException("Missing field: " + name);
    }
    return element.getAsJsonArray();
  }

  private static IllegalArgumentException invalid() {
    return new IllegalArgumentException("Unknown plan format.");
  }

  private static Path toPath(String location) throws PlanAccessException {
    try {
      return Path.of(location);
    } catch (InvalidPathException exception) {
      throw new PlanAccessException("The plan location is not a valid file path.", exception);
    }
  }

  private static void move(Path temporary, Path target) throws IOException {
    try {
      Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING,
          StandardCopyOption.ATOMIC_MOVE);
    } catch (AtomicMoveNotSupportedException exception) {
      Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  private static void deleteQuietly(Path file) {
    if (file == null) {
      return;
    }
    try {
      Files.deleteIfExists(file);
    } catch (IOException ignored) {
      // The original error is more useful to the student than a cleanup failure.
    }
  }
}
