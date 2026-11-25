package usecase.overlaps;

import domain.CourseOffering;
import domain.MeetingOverlap;
import domain.MeetingTime;
import domain.Section;
import domain.SelectedSection;
import domain.StatusFlag;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import usecase.common.CatalogAccessException;
import usecase.overlaps.SectionOverlapResult.Conflict;
import usecase.viewtimetable.OfferingLookup;

/** Checks all section types against a snapshot, using catalogue data rather than screen text. */
public final class SectionOverlapChecker implements SectionOverlapCheck {
  private final OfferingLookup offerings;

  /** Uses a replaceable offering lookup; no UI or storage implementation is required. */
  public SectionOverlapChecker(OfferingLookup offerings) {
    this.offerings = java.util.Objects.requireNonNull(offerings);
  }

  /** Resolves each offering once per check and leaves the catalogue and timetable unchanged. */
  @Override
  public List<SectionOverlapResult> check(
      List<SelectedSection> candidates, List<SelectedSection> selected)
      throws CatalogAccessException {
    Map<String, Optional<CourseOffering>> cache = new HashMap<>();
    List<SectionOverlapResult> results = new ArrayList<>();
    for (SelectedSection candidate : candidates) {
      Optional<CourseOffering> offering = lookup(candidate.offeringId(), cache);
      Optional<Section> section =
          offering.flatMap(value -> value.findSection(candidate.sectionName()));
      List<Conflict> conflicts = new ArrayList<>();
      boolean incomplete = section.isEmpty() || unknownTimes(section.get());
      for (SelectedSection existing : selected) {
        // An already-selected section must not conflict with its own meetings.
        if (existing.equals(candidate)) {
          continue;
        }
        Optional<CourseOffering> otherOffering = lookup(existing.offeringId(), cache);
        Optional<Section> other =
            otherOffering.flatMap(value -> value.findSection(existing.sectionName()));
        if (other.isEmpty() || unknownTimes(other.get())) {
          incomplete = true;
        }
        if (section.isPresent() && other.isPresent()) {
          compare(
              section.get(), other.get(), otherOffering.orElseThrow().code(), existing, conflicts);
        }
      }
      results.add(new SectionOverlapResult(candidate, conflicts, incomplete));
    }
    return List.copyOf(results);
  }

  private Optional<CourseOffering> lookup(String id, Map<String, Optional<CourseOffering>> cache)
      throws CatalogAccessException {
    if (!cache.containsKey(id)) {
      cache.put(id, offerings.findById(id));
    }
    return cache.get(id);
  }

  private boolean unknownTimes(Section section) {
    // The current domain cannot distinguish truly asynchronous sections from absent meeting data.
    return section.meetings().isEmpty() || section.timeToBeAnnounced() != StatusFlag.NO;
  }

  private void compare(
      Section candidate,
      Section existing,
      String code,
      SelectedSection key,
      List<Conflict> conflicts) {
    for (MeetingTime meeting : candidate.meetings()) {
      for (MeetingTime other : existing.meetings()) {
        MeetingOverlap.Kind kind = MeetingOverlap.compare(meeting, other);
        if (kind != MeetingOverlap.Kind.NONE) {
          boolean confirmed =
              kind == MeetingOverlap.Kind.CONFIRMED
                  && candidate.timeToBeAnnounced() == StatusFlag.NO
                  && existing.timeToBeAnnounced() == StatusFlag.NO;
          conflicts.add(new Conflict(meeting, code, key, other, confirmed));
        }
      }
    }
  }
}
