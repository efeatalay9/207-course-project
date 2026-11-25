package interfaceadapter.plan;

/** Small text helpers shared by the save and open presenters. */
public final class PlanLabels {
  private PlanLabels() {}

  /**
   * Returns the last part of a file path so status messages stay short.
   *
   * @param location a file path using either separator
   * @return the file name, or the whole location when it has no separator
   */
  public static String fileName(String location) {
    if (location == null) {
      return "the plan";
    }
    int slash = Math.max(location.lastIndexOf('/'), location.lastIndexOf('\\'));
    return location.substring(slash + 1);
  }

  /**
   * Describes a number of sections, for example "1 section" or "3 sections".
   *
   * @param count the number of sections
   * @return the count with the right noun
   */
  public static String sections(int count) {
    return count + (count == 1 ? " section" : " sections");
  }
}
