package dataaccess.catalog;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import domain.CourseOffering;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import usecase.common.CatalogAccessException;
import usecase.common.CatalogueScope;
import usecase.searchcourses.CourseCatalog;
import usecase.suggestcoursecodes.CourseCodeIndex;
import usecase.viewtimetable.OfferingLookup;

/** Reads a selected term and division from the public University of Toronto Timetable Builder. */
public final class TtbCourseCatalog implements CourseCatalog, CourseCodeIndex, OfferingLookup {
  private static final URI BASE = URI.create("https://api.easi.utoronto.ca/ttb/");
  private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(12);
  private static final int PAGE_SIZE = 20;
  private static final int MAX_PAGES = 100;
  private static final int TTB_NO_RESULTS = 4404;

  /** Minimum length supported for live course-code suggestions. */
  public static final int MINIMUM_HINT_LENGTH = 3;

  private final Transport transport;
  private final JsonOfferingMapper mapper = new JsonOfferingMapper();
  private final Map<String, CourseOffering> fetchedById = new ConcurrentHashMap<>();

  /** Creates a catalogue that uses the public Timetable Builder HTTP service. */
  public TtbCourseCatalog() {
    this(httpTransport());
  }

  TtbCourseCatalog(Transport transport) {
    this.transport = transport;
  }

  /**
   * Retrieves the current term and division options before any course search.
   *
   * @return the filter labels and the codes used by the service
   * @throws CatalogAccessException if the service cannot supply usable options
   */
  public Filters fetchFilters() throws CatalogAccessException {
    JsonObject reference = payload("reference-data", null);
    try {
      return new Filters(
          readOptions(reference, "currentSessions"), readOptions(reference, "divisions"));
    } catch (RuntimeException exception) {
      throw invalidResponse(exception);
    }
  }

  @Override
  public List<CourseOffering> findByCode(String normalizedCode) throws CatalogAccessException {
    throw missingScope();
  }

  @Override
  public List<CourseOffering> findByCode(String normalizedCode, CatalogueScope scope)
      throws CatalogAccessException {
    if (scope == null) {
      throw missingScope();
    }

    List<CourseOffering> matches = new ArrayList<>();
    Set<String> seenIds = new HashSet<>();
    int total = -1;
    for (int page = 1; page <= MAX_PAGES; page++) {
      JsonObject search =
          payload("getPageableCourses", requestBody(normalizedCode, scope, page).toString());
      if (search == null) {
        if (page == 1) {
          return List.of();
        }
        throw new CatalogAccessException("The timetable search ended before its last page.", null);
      }
      try {
        JsonObject result = object(search.get("pageableCourse"), "pageableCourse");
        JsonArray courses = array(result, "courses");
        int pageTotal = integer(result, "total");
        if (integer(result, "page") != page
            || pageTotal < 0
            || (total >= 0 && pageTotal != total)) {
          throw new IllegalArgumentException("The search changed while pages were being read.");
        }
        total = pageTotal;
        for (JsonElement record : courses) {
          CourseOffering offering = mapper.map(object(record, "course"));
          if (!seenIds.add(offering.id())) {
            throw new IllegalArgumentException("The search returned a duplicate offering.");
          }
          if (offering.code().equalsIgnoreCase(normalizedCode)) {
            matches.add(offering);
          }
        }
        if (seenIds.size() == total) {
          // Keep earlier offerings available to the weekly grid after another enquiry.
          for (CourseOffering offering : matches) {
            fetchedById.put(offering.id(), offering);
          }
          return List.copyOf(matches);
        }
        if (courses.isEmpty() || seenIds.size() > total) {
          throw new IllegalArgumentException("The search returned an incomplete result.");
        }
      } catch (RuntimeException exception) {
        throw invalidResponse(exception);
      }
    }
    throw new CatalogAccessException("The timetable search exceeded its page limit.", null);
  }

  @Override
  public List<String> findCodesByPrefix(String normalizedPrefix) throws CatalogAccessException {
    throw missingScope();
  }

  @Override
  public List<String> findCodesByPrefix(String normalizedPrefix, CatalogueScope scope)
      throws CatalogAccessException {
    if (scope == null) {
      throw missingScope();
    }
    if (normalizedPrefix.length() < MINIMUM_HINT_LENGTH) {
      return List.of();
    }
    String path =
        "getOptimizedMatchingCourseTitles?term="
            + encode(normalizedPrefix)
            + "&divisions="
            + encode(scope.division())
            + "&sessions="
            + encode(scope.session())
            + "&lowerThreshold=50&upperThreshold=200";
    JsonObject suggestions = payload(path, null);
    if (suggestions == null) {
      return List.of();
    }
    try {
      Set<String> codes = new TreeSet<>();
      for (JsonElement element : array(suggestions, "codesAndTitles")) {
        JsonObject hint = object(element, "suggestion");
        String code = text(hint, "code");
        JsonObject division = object(hint.get("division"), "division");
        if (code.startsWith(normalizedPrefix) && text(division, "code").equals(scope.division())) {
          codes.add(code);
        }
      }
      return List.copyOf(codes);
    } catch (RuntimeException exception) {
      throw invalidResponse(exception);
    }
  }

  @Override
  public Optional<CourseOffering> findById(String offeringId) {
    return Optional.ofNullable(fetchedById.get(offeringId));
  }

