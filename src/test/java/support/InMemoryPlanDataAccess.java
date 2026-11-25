package support;

import domain.SavedPlan;
import java.util.HashMap;
import java.util.Map;
import usecase.plan.PlanAccessException;
import usecase.plan.PlanDataAccess;

/** A plan store for interactor tests that keeps plans in memory and can be told to fail. */
public final class InMemoryPlanDataAccess implements PlanDataAccess {
  private final Map<String, SavedPlan> plans = new HashMap<>();
  private PlanAccessException failure;

  /**
   * Makes every later save and load throw the given exception.
   *
   * @param failure the exception to throw, or null to work normally
   */
  public void failWith(PlanAccessException failure) {
    this.failure = failure;
  }

  /**
   * Stores a plan directly, as if it had been saved earlier.
   *
   * @param location the key
   * @param plan the plan
   */
  public void put(String location, SavedPlan plan) {
    plans.put(location, plan);
  }

  /**
   * Returns the stored plan.
   *
   * @param location the key
   * @return the plan, or null if none was saved
   */
  public SavedPlan get(String location) {
    return plans.get(location);
  }

  @Override
  public void save(String location, SavedPlan plan) throws PlanAccessException {
    if (failure != null) {
      throw failure;
    }
    plans.put(location, plan);
  }

  @Override
  public SavedPlan load(String location) throws PlanAccessException {
    if (failure != null) {
      throw failure;
    }
    SavedPlan plan = plans.get(location);
    if (plan == null) {
      throw new PlanAccessException("The plan file does not exist.");
    }
    return plan;
  }
}
