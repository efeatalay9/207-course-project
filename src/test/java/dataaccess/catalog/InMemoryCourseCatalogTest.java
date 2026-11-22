package dataaccess.catalog;

import org.junit.jupiter.api.Test;
import domain.CourseOffering;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;
import static support.CatalogFixtures.offering;

// Contract tests for the production catalogue implementation.
class InMemoryCourseCatalogTest {
    @Test
    void exactLookupRetainsAllOfferingsAndDoesNotMatchPrefixes() {
        InMemoryCourseCatalog catalog = new InMemoryCourseCatalog(List.of(offering("f", "CSC108H1", "20269"),
                offering("s", "csc108h1", "20271"), offering("other", "CSC207H1", "20269")));
        assertEquals(2, catalog.findByCode("CSC108H1").size());
        assertTrue(catalog.findByCode("CSC108").isEmpty());
        assertTrue(catalog.findByCode("UNKNOWN").isEmpty());
    }

    @Test
    void indexAndResultsAreIndependentOfCallerMutations() {
        List<CourseOffering> source = new ArrayList<>(List.of(offering("f", "CSC108H1", "20269")));
        InMemoryCourseCatalog catalog = new InMemoryCourseCatalog(source);
        source.clear();
        assertEquals(1, catalog.findByCode("CSC108H1").size());
        assertThrows(UnsupportedOperationException.class, () -> catalog.findByCode("CSC108H1").clear());
    }

    @Test
    void duplicateIdentityFailsInsteadOfOverwritingData() {
        assertThrows(IllegalArgumentException.class, () -> new InMemoryCourseCatalog(List.of(
                offering("same", "CSC108H1", "20269"), offering("same", "CSC108H1", "20271"))));
    }

    @Test
    void prefixLookupSortsUnorderedInputAndDeduplicatesCodesAcrossOfferings() {
        List<CourseOffering> source = new ArrayList<>(List.of(offering("207", "CSC207H1", "20269"),
                offering("108f", "csc108h1", "20269"), offering("148", "CSC148H1", "20269"),
                offering("108s", "CSC108H1", "20271"), offering("mat", "MAT137Y1", "20269")));
        InMemoryCourseCatalog catalog = new InMemoryCourseCatalog(source);
        source.clear();
        assertEquals(List.of("CSC108H1", "CSC148H1", "CSC207H1"), catalog.findCodesByPrefix("CSC"));
        assertEquals(List.of("CSC108H1", "CSC148H1"), catalog.findCodesByPrefix("CSC1"));
        assertEquals(List.of("CSC108H1"), catalog.findCodesByPrefix("CSC108H1"));
        assertEquals(2, catalog.findByCode("CSC108H1").size());
        assertThrows(UnsupportedOperationException.class, () -> catalog.findCodesByPrefix("CSC").clear());
    }

    @Test
    void lowerBoundHandlesBothEndsGapsNoMatchesAndEmptyInputs() {
        InMemoryCourseCatalog catalog = new InMemoryCourseCatalog(List.of(offering("a", "AAA100H1", "20269"),
                offering("z", "ZZZ999H1", "20269")));
        assertEquals(List.of("AAA100H1"), catalog.findCodesByPrefix("A"));
        assertEquals(List.of("ZZZ999H1"), catalog.findCodesByPrefix("ZZZ999H1"));
        for (String prefix : List.of("", "0", "B", "ZZZ999H1X", "ZZZZ", "100")) {
            assertTrue(catalog.findCodesByPrefix(prefix).isEmpty(), prefix);
        }
        assertTrue(new InMemoryCourseCatalog(List.of()).findCodesByPrefix("CSC").isEmpty());
    }

    @Test
    void binarySearchMatchesLinearReferenceAcrossShuffledInputWithoutTruncatingResults() {
        List<CourseOffering> source = new ArrayList<>();
        for (int i = 0; i < 350; i++) {
            source.add(offering("id" + i, String.format(java.util.Locale.ROOT, "CSC%03dH1", i), "20269"));
        }
        Collections.shuffle(source, new Random(207));
        InMemoryCourseCatalog catalog = new InMemoryCourseCatalog(source);
        List<String> prefixes = new ArrayList<>(List.of("C", "CSC", "CSC0", "CSC1", "CSC3", "CSC35", "ZZZ"));
        for (CourseOffering offering : source) {
            prefixes.add(offering.code());
            prefixes.add(offering.code().substring(0, 5));
        }
        for (String prefix : prefixes) {
            List<String> expected = source.stream().map(CourseOffering::code)
                    .filter(code -> code.startsWith(prefix)).distinct().sorted().toList();
            assertEquals(expected, catalog.findCodesByPrefix(prefix), prefix);
        }
        assertEquals(350, catalog.findCodesByPrefix("CSC").size());
    }
}