  private static JsonObject requestBody(String code, CatalogueScope scope, int page) {
    JsonObject query = new JsonObject();
    query.addProperty("courseCode", code);
    query.addProperty("courseTitle", "");
    query.addProperty("courseSectionCode", "");
    query.addProperty("searchCourseDescription", false);
    JsonObject request = new JsonObject();
    request.add("courseCodeAndTitleProps", query);
    for (String key :
        List.of(
            "departmentProps",
            "campuses",
            "requirementProps",
            "courseLevels",
            "deliveryModes",
            "dayPreferences",
            "timePreferences",
            "creditWeights")) {
      request.add(key, new JsonArray());
    }
    JsonArray sessions = new JsonArray();
    sessions.add(scope.session());
    request.add("sessions", sessions);
    JsonArray divisions = new JsonArray();
    divisions.add(scope.division());
    request.add("divisions", divisions);
    request.addProperty("instructor", "");
    request.addProperty("availableSpace", false);
    request.addProperty("waitListable", false);
    request.addProperty("page", page);
    request.addProperty("pageSize", PAGE_SIZE);
    request.addProperty("direction", "asc");
    return request;
  }

  private JsonObject payload(String path, String json) throws CatalogAccessException {
    Response response;
    try {
      response = transport.read(BASE.resolve(path), json);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new CatalogAccessException("The Timetable Builder request was interrupted.", exception);
    } catch (IOException exception) {
      throw new CatalogAccessException(
          "Could not reach the Timetable Builder: " + exception.getMessage(), exception);
    }
    try {
      JsonObject envelope = object(JsonParser.parseString(response.body()), "response");
      JsonArray status = array(envelope, "status");
      JsonElement result = envelope.get("payload");
      // The public search returns HTTP 404 with this application code for a valid empty query.
      if (response.status() == 404
          && status.size() == 1
          && result != null
          && result.isJsonNull()
          && integer(object(status.get(0), "status"), "code") == TTB_NO_RESULTS) {
        return null;
      }
      if (response.status() != 200) {
        throw new IllegalArgumentException("HTTP " + response.status());
      }
      if (status.isEmpty()) {
        throw new IllegalArgumentException("The service omitted its request status.");
      }
      for (JsonElement entry : status) {
        if (integer(object(entry, "status"), "code") != 0) {
          throw new IllegalArgumentException("The service rejected the request.");
        }
      }
      return object(result, "payload");
    } catch (RuntimeException exception) {
      throw invalidResponse(exception);
    }
  }

  private static Transport httpTransport() {
    HttpClient client = HttpClient.newBuilder().connectTimeout(REQUEST_TIMEOUT).build();
    return (uri, json) -> {
      HttpRequest.Builder request =
          HttpRequest.newBuilder(uri).timeout(REQUEST_TIMEOUT).header("Accept", "application/json");
      if (json == null) {
        request.GET();
      } else {
        request
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json));
      }
      HttpResponse<String> response =
          client.send(request.build(), HttpResponse.BodyHandlers.ofString());
      return new Response(response.statusCode(), response.body());
    };
  }

  private static List<Option> readOptions(JsonObject reference, String field) {
    if (reference == null) {
      throw new IllegalArgumentException("The Timetable Builder returned no filter choices.");
    }
    List<Option> options = new ArrayList<>();
    for (JsonElement entry : array(reference, field)) {
      JsonObject option = object(entry, "filter choice");
      JsonElement header = option.get("header");
      if (header == null || !header.getAsBoolean()) {
        options.add(new Option(text(option, "value"), text(option, "label")));
      }
    }
    if (options.isEmpty()) {
      throw new IllegalArgumentException("No filter choices found for " + field);
    }
    return List.copyOf(options);
  }

  private static JsonObject object(JsonElement value, String name) {
    if (value == null || !value.isJsonObject()) {
      throw new IllegalArgumentException("Expected an object for " + name);
    }
    return value.getAsJsonObject();
  }

  private static JsonArray array(JsonObject source, String name) {
    JsonElement value = source.get(name);
    if (value == null || !value.isJsonArray()) {
      throw new IllegalArgumentException("Expected an array for " + name);
    }
    return value.getAsJsonArray();
  }

  private static String text(JsonObject source, String name) {
    JsonElement value = source.get(name);
    if (value == null
        || !value.isJsonPrimitive()
        || !value.getAsJsonPrimitive().isString()
        || value.getAsString().isBlank()) {
      throw new IllegalArgumentException("Expected text for " + name);
    }
    return value.getAsString();
  }

  private static int integer(JsonObject source, String name) {
    JsonElement value = source.get(name);
    if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
      throw new IllegalArgumentException("Expected an integer for " + name);
    }
    return value.getAsBigDecimal().intValueExact();
  }

  private static String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  private static CatalogAccessException invalidResponse(RuntimeException cause) {
    return new CatalogAccessException(
        "The Timetable Builder returned unusable data: " + cause.getMessage(), cause);
  }

  private static CatalogAccessException missingScope() {
    return new CatalogAccessException(
        "Choose a term and a division before using the live catalogue.", null);
  }

  /**
   * A published term or division choice.
   *
   * @param code the value sent with catalogue requests
   * @param label the name shown in the dropdown
   */
  public record Option(String code, String label) {}

  /**
   * Available selections obtained from the Timetable Builder.
   *
   * @param sessions terms offered by the service
   * @param divisions faculties or divisions offered by the service
   */
  public record Filters(List<Option> sessions, List<Option> divisions) {
    /** Takes read-only copies so the filter options stay fixed for the caller. */
    public Filters {
      sessions = List.copyOf(sessions);
      divisions = List.copyOf(divisions);
    }
  }

  @FunctionalInterface
  interface Transport {
    Response read(URI uri, String json) throws IOException, InterruptedException;
  }

  record Response(int status, String body) {}
}
