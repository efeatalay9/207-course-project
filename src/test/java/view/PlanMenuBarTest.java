package view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dataaccess.plan.JsonPlanFileDataAccess;
import domain.SelectedSection;
import domain.Timetable;
import interfaceadapter.openplan.OpenPlanController;
import interfaceadapter.openplan.OpenPlanPresenter;
import interfaceadapter.saveplan.SavePlanController;
import interfaceadapter.saveplan.SavePlanPresenter;
import interfaceadapter.viewtimetable.TimetableViewModel;
import java.io.File;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import usecase.openplan.OpenPlanInteractor;
import usecase.saveplan.SavePlanInteractor;

class PlanMenuBarTest {
  @TempDir
  Path folder;

  @Test
  void savedPlanReopensAfterTheTimetableChanges() throws Exception {
    SwingUtilities.invokeAndWait(() -> {
      Timetable timetable = new Timetable();
      TimetableViewModel model = new TimetableViewModel();
      JsonPlanFileDataAccess files = new JsonPlanFileDataAccess();
      AtomicInteger refreshes = new AtomicInteger();
      PlanMenuBar menu = new PlanMenuBar(
          new SavePlanController(new SavePlanInteractor(timetable, files,
              new SavePlanPresenter(model), Clock.systemUTC())),
          new OpenPlanController(new OpenPlanInteractor(timetable, files,
              new OpenPlanPresenter(model, ZoneOffset.UTC))),
          refreshes::incrementAndGet);

      timetable.addSection("csc207-fall", "LEC0101");
      File file = PlanMenuBar.withExtension(folder.resolve("fall").toFile());
      menu.save(file);
      assertEquals("Saved 1 section to fall.json.", model.status());

      timetable.removeSection("csc207-fall", "LEC0101");
      timetable.addSection("mat137-year", "LEC0101");
      menu.open(file);

      assertEquals(List.of(new SelectedSection("csc207-fall", "LEC0101")),
          timetable.selections());
      assertEquals(2, refreshes.get());
      assertEquals("Plan", menu.getMenu(0).getText());
      assertEquals(2, menu.getMenu(0).getItemCount());
    });
  }

  @Test
  void addsTheJsonExtensionOnlyWhenMissing() {
    assertEquals("a.json", PlanMenuBar.withExtension(new File("a")).getName());
    assertEquals("b.JSON", PlanMenuBar.withExtension(new File("b.JSON")).getName());
  }
}
