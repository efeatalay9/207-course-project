package domain;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * One scheduled offering of a course.
 *
 * @param id the unique catalogue identifier for this offering
 * @param code the course code, which can appear in more than one offering
 * @param title the published course title
 * @param campus the campus associated with this offering
 * @param sessions the academic sessions for this offering
 * @param termDesignation the catalogue's term label
 * @param cancelled the offering's cancellation status
 * @param sections the lectures, tutorials, and practicals associated with this offering
 */
public record CourseOffering(
    String id,
    String code,
    String title,
    String campus,
    List<String> sessions,
    String termDesignation,
    StatusFlag cancelled,
    List<Section> sections) {
  /** Validates the identifiers and takes immutable copies of the sessions and sections. */
  public CourseOffering {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(code, "code");
    Objects.requireNonNull(title, "title");
    Objects.requireNonNull(campus, "campus");
    Objects.requireNonNull(termDesignation, "termDesignation");
    Objects.requireNonNull(cancelled, "cancelled");
    if (id.isBlank() || code.isBlank()) {
      throw new IllegalArgumentException("An offering needs an ID and course code.");
    }
    sessions = List.copyOf(sessions);
    sections = List.copyOf(sections);
  }

  /**
   * Finds a section by its exact name within this offering.
   *
   * @param sectionName the source section name
   * @return the matching section, or empty if the offering does not contain it
   */
  public Optional<Section> findSection(String sectionName) {
    return sections.stream().filter(section -> section.name().equals(sectionName)).findFirst();
  }
}
