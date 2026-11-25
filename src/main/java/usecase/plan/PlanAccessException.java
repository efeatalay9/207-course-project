package usecase.plan;

/** Reports that a saved plan could not be written or read. The message is shown to students. */
public class PlanAccessException extends Exception {
  private static final long serialVersionUID = 1L;

  /**
   * Creates the exception.
   *
   * @param message a short explanation for the student
   */
  public PlanAccessException(String message) {
    super(message);
  }

  /**
   * Creates the exception with its underlying cause.
   *
   * @param message a short explanation for the student
   * @param cause the original failure
   */
  public PlanAccessException(String message, Throwable cause) {
    super(message, cause);
  }
}
