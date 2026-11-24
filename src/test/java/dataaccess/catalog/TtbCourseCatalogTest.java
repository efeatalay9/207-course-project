package dataaccess.catalog;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import domain.CourseOffering;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import usecase.common.CatalogAccessException;
import usecase.common.CatalogueScope;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TtbCourseCatalogTest {
    private static final CatalogueScope FALL_ARTS = new CatalogueScope("20269", "ARTSC");

    @Test
    void startupGetsOnlyActualTermAndDivisionChoices() throws Exception {
        List<URI> calls = new ArrayList<>();
        TtbCourseCatalog catalog = new TtbCourseCatalog((uri, body) -> {
            calls.add(uri);
            assertEquals("/ttb/reference-data", uri.getPath());
            assertNull(body);
            return success("""
                    {"currentSessions":[{"header":true,"value":"Fall-Winter","label":"Academic year"},
                    {"header":false,"value":"20269","label":"Fall 2026"}],
                    "divisions":[{"header":false,"value":"ARTSC","label":"Faculty of Arts and Science"}]}
                    """);
        });

        TtbCourseCatalog.Filters filters = catalog.fetchFilters();

        assertEquals(List.of(new TtbCourseCatalog.Option("20269", "Fall 2026")), filters.sessions());
        assertEquals(List.of(new TtbCourseCatalog.Option("ARTSC", "Faculty of Arts and Science")), filters.divisions());
        assertThrows(CatalogAccessException.class, () -> catalog.findByCode("CSC108H1"));
        assertThrows(CatalogAccessException.class, () -> catalog.findCodesByPrefix("CSC", null));
        assertEquals(1, calls.size());
    }

    @Test
    void searchUsesTheScopeAndPagesAndKeepsPreviouslySelectedOfferingsAvailable() throws Exception {
        List<JsonObject> requests = new ArrayList<>();
        TtbCourseCatalog catalog = new TtbCourseCatalog((uri, body) -> {
            assertEquals("/ttb/getPageableCourses", uri.getPath());
            JsonObject request = JsonParser.parseString(body).getAsJsonObject();
            requests.add(request);
            int page = request.get("page").getAsInt();
            String code = request.getAsJsonObject("courseCodeAndTitleProps").get("courseCode").getAsString();
            if (code.equals("CSC148H1")) {
                return page(1, 1, offering("winter", code, "20271"));
            }
            return page(page, 2, offering("fall-" + page, code, "20269"));
        });

        List<CourseOffering> fall = catalog.findByCode("CSC108H1", FALL_ARTS);
        List<CourseOffering> winter = catalog.findByCode("CSC148H1", new CatalogueScope("20271", "ARTSC"));

        assertEquals(List.of("fall-1", "fall-2"), fall.stream().map(CourseOffering::id).toList());
        assertEquals("winter", winter.get(0).id());
        assertEquals("CSC108H1", catalog.findById("fall-1").orElseThrow().code());
        assertEquals(List.of(1, 2, 1), requests.stream().map(r -> r.get("page").getAsInt()).toList());
        assertEquals("20269", requests.get(0).getAsJsonArray("sessions").get(0).getAsString());
        assertEquals("ARTSC", requests.get(0).getAsJsonArray("divisions").get(0).getAsString());
        assertEquals("20271", requests.get(2).getAsJsonArray("sessions").get(0).getAsString());
        assertFalse(requests.get(0).get("availableSpace").getAsBoolean());
    }

    @Test
    void discardsAnIncompleteSearchInsteadOfCachingItsFirstPage() {
        TtbCourseCatalog catalog = new TtbCourseCatalog((uri, body) -> {
            int page = JsonParser.parseString(body).getAsJsonObject().get("page").getAsInt();
            return page == 1 ? page(1, 2, offering("partial", "CSC108H1", "20269")) : page(2, 2, "");
        });

        assertThrows(CatalogAccessException.class, () -> catalog.findByCode("CSC108H1", FALL_ARTS));
        assertTrue(catalog.findById("partial").isEmpty());
    }

    @Test
    void suggestionsStayScopedAndReturnOnlyDistinctExactPrefixes() throws Exception {
        List<URI> calls = new ArrayList<>();
        TtbCourseCatalog catalog = new TtbCourseCatalog((uri, body) -> {
            calls.add(uri);
            assertNull(body);
            return success("""
                    {"codesAndTitles":[{"code":"CSC148H1","division":{"code":"ARTSC"}},
                    {"code":"CSC148H1","division":{"code":"ARTSC"}},
                    {"code":"CSCA08H3","division":{"code":"SCAR"}},
                    {"code":"MAT137Y1","division":{"code":"ARTSC"}},
                    {"code":"CSC108H1","division":{"code":"ARTSC"}}]}
                    """);
        });

        assertTrue(catalog.findCodesByPrefix("CS", FALL_ARTS).isEmpty());
        assertEquals(List.of("CSC108H1", "CSC148H1"), catalog.findCodesByPrefix("CSC", FALL_ARTS));
        assertEquals(1, calls.size());
        assertEquals("/ttb/getOptimizedMatchingCourseTitles", calls.get(0).getPath());
        assertTrue(calls.get(0).getQuery().contains("term=CSC&divisions=ARTSC&sessions=20269"));
    }

    @Test
    void emptyResultsAndServiceFailuresStayDifferent() throws Exception {
        TtbCourseCatalog empty = new TtbCourseCatalog((uri, json) -> page(1, 0, ""));
        TtbCourseCatalog noMatches = new TtbCourseCatalog((uri, json) -> new TtbCourseCatalog.Response(404, """
                {"payload":null,"status":[{"code":4404,"message":"No results found. Please try modifying your search criteria."}]}
                """));
        TtbCourseCatalog wrongEndpoint = new TtbCourseCatalog((uri, json) -> new TtbCourseCatalog.Response(404, """
                {"payload":null,"status":[{"code":404}]}
                """));
        TtbCourseCatalog rejected = new TtbCourseCatalog((uri, json) -> new TtbCourseCatalog.Response(200, """
                {"status":[{"code":1}],"payload":null}
                """));
        TtbCourseCatalog offline = new TtbCourseCatalog((uri, json) -> {
            throw new IOException("timed out");
        });

        assertTrue(empty.findByCode("CSC108H1", FALL_ARTS).isEmpty());
        assertTrue(noMatches.findByCode("CSC108H1", FALL_ARTS).isEmpty());
        assertThrows(CatalogAccessException.class, () -> wrongEndpoint.findByCode("CSC108H1", FALL_ARTS));
        assertThrows(CatalogAccessException.class, () -> rejected.findByCode("CSC108H1", FALL_ARTS));
        assertThrows(CatalogAccessException.class, offline::fetchFilters);
    }

    private static TtbCourseCatalog.Response success(String payload) {
        return new TtbCourseCatalog.Response(200, "{\"status\":[{\"code\":0}],\"payload\":" + payload + "}");
    }

    private static TtbCourseCatalog.Response page(int page, int total, String records) {
        return success("""
                {"pageableCourse":{"page":%d,"total":%d,"courses":[%s]}}
                """.formatted(page, total, records));
    }

    private static String offering(String id, String code, String session) {
        return """
                {"id":"%s","code":"%s","name":"Test course","sessions":["%s"],"sections":[]}
                """.formatted(id, code, session);
    }
}
