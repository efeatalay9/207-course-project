package interfaceadapter.checkconflicts;

import static org.junit.jupiter.api.Assertions.assertEquals;

import interfaceadapter.checkconflicts.ConflictsViewModel.Level;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import usecase.checkconflicts.CheckConflictsRequest;
import usecase.checkconflicts.CheckConflictsResponse;
import usecase.checkconflicts.CheckConflictsResponse.Conflict;
import usecase.checkconflicts.CheckConflictsResponse.Reason;
import usecase.checkconflicts.CheckConflictsResponse.SectionLabel;
import usecase.checkconflicts.CheckConflictsResponse.UncheckedSection;

class CheckConflictsAdaptersTest {
  private static final SectionLabel CSC = new SectionLabel("csc207", "CSC207H1", "LEC0101");
  private static final SectionLabel MAT = new SectionLabel("mat137", "MAT137Y1", "TUT0102");

  private final ConflictsViewModel model = new ConflictsViewModel();
  private final CheckConflictsPresenter presenter = new CheckConflictsPresenter(model);

  private static Conflict conflict(boolean confirmed) {
    return new Conflict(CSC, MAT, DayOfWeek.MONDAY, LocalTime.of(10, 30), LocalTime.of(11, 0),
        confirmed);
  }

  @Test
  void controllerSendsTheShownSession() {
    List<CheckConflictsRequest> requests = new ArrayList<>();
    CheckConflictsController controller = new CheckConflictsController(requests::add);
    controller.check("20269");
    controller.check(null);
    assertEquals(List.of(new CheckConflictsRequest("20269"), new CheckConflictsRequest("")),
        requests);
  }

  @Test
  void confirmedOverlapIsWarning() {
    presenter.present(new CheckConflictsResponse("20269", List.of(conflict(true)), List.of()));

    assertEquals(new ConflictsViewModel.State(Level.WARNING, "1 overlap found in Fall 2026.",
        List.of("Mon 10:30–11:00 · CSC207H1 LEC0101 and MAT137Y1 TUT0102 (30 min)"),
        List.of()), model.state());
  }

  @Test
  void possibleOverlapsAskForReview() {
    presenter.present(new CheckConflictsResponse("20271",
        List.of(conflict(false), conflict(false)), List.of()));

    assertEquals(Level.REVIEW, model.state().level());
    assertEquals("2 possible overlaps in Winter 2027.", model.state().heading());
    assertEquals(
        "Mon 10:30–11:00 · CSC207H1 LEC0101 and MAT137Y1 TUT0102 (30 min) — check",
        model.state().conflicts().get(0));
    assertEquals(1, model.state().notes().size());
  }

  @Test
  void uncheckedSectionsAreListed() {
    presenter.present(new CheckConflictsResponse("20265", List.of(), List.of(
        new UncheckedSection(CSC, Reason.TIME_TBA),
        new UncheckedSection(new SectionLabel("gone", "", "LEC0101"), Reason.NOT_FOUND))));

    assertEquals(new ConflictsViewModel.State(Level.REVIEW,
        "No overlaps found in Summer 2026, but some sections were not checked.", List.of(),
        List.of("CSC207H1 LEC0101: times not announced yet, so it was not checked.",
            "LEC0101: no longer in the catalogue, so it was not checked.")), model.state());
  }

  @Test
  void noOverlapsIsClear() {
    presenter.present(new CheckConflictsResponse("20269", List.of(), List.of()));
    assertEquals(Level.CLEAR, model.state().level());
    assertEquals("No overlaps in Fall 2026. Back-to-back classes are fine.",
        model.state().heading());
  }

  @Test
  void emptyAndFailedChecksExplainThemselves() {
    presenter.presentNothingToCheck();
    assertEquals(Level.NONE, model.state().level());
    assertEquals("Add sections to your timetable before checking for overlaps.",
        model.state().heading());

    presenter.presentFailure("Timetable Builder is unavailable.");
    assertEquals(Level.ERROR, model.state().level());
    assertEquals("Could not check for overlaps. Timetable Builder is unavailable.",
        model.state().heading());
  }

  @Test
  void sessionLabelsFallBackToTheCode() {
    assertEquals("session 20263", CheckConflictsPresenter.sessionLabel("20263"));
    assertEquals("session F", CheckConflictsPresenter.sessionLabel("F"));
    assertEquals("your timetable", CheckConflictsPresenter.sessionLabel(""));
  }
}
