package usecase.saveplan;

import java.time.Instant;

/**
 * Describes a plan that was saved.
 *
 * @param location where the plan was saved
 * @param sectionCount how many sections the plan holds
 * @param savedAt when the plan was saved
 */
public record SavePlanResponse(String location, int sectionCount, Instant savedAt) {}
