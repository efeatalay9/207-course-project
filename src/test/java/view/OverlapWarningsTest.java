package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dataaccess.catalog.InMemoryCourseCatalog;
import dataaccess.catalog.InMemoryOfferingLookup;
import dataaccess.catalog.JsonCatalogLoader;
import domain.CourseOffering;
import domain.Timetable;
import interfaceadapter.addsection.AddSectionController;
import interfaceadapter.addsection.AddSectionPresenter;
import interfaceadapter.overlaps.CheckSectionOverlapsController;
import interfaceadapter.overlaps.CheckSectionOverlapsPresenter;
import interfaceadapter.removesection.RemoveSectionController;
import interfaceadapter.removesection.RemoveSectionPresenter;
import interfaceadapter.searchcourses.SearchCoursesController;
import interfaceadapter.searchcourses.SearchCoursesPresenter;
import interfaceadapter.searchcourses.SearchCoursesViewModel;
import interfaceadapter.searchcourses.SearchCoursesViewModel.SectionChoice;
import interfaceadapter.viewtimetable.TimetableViewModel;
import interfaceadapter.viewtimetable.ViewTimetableController;
import interfaceadapter.viewtimetable.ViewTimetablePresenter;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import usecase.addsection.AddSectionInteractor;
import usecase.overlaps.CheckSectionOverlapsInteractor;
import usecase.overlaps.SectionOverlapChecker;
import usecase.removesection.RemoveSectionInteractor;
import usecase.searchcourses.SearchCoursesInteractor;
import usecase.viewtimetable.ViewTimetableInteractor;

class OverlapWarningsTest {
  @Test
  void flagsSearchAndGridAllowsAddingAndClearsAfterRemovingConflict() throws Exception {
    List<CourseOffering> offerings =
        new JsonCatalogLoader().load(Path.of("examples/overlap-courses.json"));
    SwingUtilities.invokeAndWait(() -> {
      InMemoryOfferingLookup lookup = new InMemoryOfferingLookup(offerings);
      Timetable timetable = new Timetable();
      SearchCoursesViewModel searchModel = new SearchCoursesViewModel();
      TimetableViewModel gridModel = new TimetableViewModel();
      SearchCoursesController search =
          new SearchCoursesController(
              new SearchCoursesInteractor(
                  new InMemoryCourseCatalog(offerings), new SearchCoursesPresenter(searchModel)));
      CheckSectionOverlapsController warnings =
          new CheckSectionOverlapsController(
              new CheckSectionOverlapsInteractor(
                  timetable,
                  new SectionOverlapChecker(lookup),
                  new CheckSectionOverlapsPresenter(searchModel)));
      AddSectionController add =
          new AddSectionController(
              new AddSectionInteractor(timetable, lookup, new AddSectionPresenter(gridModel)));
      WeeklyTimetablePanel panel =
          new WeeklyTimetablePanel(
              new ViewTimetableController(
                  new ViewTimetableInteractor(
                      timetable, lookup, new ViewTimetablePresenter(gridModel))),
              new RemoveSectionController(
                  new RemoveSectionInteractor(timetable, new RemoveSectionPresenter(gridModel))),
              gridModel);
      panel.setOnSelectionsChanged(() -> warnings.check(searchModel.state().sectionChoices()));

      add.addSection("overlap-CSC108H1", "LEC0101");
      search.search("CSC148H1");
      warnings.check(searchModel.state().sectionChoices());
      SectionChoice tutorial = choice(searchModel, "TUT0101");
      assertTrue(tutorial.label().startsWith("[OVERLAP]"));
      assertTrue(tutorial.warning().contains("CSC108H1 LEC0101"));
      assertTrue(choice(searchModel, "PRA0101").warning().isEmpty(), "Back-to-back is allowed");
      assertEquals(1, timetable.selections().size(), "Checking must not add a candidate");

      DefaultListModel<SectionChoice> rows = new DefaultListModel<>();
      rows.addElement(tutorial);
      SectionChoiceList list = new SectionChoiceList(rows);
      list.setFixedCellHeight(25);
      list.setSize(600, 100);
      String tooltip = list.getToolTipText(mouse(list, 5, 10));
      assertTrue(tooltip.contains("Overlaps with CSC108H1 LEC0101"));
      assertTrue(tooltip.contains("You can still add"));
      assertNull(list.getToolTipText(mouse(list, 5, 80)), "Blank list area has no warning");

      add.addSection("overlap-CSC148H1", "TUT0101");
      panel.refresh();
      assertEquals(2, timetable.selections().size(), "An overlapping tutorial must remain addable");
      assertTrue(gridModel.state().blocks().stream().allMatch(block -> !block.warning().isEmpty()));
      WeeklyGrid grid = new WeeklyGrid();
      grid.setState(gridModel.state());
      grid.setSize(700, 500);
      BufferedImage image = new BufferedImage(700, 500, BufferedImage.TYPE_INT_RGB);
      java.awt.Graphics2D graphics = image.createGraphics();
      grid.paint(graphics);
      graphics.dispose();
      assertTrue(
          grid.getToolTipText(mouse(grid, 60, 35)).contains("Overlaps with CSC148H1 TUT0101"));

      JList<?> selected = find(panel, JList.class);
      selected.setSelectedIndex(0);
      findRemoveButton(panel).doClick();
      assertEquals(1, timetable.selections().size());
      assertTrue(choice(searchModel, "TUT0101").warning().isEmpty());
      assertFalse(choice(searchModel, "TUT0101").label().contains("[OVERLAP]"));
      assertTrue(gridModel.state().blocks().get(0).warning().isEmpty());
    });
  }

