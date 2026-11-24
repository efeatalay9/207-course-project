package dataaccess.catalog;

import org.junit.jupiter.api.Test;
import support.CatalogFixtures;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryOfferingLookupTest {
    @Test
    void findsOfferingsByIdAndReportsMissingOnes() {
        InMemoryOfferingLookup lookup = new InMemoryOfferingLookup(List.of(
                CatalogFixtures.offering("fall", "CSC108H1", "20269"),
                CatalogFixtures.offering("winter", "CSC108H1", "20271")));
        assertEquals("20271", lookup.findById("winter").orElseThrow().sessions().get(0));
        assertTrue(lookup.findById("missing").isEmpty());
    }
}
