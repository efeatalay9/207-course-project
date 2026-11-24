package dataaccess.catalog;

import domain.CourseOffering;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import usecase.viewtimetable.OfferingLookup;

/**
 * Finds offerings by ID from catalogue data that is already loaded in memory.
 *
 * <p>Offering IDs are unique; the catalogue loader rejects duplicates, so a simple map is enough.
 */
public final class InMemoryOfferingLookup implements OfferingLookup {
    private final Map<String, CourseOffering> offeringsById = new HashMap<>();

    /**
     * Indexes the given offerings by ID.
     *
     * @param offerings the loaded catalogue
     */
    public InMemoryOfferingLookup(List<CourseOffering> offerings) {
        for (CourseOffering offering : offerings) {
            offeringsById.put(offering.id(), offering);
        }
    }

    @Override
    public Optional<CourseOffering> findById(String offeringId) {
        return Optional.ofNullable(offeringsById.get(offeringId));
    }
}