  @Test
  void tooltipEscapesCatalogueTextAndUnavailableReplacesOldWarnings() throws Exception {
    SwingUtilities.invokeAndWait(() -> {
      SearchCoursesViewModel model = new SearchCoursesViewModel();
      model.update(
          new SearchCoursesViewModel.State(
              "Results",
              "Details",
              List.of(new SectionChoice("one", "TUT0101", "Test", "<unsafe> & room"))));
      CheckSectionOverlapsPresenter presenter = new CheckSectionOverlapsPresenter(model);
      presenter.presentUnavailable();
      assertTrue(model.state().sectionChoices().get(0).warning().contains("unavailable"));
      DefaultListModel<SectionChoice> rows = new DefaultListModel<>();
      rows.addElement(model.state().sectionChoices().get(0));
      SectionChoiceList list = new SectionChoiceList(rows);
      list.setFixedCellHeight(25);
      list.setSize(200, 50);
      assertTrue(list.getToolTipText(mouse(list, 5, 10)).contains("&lt;unsafe&gt; &amp; room"));
    });
  }

  private SectionChoice choice(SearchCoursesViewModel model, String section) {
    return model.state().sectionChoices().stream()
        .filter(item -> item.sectionName().equals(section))
        .findFirst()
        .orElseThrow();
  }

  private MouseEvent mouse(Component component, int x, int y) {
    return new MouseEvent(component, MouseEvent.MOUSE_MOVED, 0, 0, x, y, 0, false);
  }

  private JButton findRemoveButton(Container root) {
    for (Component component : root.getComponents()) {
      if (component instanceof JButton button && "Remove".equals(button.getText())) {
        return button;
      }
      if (component instanceof Container container) {
        JButton button = findRemoveButton(container);
        if (button != null) {
          return button;
        }
      }
    }
    return null;
  }

  private <T extends Component> T find(Container root, Class<T> type) {
    for (Component component : root.getComponents()) {
      if (type.isInstance(component)) {
        return type.cast(component);
      }
      if (component instanceof Container container) {
        T child = find(container, type);
        if (child != null) {
          return child;
        }
      }
    }
    return null;
  }
}
