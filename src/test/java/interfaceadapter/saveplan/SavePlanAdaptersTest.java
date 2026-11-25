package interfaceadapter.saveplan;

import static org.junit.jupiter.api.Assertions.assertEquals;

import interfaceadapter.viewtimetable.TimetableViewModel;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import usecase.saveplan.SavePlanRequest;
import usecase.saveplan.SavePlanResponse;

class SavePlanAdaptersTest {
  @Test
  void controllerPassesTheChosenFile() {
    List<SavePlanRequest> requests = new ArrayList<>();
    new SavePlanController(requests::add).save("/home/leon/fall.json");
    assertEquals(List.of(new SavePlanRequest("/home/leon/fall.json")), requests);
  }

  @Test
  void presenterWritesShortStatusMessages() {
    TimetableViewModel model = new TimetableViewModel();
    SavePlanPresenter presenter = new SavePlanPresenter(model);

    presenter.presentSaved(new SavePlanResponse("/home/leon/fall.json", 3, Instant.EPOCH));
    assertEquals("Saved 3 sections to fall.json.", model.status());

    presenter.presentSaved(new SavePlanResponse("C:\\plans\\one.json", 1, Instant.EPOCH));
    assertEquals("Saved 1 section to one.json.", model.status());

    presenter.presentNothingToSave();
    assertEquals("Add at least one section before saving a plan.", model.status());

    presenter.presentNoLocation();
    assertEquals("Choose a file name before saving the plan.", model.status());

    presenter.presentSaveFailed("/tmp/x.json", "Could not write the plan file.");
    assertEquals("Could not save x.json. Could not write the plan file. "
        + "Your timetable is unchanged.", model.status());
  }
}
