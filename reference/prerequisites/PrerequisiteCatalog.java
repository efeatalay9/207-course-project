import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Downloads catalogue rules and evaluates a bounded set of operations on local student data. */
public final class PrerequisiteCatalog {
    private static final int MAX_BYTES = 5_000_000;
    private static final String COURSE_CODE = "[A-Z]{3,4}[0-9]{2,3}[HY][0-9]";
    private final Map<String, Entry> offerings;

    public enum Status { MET, NOT_MET, NEEDS_REVIEW, NONE_LISTED }

    /** Supply earned course codes and credits as of the applicable prerequisite deadline. */
    public record Profile(Set<String> completed, Map<String, BigDecimal> grades, boolean historyComplete,
                          BigDecimal earnedCredits, Map<String, Boolean> confirmedFacts,
                          Map<String, EarnedCourse> earnedCourses, BigDecimal cumulativeGpa) {
        public Profile {
            completed = Set.copyOf(completed);
            grades = Map.copyOf(grades);
            confirmedFacts = Map.copyOf(confirmedFacts);
            earnedCourses = Map.copyOf(earnedCourses);
            if (cumulativeGpa != null) {
                validateGpa(cumulativeGpa);
            }
        }

        public Profile(Set<String> completed, Map<String, BigDecimal> grades, boolean historyComplete,
                       BigDecimal earnedCredits, Map<String, Boolean> confirmedFacts) {
            this(completed, grades, historyComplete, earnedCredits, confirmedFacts, Map.of(), null);
        }
    }

    /** Course classification and awarded credit from verified records. Subject and level can be unknown. */
    public record EarnedCourse(String subject, Integer level, BigDecimal credits) {
        public EarnedCourse {
            require(subject == null || subject.matches("[A-Z]{3,4}"), "Invalid subject code");
            require(level == null || validLevel(level), "Invalid course level");
            validateThreshold(credits);
        }
    }

    public record Result(Status status, String publishedText, List<String> notes, String message) {}

    private record Entry(String code, String text, List<String> notes, JsonObject rule) {}

    private PrerequisiteCatalog(Map<String, Entry> offerings) {
        this.offerings = Map.copyOf(offerings);
    }

