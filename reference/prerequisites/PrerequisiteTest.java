import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public final class PrerequisiteTest {
    private static final String CSC108 = "69dd3ea4830c3634bbb69a3b";
    private static final String CSC148 = "69dd3ea4830c3634bbb69a3f";
    private static final String CSC207 = "69dd3ea4830c3634bbb69a45";
    private static final String CSC209 = "69dd3ea4830c3634bbb69a46";
    private static final String CSC263 = "69dd3ea4830c3634bbb69a4c";
    private static final String AFR260 = "69dd3ea4830c3634bbb697a3";
    private static final String MAT257 = "69dd3ea4830c3634bbb6a072";
    private static final String MGT480 = "69d94299830c3634bbb694a6";
    private static final String ECO499 = "69dd3ea4830c3634bbb69bbc";
    private static final String ENG201 = "69dd3ea4830c3634bbb69c0e";
    private static final String SOC305 = "69dd3ea4830c3634bbb6a5a3";
    private static final String ARC426 = "6a26f5d65da4a842c5b125fe";
    private static final String ECO200 = "69d94299830c3634bbb6918d";
    private static final String CDN420 = "69dd3ea4830c3634bbb698de";
    private static final String CRI365 = "69dd3ea4830c3634bbb699ef";
    private static final String SOC332 = "69dd3ea4830c3634bbb6a5a7";
    private static final String CSB429 = "69dd3ea4830c3634bbb69a20";
    private static final String ACMD01 = "69f121157db0610dec33b94a";
    private static final String PSY352 = "69eb719d7db0610dec33b854";

    public static void main(String[] args) throws Exception {
        byte[] file = Files.readAllBytes(Path.of(args[0]));
        PrerequisiteCatalog catalog = PrerequisiteCatalog.fromJson(file);
        testCatalogue(file, catalog);
        if (args.length == 3 && args[2].equals("--http")) {
            testHttp(file, Files.readAllBytes(Path.of(args[1])));
        }
    }

    private static void testCatalogue(byte[] file, PrerequisiteCatalog catalog) throws IOException {
        require(catalog.size() == 8_172, "Expected the complete 2026-09-05 capture");
        PrerequisiteCatalog.Profile empty = profile(Set.of(), Map.of(), true, null, Map.of());
        PrerequisiteCatalog.Profile incomplete = profile(Set.of(), Map.of(), false, null, Map.of());

        check(catalog, CSC209, "CSC209H1", completed("CSC207H5"), PrerequisiteCatalog.Status.MET);
        check(catalog, CSC209, "CSC209H1", completed("CSC148H1"), PrerequisiteCatalog.Status.NOT_MET);
        check(catalog, CSC209, "CSC209H1", incomplete, PrerequisiteCatalog.Status.NEEDS_REVIEW);
        check(catalog, CSC209, "CSC209H5", empty, PrerequisiteCatalog.Status.NEEDS_REVIEW);
        check(catalog, "missing-offering", "CSC209H1", empty, PrerequisiteCatalog.Status.NEEDS_REVIEW);

        check(catalog, CSC207, "CSC207H1", csc148Grade("60"), PrerequisiteCatalog.Status.MET);
        check(catalog, CSC207, "CSC207H1", csc148Grade("59.99"), PrerequisiteCatalog.Status.NOT_MET);
        check(catalog, CSC207, "CSC207H1", profile(Set.of("CSC148H1"), Map.of("CSC148H1", new BigDecimal("59.99")), false, null, Map.of()), PrerequisiteCatalog.Status.NEEDS_REVIEW);
        check(catalog, CSC207, "CSC207H1", profile(Set.of("CSC148H1", "CSC111H1"), Map.of("CSC148H1", new BigDecimal("59"), "CSC111H1", new BigDecimal("60")), false, null, Map.of()), PrerequisiteCatalog.Status.MET);
        check(catalog, CSC207, "CSC207H1", completed("CSC148H1"), PrerequisiteCatalog.Status.NEEDS_REVIEW);
        check(catalog, CSC207, "CSC207H1", completed("CSC148H3"), PrerequisiteCatalog.Status.NOT_MET);
        check(catalog, CSC207, "CSC207H1", profile(Set.of("CSCA48H3"), Map.of("CSCA48H3", new BigDecimal("65")), true, null, Map.of()), PrerequisiteCatalog.Status.MET);

        check(catalog, CSC148, "CSC148H1", completed("CSC108H1"), PrerequisiteCatalog.Status.MET);
        check(catalog, CSC148, "CSC148H1", empty, PrerequisiteCatalog.Status.NEEDS_REVIEW);
        check(catalog, CSC148, "CSC148H1", profile(Set.of(), Map.of(), true, null, Map.of(CSC148 + ":equivalent-programming-experience", true)), PrerequisiteCatalog.Status.MET);
        check(catalog, CSC148, "CSC148H1", profile(Set.of(), Map.of(), true, null, Map.of(CSC148 + ":equivalent-programming-experience", false)), PrerequisiteCatalog.Status.NOT_MET);

        Set<String> science = Set.of("CSC236H1", "STA237H1");
        Set<String> engineering = Set.of("APS105H1", "ECE302H1");
        check(catalog, CSC263, "CSC263H1", profile(science, Map.of(), true, null, Map.of("faculty.is-engineering", false)), PrerequisiteCatalog.Status.MET);
        check(catalog, CSC263, "CSC263H1", profile(science, Map.of(), true, null, Map.of("faculty.is-engineering", true)), PrerequisiteCatalog.Status.NOT_MET);
        check(catalog, CSC263, "CSC263H1", profile(engineering, Map.of(), true, null, Map.of("faculty.is-engineering", true)), PrerequisiteCatalog.Status.MET);
        check(catalog, CSC263, "CSC263H1", completed("CSC236H1", "STA237H1"), PrerequisiteCatalog.Status.NEEDS_REVIEW);
        check(catalog, CSC263, "CSC263H1", profile(Set.of("APS105H1", "ECE302H1", "CSC236H1", "STA237H1"), Map.of(), true, null, Map.of()), PrerequisiteCatalog.Status.MET);
        check(catalog, CSC263, "CSC263H1", profile(Set.of("APS105H1", "ECE302H1"), Map.of(), true, null, Map.of()), PrerequisiteCatalog.Status.NEEDS_REVIEW);
        check(catalog, CSC263, "CSC263H1", empty, PrerequisiteCatalog.Status.NOT_MET);

        check(catalog, AFR260, "AFR260H1", profile(Set.of(), Map.of(), true, "2.0", Map.of()), PrerequisiteCatalog.Status.MET);
        check(catalog, AFR260, "AFR260H1", profile(Set.of(), Map.of(), true, "1.5", Map.of()), PrerequisiteCatalog.Status.NOT_MET);
        check(catalog, AFR260, "AFR260H1", empty, PrerequisiteCatalog.Status.NEEDS_REVIEW);
        check(catalog, CSC108, "CSC108H1", empty, PrerequisiteCatalog.Status.NONE_LISTED);

        check(catalog, MAT257, "MAT257Y1", completed("MAT157Y1"), PrerequisiteCatalog.Status.NEEDS_REVIEW);
        check(catalog, ECO499, "ECO499H1", empty, PrerequisiteCatalog.Status.NEEDS_REVIEW);
        check(catalog, MGT480, "MGT480H5", empty, PrerequisiteCatalog.Status.NEEDS_REVIEW);
        require(catalog.check(MGT480, "MGT480H5", empty).notes().get(0).contains("Only during 2026-27"), "Term-specific waiver evidence was dropped");
        check(catalog, ACMD01, "ACMD01H3", empty, PrerequisiteCatalog.Status.NEEDS_REVIEW);
        require(catalog.check(ACMD01, "ACMD01H3", empty).notes().get(0).contains("Permission of ACM Program Manager"),
                "Course approval evidence was dropped");

        testAcademicThresholds(catalog);
        testRuleValidation(file);

        System.out.println("Catalogue tests passed: 8,172 offerings validated, real-case outcomes checked, malformed rules rejected.");
    }

    private static void testAcademicThresholds(PrerequisiteCatalog catalog) {
        check(catalog, ENG201, "ENG201H1", profile(Set.of(), Map.of(), true, "4.0", Map.of()),
                PrerequisiteCatalog.Status.MET);
        check(catalog, ENG201, "ENG201H1", transcript(Map.of("ENG202H1", credit("ENG", 200, "1")), false),
                PrerequisiteCatalog.Status.MET);
        check(catalog, SOC305, "SOC305H1", transcript(
                Map.of("SOC201H1", credit("SOC", 200, "0.5"), "SOC311H1", credit("SOC", 300, "0.5")), true),
                PrerequisiteCatalog.Status.MET);
        check(catalog, SOC305, "SOC305H1", transcript(Map.of("SOC100H1", credit("SOC", 100, "1")), true),
                PrerequisiteCatalog.Status.NOT_MET);
        check(catalog, SOC305, "SOC305H1", transcript(Map.of("SOC100H1", credit("SOC", null, "1")), true),
                PrerequisiteCatalog.Status.NEEDS_REVIEW);
        check(catalog, SOC305, "SOC305H1", transcript(Map.of("ENG202H1", credit("ENG", null, "1")), true),
                PrerequisiteCatalog.Status.NOT_MET);
        check(catalog, SOC305, "SOC305H1", completed("SOC201H1", "SOC311H1"),
                PrerequisiteCatalog.Status.NEEDS_REVIEW);
        check(catalog, ARC426, "ARC426H1", transcript(Map.of("ARC400H1", credit("ARC", 400, "1")), true),
                PrerequisiteCatalog.Status.NOT_MET);
        check(catalog, ARC426, "ARC426H1", transcript(
                Map.of("ARC301H1", credit("ARC", 300, "0.5"), "ARC302H1", credit("ARC", 300, "0.5")), true),
                PrerequisiteCatalog.Status.MET);
        check(catalog, CRI365, "CRI365H1", transcript(
                Map.of("CRI205H1", credit(null, null, "0.5"), "CRI210H1", credit(null, null, "0.5")), true),
                PrerequisiteCatalog.Status.MET);
        check(catalog, CRI365, "CRI365H1", transcript(
                Map.of("CRI205H1", credit("CRI", 200, "0.5"), "CRI399H1", credit("CRI", 300, "0.5")), true),
                PrerequisiteCatalog.Status.NOT_MET);
        check(catalog, SOC332, "SOC332H1", transcript(Map.of(
                "SOC201H1", credit("SOC", 200, "0.5"), "SOC202H1", credit("SOC", 200, "0.5"),
                "SOC204H1", credit("SOC", 200, "0.5"), "SOC252H1", credit("SOC", 200, "0.5"),
                "SOC254H1", credit("SOC", 200, "0.5")), true), PrerequisiteCatalog.Status.MET);
        check(catalog, CDN420, "CDN420Y1", profileWithGpa("15.0", "3.0"), PrerequisiteCatalog.Status.MET);
        check(catalog, CDN420, "CDN420Y1", profileWithGpa("15.0", "2.99"), PrerequisiteCatalog.Status.NOT_MET);
        check(catalog, CDN420, "CDN420Y1", profileWithGpa("15.0", null), PrerequisiteCatalog.Status.NEEDS_REVIEW);
        check(catalog, ECO200, "ECO200Y5", profile(Set.of("ECO101H5", "ECO102H5"),
                Map.of("ECO101H5", new BigDecimal("63"), "ECO102H5", new BigDecimal("63")), true, null, Map.of()),
                PrerequisiteCatalog.Status.MET);
        check(catalog, ECO200, "ECO200Y5", profile(Set.of("ECO101H5", "ECO102H5"),
                Map.of("ECO101H5", new BigDecimal("63"), "ECO102H5", new BigDecimal("62.99")), true, null, Map.of()),
                PrerequisiteCatalog.Status.NOT_MET);
        check(catalog, CSB429, "CSB429H1", completed("CSB349H1"), PrerequisiteCatalog.Status.NEEDS_REVIEW);
        check(catalog, CSB429, "CSB429H1", profile(Set.of("CSB349H1"),
                Map.of("CSB349H1", new BigDecimal("76")), true, null, Map.of()), PrerequisiteCatalog.Status.NOT_MET);
        check(catalog, CSB429, "CSB429H1", profile(Set.of("CSB349H1"),
                Map.of("CSB349H1", new BigDecimal("77")), true, null, Map.of()), PrerequisiteCatalog.Status.MET);
        Map<String, Boolean> equivalent = Map.of(PSY352 + ":approved-equivalent-to-psy201h5", true);
        check(catalog, PSY352, "PSY352H5", profile(Set.of("PSY290H5"), Map.of(), true, null, equivalent),
                PrerequisiteCatalog.Status.MET);
        check(catalog, PSY352, "PSY352H5", profile(Set.of(), Map.of(), true, null, equivalent),
                PrerequisiteCatalog.Status.NOT_MET);

        PrerequisiteCatalog.Result archived = catalog.check(CSC207, "CSC207H1", csc148Grade("65"));
        PrerequisiteCatalog.Result changed = catalog.checkCurrent(CSC207, "CSC207H1", archived.publishedText().replace("60%", "70%"),
                List.of(), csc148Grade("65"));
        require(changed.status() == PrerequisiteCatalog.Status.NEEDS_REVIEW && changed.publishedText().contains("70%"),
                "A stale rule gave a definite result for changed wording");
        require(catalog.checkCurrent(CSC207, "CSC207H1", archived.publishedText(), List.of("New approval required"),
                csc148Grade("65")).status() == PrerequisiteCatalog.Status.NEEDS_REVIEW, "A changing note was ignored");
        require(catalog.checkCurrent(CSC207, "CSC207H1", archived.publishedText(), List.of(),
                csc148Grade("65")).status() == PrerequisiteCatalog.Status.MET, "Matching current rule failed");
    }

    private static void testRuleValidation(byte[] file) throws IOException {
        JsonObject malformed = json(file);
        findRule(malformed, CSC207).addProperty("op", "run-code");
        expectInvalid(bytes(malformed));
        malformed = json(file);
        malformed.addProperty("version", 3);
        expectInvalid(bytes(malformed));
        malformed = json(file);
        findRule(malformed, SOC305).addProperty("level", 250);
        expectInvalid(bytes(malformed));
        malformed = json(file);
        findRule(malformed, CRI365).getAsJsonArray("codes").add("CRI205H1");
        expectInvalid(bytes(malformed));
    }

    private static void testHttp(byte[] file, byte[] updatedFile) throws Exception {
        AtomicReference<byte[]> published = new AtomicReference<>(file);
        List<String> requests = new CopyOnWriteArrayList<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/prerequisites.json", exchange -> {
            try (exchange) {
                int bodySize = exchange.getRequestBody().readAllBytes().length;
                requests.add(exchange.getRequestMethod() + " " + exchange.getRequestURI() + " body=" + bodySize
                        + " authorization=" + exchange.getRequestHeaders().containsKey("Authorization")
                        + " cookie=" + exchange.getRequestHeaders().containsKey("Cookie"));
                byte[] response = published.get();
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length);
                exchange.getResponseBody().write(response);
            }
        });
        server.start();
        try {
            URI endpoint = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/prerequisites.json");
            PrerequisiteCatalog initial = PrerequisiteCatalog.fetch(endpoint);
            check(initial, CSC207, "CSC207H1", csc148Grade("65"), PrerequisiteCatalog.Status.MET);
            check(initial, CSC207, "CSC207H1", csc148Grade("55"), PrerequisiteCatalog.Status.NOT_MET);
            require(requests.size() == 1, "An evaluation sent a network request");

            // Pick up the file the compiler produced from the edited source sentence.
            published.set(updatedFile);
            PrerequisiteCatalog refreshed = PrerequisiteCatalog.fetch(endpoint);
            check(refreshed, CSC207, "CSC207H1", csc148Grade("65"), PrerequisiteCatalog.Status.NOT_MET);
            check(refreshed, CSC207, "CSC207H1", csc148Grade("70"), PrerequisiteCatalog.Status.MET);

            published.set("{invalid".getBytes(StandardCharsets.UTF_8));
            try {
                PrerequisiteCatalog.fetch(endpoint);
                throw new AssertionError("Accepted malformed refresh");
            } catch (IOException expected) {
                check(refreshed, CSC207, "CSC207H1", csc148Grade("70"), PrerequisiteCatalog.Status.MET);
            }
            require(requests.size() == 3, "Expected three explicit refresh requests");
            for (String request : requests) {
                require(request.equals("GET /prerequisites.json body=0 authorization=false cookie=false"), "Unexpected request: " + request);
            }
            System.out.println("HTTP test passed: three identical bodyless GETs, local outcomes changed after refresh, malformed update rejected.");
        } finally {
            server.stop(0);
        }
    }

    private static PrerequisiteCatalog.Profile completed(String... codes) {
        return profile(Set.of(codes), Map.of(), true, null, Map.of());
    }

    private static PrerequisiteCatalog.Profile csc148Grade(String grade) {
        return profile(Set.of("CSC148H1"), Map.of("CSC148H1", new BigDecimal(grade)), true, null, Map.of());
    }

    private static PrerequisiteCatalog.Profile profile(Set<String> completed, Map<String, BigDecimal> grades,
                                                       boolean complete, String credits, Map<String, Boolean> facts) {
        return new PrerequisiteCatalog.Profile(completed, grades, complete, credits == null ? null : new BigDecimal(credits), facts);
    }

    private static PrerequisiteCatalog.EarnedCourse credit(String subject, Integer level, String amount) {
        return new PrerequisiteCatalog.EarnedCourse(subject, level, new BigDecimal(amount));
    }

    private static PrerequisiteCatalog.Profile transcript(Map<String, PrerequisiteCatalog.EarnedCourse> courses, boolean complete) {
        return new PrerequisiteCatalog.Profile(courses.keySet(), Map.of(), complete, null, Map.of(), courses, null);
    }

    private static PrerequisiteCatalog.Profile profileWithGpa(String credits, String gpa) {
        return new PrerequisiteCatalog.Profile(Set.of(), Map.of(), true, new BigDecimal(credits), Map.of(), Map.of(),
                gpa == null ? null : new BigDecimal(gpa));
    }

    private static void check(PrerequisiteCatalog catalog, String offering, String code,
                              PrerequisiteCatalog.Profile profile, PrerequisiteCatalog.Status expected) {
        PrerequisiteCatalog.Result result = catalog.check(offering, code, profile);
        require(result.status() == expected, code + " expected " + expected + " but got " + result);
    }

    private static JsonObject json(byte[] data) {
        return JsonParser.parseString(new String(data, StandardCharsets.UTF_8)).getAsJsonObject();
    }

    private static byte[] bytes(JsonObject document) {
        return document.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static JsonObject findRule(JsonObject document, String id) {
        for (JsonElement element : document.getAsJsonArray("offerings")) {
            JsonObject offering = element.getAsJsonObject();
            if (offering.get("id").getAsString().equals(id)) {
                return offering.getAsJsonObject("rule");
            }
        }
        throw new AssertionError("Missing test offering " + id);
    }

    private static void expectInvalid(byte[] data) throws IOException {
        try {
            PrerequisiteCatalog.fromJson(data);
            throw new AssertionError("Accepted invalid file");
        } catch (IOException expected) {
            require(expected.getMessage().startsWith("Invalid prerequisite file"), "Wrong validation failure");
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
