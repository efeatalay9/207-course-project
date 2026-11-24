package view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import domain.CourseOffering;
import domain.MeetingTime;
import domain.Section;
import domain.StatusFlag;
import domain.Timetable;
import interfaceadapter.addsection.AddSectionController;
import interfaceadapter.addsection.AddSectionPresenter;
import interfaceadapter.removesection.RemoveSectionController;
import interfaceadapter.removesection.RemoveSectionPresenter;
import interfaceadapter.viewtimetable.TimetableViewModel;
import interfaceadapter.viewtimetable.ViewTimetableController;
import interfaceadapter.viewtimetable.ViewTimetablePresenter;
import java.awt.BorderLayout;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import usecase.addsection.AddSectionInteractor;
import usecase.removesection.RemoveSectionInteractor;
import usecase.viewtimetable.OfferingLookup;
import usecase.viewtimetable.ViewTimetableInteractor;

class WeeklyTimetablePanelTest {
  @Test
  void refreshingAfterAddShowsTheMeetingAndPreservesActionFeedback() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          Timetable timetable = new Timetable();
          TimetableViewModel model = new TimetableViewModel();
          OfferingLookup lookup =
              id -> id.equals("fall") ? Optional.of(offering()) : Optional.empty();
          AddSectionController addController =
              new AddSectionController(
                  new AddSectionInteractor(timetable, lookup, new AddSectionPresenter(model)));
          ViewTimetableController viewController =
              new ViewTimetableController(
                  new ViewTimetableInteractor(
                      timetable, lookup, new ViewTimetablePresenter(model)));
          RemoveSectionController removeController =
              new RemoveSectionController(
                  new RemoveSectionInteractor(timetable, new RemoveSectionPresenter(model)));
          WeeklyTimetablePanel panel =
              new WeeklyTimetablePanel(viewController, removeController, model);
          BorderLayout layout = (BorderLayout) panel.getLayout();
          JLabel status = (JLabel) layout.getLayoutComponent(BorderLayout.SOUTH);

          addController.addSection("fall", "LEC0101");
          panel.refresh();
          assertEquals("Added CSC108H1 LEC0101 to your timetable.", status.getText());
          assertEquals(1, model.state().blocks().size());

          addController.addSection("fall", "LEC0101");
          panel.refresh();
          assertEquals("CSC108H1 LEC0101 is already in your timetable.", status.getText());
          assertEquals(1, model.state().sections().size());

          addController.addSection("missing", "LEC0101");
          panel.refresh();
          assertEquals(
              "That section is no longer in the catalogue. Search again to refresh it.",
              status.getText());
          assertEquals(1, model.state().blocks().size());
        });
  }

  private static CourseOffering offering() {
    MeetingTime meeting =
        new MeetingTime(
            DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 0), "20269", "BA", "", "");
    Section section =
        new Section("LEC0101", "LEC", StatusFlag.NO, StatusFlag.NO, List.of(meeting), List.of());
    return new CourseOffering(
        "fall",
        "CSC108H1",
        "Sample course",
        "St. George",
        List.of("20269"),
        "F",
        StatusFlag.NO,
        List.of(section));
  }
}
