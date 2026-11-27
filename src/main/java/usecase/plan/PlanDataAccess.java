package usecase.plan;

import domain.SavedPlan;

/**
 * Stores and reads saved timetable plans.
 *
 * <p>The use cases only know a location chosen by the student. The data access layer decides the
 * file format, so the format can change without touching the interactors.
 */
public interface PlanDataAccess {
  /**
   * Writes a plan, replacing anything already stored at the location.
   *
   * @param location where to store the plan, for example a file path
   * @param plan the snapshot to store
   * @throws PlanAccessException if the plan cannot be written
   */
  void save(String location, SavedPlan plan) throws PlanAccessException;

  /**
   * Reads a complete plan.
   *
   * @param location where the plan is stored
   * @return the stored snapshot
   * @throws PlanAccessException if the plan is missing, unreadable, or not a valid plan
   */
  SavedPlan load(String location) throws PlanAccessException;
}
