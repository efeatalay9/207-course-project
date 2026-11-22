package dataaccess.catalog;

import domain.CourseOffering;
import usecase.searchcourses.CourseCatalog;
import usecase.suggestcoursecodes.CourseCodeIndex;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

// An immutable index built once; each lookup only visits offerings for one code.
public final class InMemoryCourseCatalog implements CourseCatalog, CourseCodeIndex {
    private final Map<String, List<CourseOffering>> offeringsByCode;
    private final List<String> sortedCodes;

    // Duplicate IDs indicate corrupt input; duplicate codes are expected and preserved.
    public InMemoryCourseCatalog(List<CourseOffering> offerings) {
        Map<String, List<CourseOffering>> index = new HashMap<>();
        Set<String> ids = new HashSet<>();
        for (CourseOffering offering : offerings) {
            if (!ids.add(offering.id())) {
                throw new IllegalArgumentException("Duplicate offering ID: " + offering.id());
            }
            String code = offering.code().strip().toUpperCase(Locale.ROOT);
            index.computeIfAbsent(code, ignored -> new ArrayList<>()).add(offering);
        }
        index.replaceAll((code, matches) -> List.copyOf(matches));
        offeringsByCode = Map.copyOf(index);
        // REQUIRED INVARIANT: binary search needs distinct, normalized codes in natural String order.
        // Never trust the input file's ordering. Build once and keep immutable; rebuild on data updates.
        sortedCodes = index.keySet().stream().sorted().toList();
    }

    // The use case supplies normalized input. Results and the index cannot be mutated.
    @Override
    public List<CourseOffering> findByCode(String normalizedCode) {
        return offeringsByCode.getOrDefault(normalizedCode, List.of());
    }

    // O(log n + k): binary search the first possible match, then visit only the k matches.
    // Prefix matches form one contiguous range because sortedCodes uses the same comparator.
    @Override
    public List<String> findCodesByPrefix(String normalizedPrefix) {
        if (normalizedPrefix.isEmpty()) {
            return List.of();
        }
        int first = lowerBound(normalizedPrefix);
        int end = first;
        while (end < sortedCodes.size() && sortedCodes.get(end).startsWith(normalizedPrefix)) {
            end++;
        }
        return List.copyOf(sortedCodes.subList(first, end));
    }

    // Return the first index whose code is >= prefix, including insertion points after the last code.
    private int lowerBound(String prefix) {
        int low = 0;
        int high = sortedCodes.size();
        while (low < high) {
            int middle = low + (high - low) / 2;
            if (sortedCodes.get(middle).compareTo(prefix) < 0) {
                low = middle + 1;
            } else {
                high = middle;
            }
        }
        return low;
    }
}
