package usecase.saveplan;

/**
 * Says where the student wants to save their plan.
 *
 * @param location the chosen location, for example a file path
 */
public record SavePlanRequest(String location) {}
