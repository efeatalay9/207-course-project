package support;

import dataaccess.catalog.JsonCatalogLoader;
import domain.CourseOffering;
import domain.StatusFlag;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

// Small fixtures keep tests independent of the full production catalogue.
public final class CatalogFixtures {
    private CatalogFixtures() { }

    // Read the same fictional sample used by the manual tester.
    public static List<CourseOffering> sample() throws Exception {
        try (InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(
                CatalogFixtures.class.getResourceAsStream("/sample-courses.json")), StandardCharsets.UTF_8)) {
            return new JsonCatalogLoader().load(reader);
        }
    }

    // A minimal valid offering for tests that do not concern JSON or meeting details.
    public static CourseOffering offering(String id, String code, String session) {
        return new CourseOffering(id, code, "Test course", "St. George", List.of(session),
                "F", StatusFlag.NO, List.of());
    }
}