    public static PrerequisiteCatalog fetch(URI url) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        HttpRequest request = HttpRequest.newBuilder(url).timeout(Duration.ofSeconds(10)).GET().build();
        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        try (InputStream body = response.body()) {
            if (response.statusCode() != 200) {
                throw new IOException("Rules request returned HTTP " + response.statusCode());
            }
            return fromJson(body.readNBytes(MAX_BYTES + 1));
        }
    }

    public static PrerequisiteCatalog fromJson(byte[] bytes) throws IOException {
        if (bytes.length > MAX_BYTES) {
            throw new IOException("Prerequisite file is too large");
        }
        try {
            JsonObject document = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
            BigDecimal version = number(document, "version");
            boolean versionTwo = version.compareTo(BigDecimal.valueOf(2)) == 0;
            require(version.compareTo(BigDecimal.ONE) == 0 || versionTwo, "Unsupported format version");
            string(document, "capturedAt");
            Map<String, Entry> entries = new HashMap<>();
            for (JsonElement element : document.getAsJsonArray("offerings")) {
                JsonObject value = element.getAsJsonObject();
                String id = string(value, "id");
                require(!id.isBlank() && !entries.containsKey(id), "Repeated or blank offering ID");
                JsonObject rule = value.getAsJsonObject("rule");
                validateRule(rule, 0, versionTwo);
                List<String> notes = new ArrayList<>();
                if (value.has("notes")) {
                    for (JsonElement note : value.getAsJsonArray("notes")) {
                        notes.add(string(note, "notes"));
                    }
                }
                String code = string(value, "code");
                require(!code.isBlank(), "Blank course code");
                entries.put(id, new Entry(code, string(value, "publishedText"), List.copyOf(notes), rule));
            }
            require(!entries.isEmpty(), "Empty offering list");
            return new PrerequisiteCatalog(entries);
        } catch (JsonParseException | IllegalArgumentException | IllegalStateException | NullPointerException
                 | ClassCastException malformed) {
            throw new IOException("Invalid prerequisite file: " + malformed.getMessage(), malformed);
        }
    }

    public int size() {
        return offerings.size();
    }

    public Result check(String offeringId, String courseCode, Profile profile) {
        Entry entry = offerings.get(offeringId);
        if (entry == null || !entry.code().equals(courseCode)) {
            return new Result(Status.NEEDS_REVIEW, "", List.of(), "No matching offering in the prerequisite file");
        }
        Status status = evaluate(entry.rule(), profile);
        String message = switch (status) {
            case MET -> "Listed prerequisites met based on the supplied profile";
            case NOT_MET -> "Listed prerequisites not met based on the supplied profile";
            case NONE_LISTED -> "No prerequisite listed in the source field";
            case NEEDS_REVIEW -> entry.rule().has("reason") ? string(entry.rule(), "reason")
                    : "Check the published requirements and any missing grades, credits, cumulative GPA, or confirmations";
        };
        return new Result(status, entry.text(), entry.notes(), message);
    }

    /** Use the live field and relevant notes after applying the same normalization as the compiler. */
    public Result checkCurrent(String offeringId, String courseCode, String text, List<String> notes, Profile profile) {
        Entry entry = offerings.get(offeringId);
        if (entry == null || !entry.code().equals(courseCode) || !entry.text().equals(text) || !entry.notes().equals(notes)) {
            return new Result(Status.NEEDS_REVIEW, text == null ? "" : text, List.copyOf(notes),
                    "The current offering or prerequisite wording differs from the rule file");
        }
        return check(offeringId, courseCode, profile);
    }

    private static Status evaluate(JsonObject rule, Profile profile) {
        return switch (string(rule, "op")) {
            case "none" -> Status.NONE_LISTED;
            case "review" -> Status.NEEDS_REVIEW;
            case "course" -> courseStatus(rule, profile);
            case "credits" -> minimumStatus(profile.earnedCredits(), number(rule, "minimum"));
            case "cgpa" -> minimumStatus(profile.cumulativeGpa(), number(rule, "minimum"));
            case "scopedCredits" -> scopedCreditStatus(rule, profile);
            case "fact" -> {
                Boolean confirmed = profile.confirmedFacts().get(string(rule, "key"));
                yield confirmed == null ? Status.NEEDS_REVIEW : confirmed ? Status.MET : Status.NOT_MET;
            }
            case "all" -> groupStatus(rule, profile, Status.NOT_MET, Status.MET);
            case "any" -> groupStatus(rule, profile, Status.MET, Status.NOT_MET);
            case "if" -> conditionalStatus(rule, profile);
            default -> throw new IllegalStateException("Rule was never validated");
        };
    }

    private static Status courseStatus(JsonObject rule, Profile profile) {
        String code = string(rule, "code");
        if (!profile.completed().contains(code)) {
            return profile.historyComplete() ? Status.NOT_MET : Status.NEEDS_REVIEW;
        }
        return rule.has("minGrade") ? minimumStatus(profile.grades().get(code), number(rule, "minGrade")) : Status.MET;
    }

    private static Status minimumStatus(BigDecimal recorded, BigDecimal required) {
        if (recorded == null) {
            return Status.NEEDS_REVIEW;
        }
        return recorded.compareTo(required) >= 0 ? Status.MET : Status.NOT_MET;
    }

    private static Status scopedCreditStatus(JsonObject rule, Profile profile) {
        String subject = rule.has("subject") ? string(rule, "subject") : null;
        Set<String> eligibleCodes = rule.has("codes") ? creditCodes(rule) : Set.of();
        Integer level = rule.has("level") ? number(rule, "level").intValueExact() : null;
        Integer minimumLevel = rule.has("minimumLevel") ? number(rule, "minimumLevel").intValueExact() : null;
        BigDecimal subtotal = BigDecimal.ZERO;
        boolean unclassifiedCredits = !profile.historyComplete();
        for (String code : profile.completed()) {
            if (!eligibleCodes.isEmpty() && !eligibleCodes.contains(code)) {
                continue;
            }
            EarnedCourse course = profile.earnedCourses().get(code);
            if (course == null) {
                unclassifiedCredits = true;
            } else if (course.credits().signum() > 0) {
                if (subject != null && course.subject() == null) {
                    unclassifiedCredits = true;
                } else if (subject != null && !subject.equals(course.subject())) {
                    continue;
                } else if (course.level() == null && (level != null || minimumLevel != null)) {
                    unclassifiedCredits = true;
                } else if ((level == null || level.equals(course.level()))
                        && (minimumLevel == null || course.level() >= minimumLevel)) {
                    subtotal = subtotal.add(course.credits());
                }
            }
        }
        if (subtotal.compareTo(number(rule, "minimum")) >= 0) {
            return Status.MET;
        }
        return unclassifiedCredits ? Status.NEEDS_REVIEW : Status.NOT_MET;
    }

    private static Status groupStatus(JsonObject rule, Profile profile, Status decisive, Status fallback) {
        Status overall = fallback;
        for (JsonElement child : rule.getAsJsonArray("rules")) {
            Status result = evaluate(child.getAsJsonObject(), profile);
            if (result == decisive) {
                return decisive;
            }
            if (result == Status.NEEDS_REVIEW) {
                overall = Status.NEEDS_REVIEW;
            }
        }
        return overall;
    }

    private static Status conditionalStatus(JsonObject rule, Profile profile) {
        Status condition = evaluate(rule.getAsJsonObject("condition"), profile);
        if (condition == Status.MET) {
            return evaluate(rule.getAsJsonObject("then"), profile);
        }
        if (condition == Status.NOT_MET) {
            return evaluate(rule.getAsJsonObject("else"), profile);
        }
        Status whenTrue = evaluate(rule.getAsJsonObject("then"), profile);
        Status whenFalse = evaluate(rule.getAsJsonObject("else"), profile);
        return whenTrue == whenFalse ? whenTrue : Status.NEEDS_REVIEW;
    }

    private static void validateRule(JsonObject rule, int depth, boolean versionTwo) {
        require(depth <= 32, "Rule exceeds the nesting limit");
        String op = string(rule, "op");
        switch (op) {
            case "none" -> require(depth == 0, "None-listed is an offering status");
            case "review" -> string(rule, "reason");
            case "course" -> {
                require(string(rule, "code").matches(COURSE_CODE), "Invalid course code");
                if (rule.has("minGrade")) {
                    validateThreshold(number(rule, "minGrade"));
                }
            }
            case "credits" -> validateThreshold(number(rule, "minimum"));
            case "cgpa" -> {
                require(versionTwo, "Cumulative GPA rules require format version 2");
                validateGpa(number(rule, "minimum"));
            }
            case "scopedCredits" -> {
                require(versionTwo, "Scoped credit rules require format version 2");
                validateScopedCredits(rule);
            }
            case "fact" -> {
                require(!string(rule, "key").isBlank(), "Blank fact key");
                string(rule, "label");
            }
            case "all", "any" -> {
                require(rule.getAsJsonArray("rules").size() >= 2, "A rule group needs two or more alternatives or requirements");
                for (JsonElement child : rule.getAsJsonArray("rules")) {
                    validateRule(child.getAsJsonObject(), depth + 1, versionTwo);
                }
            }
            case "if" -> {
                validateRule(rule.getAsJsonObject("condition"), depth + 1, versionTwo);
                validateRule(rule.getAsJsonObject("then"), depth + 1, versionTwo);
                validateRule(rule.getAsJsonObject("else"), depth + 1, versionTwo);
            }
            default -> throw new IllegalArgumentException("Unknown rule operation: " + op);
        }
    }

    private static void validateThreshold(BigDecimal value) {
        require(value.signum() >= 0 && value.compareTo(BigDecimal.valueOf(100)) <= 0, "Threshold outside 0–100");
    }

    private static void validateGpa(BigDecimal value) {
        require(value.signum() >= 0 && value.compareTo(BigDecimal.valueOf(4)) <= 0, "Cumulative GPA outside 0–4");
    }

    private static void validateScopedCredits(JsonObject rule) {
        BigDecimal minimum = number(rule, "minimum");
        validateThreshold(minimum);
        require(minimum.signum() > 0, "Scoped credit minimum must be positive");
        if (rule.has("codes")) {
            require(!rule.has("subject") && !rule.has("level") && !rule.has("minimumLevel"),
                    "A course list cannot also specify a subject or level");
            require(!creditCodes(rule).isEmpty(), "Expected a nonempty list of eligible course codes");
        } else {
            require(string(rule, "subject").matches("[A-Z]{3,4}"), "Invalid subject code");
            require(!(rule.has("level") && rule.has("minimumLevel")), "Specify exact or minimum level");
            if (rule.has("level")) {
                validateLevel(number(rule, "level"));
            }
            if (rule.has("minimumLevel")) {
                validateLevel(number(rule, "minimumLevel"));
            }
        }
    }

    private static Set<String> creditCodes(JsonObject rule) {
        Set<String> codes = new HashSet<>();
        for (JsonElement value : rule.getAsJsonArray("codes")) {
            String code = string(value, "codes");
            require(code.matches(COURSE_CODE) && codes.add(code), "Invalid or duplicate eligible course code");
        }
        return codes;
    }

    private static void validateLevel(BigDecimal value) {
        require(value.compareTo(BigDecimal.valueOf(100)) >= 0 && value.compareTo(BigDecimal.valueOf(900)) <= 0
                && value.stripTrailingZeros().scale() <= 0 && validLevel(value.intValue()), "Invalid course level");
    }

    private static boolean validLevel(int level) {
        return level >= 100 && level <= 900 && level % 100 == 0;
    }

    private static String string(JsonObject object, String key) {
        return string(object.get(key), key);
    }

    private static String string(JsonElement value, String key) {
        require(value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString(), "Expected text for " + key);
        return value.getAsString();
    }

    private static BigDecimal number(JsonObject object, String key) {
        JsonElement value = object.get(key);
        require(value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber(), "Expected number for " + key);
        return value.getAsBigDecimal();
    }

    private static void require(boolean condition, String error) {
        if (!condition) {
            throw new IllegalArgumentException(error);
        }
    }
}
