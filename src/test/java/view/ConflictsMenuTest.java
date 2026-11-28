package view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import domain.CourseOffering;
import domain.MeetingTime;
import domain.Section;
import domain.StatusFlag;
import domain.Timetable;
import interfaceadapter.checkconflicts.CheckConflictsController;
import interfaceadapter.checkconflicts.CheckConflictsPresenter;
import interfaceadapter.checkconflicts.ConflictsViewModel;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import usecase.checkconflicts.CheckConflictsInteractor;

class ConflictsMenuTest {
  @Test
  void checkShowsTheOverlapForTheShownSession() throws Exception {
    SwingUtilities.invokeAndWait(() -> {
      MeetingTime lecture = new MeetingTime(DayOfWeek.MONDAY, LocalTime.of(10, 0),
          LocalTime.of(11, 0), "20269", "", "WEEKLY", "ONCE_A_WEEK");
      MeetingTime tutorial = new MeetingTime(DayOfWeek.MONDAY, LocalTime.of(10, 0),
          LocalTime.of(10, 50), "20269", "", "WEEKLY", "ONCE_A_WEEK");
      CourseOffering offering = new CourseOffering("csc207", "CSC207H1", "Software Design",
          "St. George", List.of("20269"), "F", StatusFlag.NO, List.of(
              new Section("LEC0101", "LEC", StatusFlag.NO, StatusFlag.NO, List.of(lecture),
                  List.of()),
              new Section("TUT0101", "TUT", StatusFlag.NO, StatusFlag.NO, List.of(tutorial),
                  List.of())));
      Timetable timetable = new Timetable();
      ConflictsViewModel model = new ConflictsViewModel();
      ConflictsMenu menu = new ConflictsMenu(new CheckConflictsController(
          new CheckConflictsInteractor(timetable, id -> Optional.of(offering),
              new CheckConflictsPresenter(model))), model, () -> "20269");

      assertEquals("Add sections to your timetable before checking for overlaps.", menu.check());
      assertEquals(JOptionPane.INFORMATION_MESSAGE, menu.messageType());

      timetable.addSection("csc207", "LEC0101");
      timetable.addSection("csc207", "TUT0101");
      assertEquals("1 overlap found in Fall 2026.\n\n"
          + "Mon 10:00–10:50 · CSC207H1 LEC0101 and CSC207H1 TUT0101 (50 min)",
          menu.check());
      assertEquals(JOptionPane.WARNING_MESSAGE, menu.messageType());
      assertEquals("Timetable", menu.getText());
      assertEquals(1, menu.getItemCount());
    });
  }

  @Test
  void notesAndErrorsAreShown() throws Exception {
    SwingUtilities.invokeAndWait(() -> {
      ConflictsViewModel model = new ConflictsViewModel();
      ConflictsMenu menu = new ConflictsMenu(new CheckConflictsController(request ->
          model.update(new ConflictsViewModel.State(ConflictsViewModel.Level.ERROR, "Broken.",
              List.of(), List.of("Note.")))), model, () -> "");

      assertEquals("Broken.\n\nNote.", menu.check());
      assertEquals(JOptionPane.ERROR_MESSAGE, menu.messageType());
    });
  }
}
