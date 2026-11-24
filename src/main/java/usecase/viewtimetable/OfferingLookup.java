package usecase.viewtimetable;

import domain.CourseOffering;
import java.util.Optional;
import usecase.common.CatalogAccessException;

/**
 * Looks up an offering for adding or displaying a timetable section.
 *
 * <p>The timetable stores offering IDs and section names. The use cases look up the offering to
 * validate a selection or recover its meeting times.
 */
public interface OfferingLookup {
  /**
   * Finds an offering by its unique ID.
   *
   * @param offeringId the offering ID stored in the timetable
   * @return the offering, or empty if the catalogue no longer contains it
   * @throws CatalogAccessException if the catalogue cannot be read
   */
  Optional<CourseOffering> findById(String offeringId) throws CatalogAccessException;
}
