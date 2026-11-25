package interfaceadapter.openplan;

import static org.junit.jupiter.api.Assertions.assertEquals;

import interfaceadapter.viewtimetable.TimetableViewModel;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import usecase.openplan.OpenPlanRequest;
import usecase.openplan.OpenPlanResponse;

class OpenPlanAdaptersTest {
  @Test
  void controllerPassesTheChosenFile() {
    List<OpenPlanRequest> requests = new ArrayList<>();
    new OpenPlanController(requests::add).open("fall.json");
    assertEquals(List.of(new OpenPlanRequest("fall.json")), requests);
  }

  @Test
  void presenterShowsTheSaveTimeInTheStudentsZone() {
    TimetableViewModel model = new TimetableViewModel();
    OpenPlanPresenter presenter = new OpenPlanPresenter(model, ZoneId.of("America/Toronto"));

    presenter.presentOpened(new OpenPlanResponse("/home/leon/fall.json", 2,
        Instant.parse("2026-11-24T23:00:00Z")));
    assertEquals("Opened fall.json with 2 sections, saved Nov 24, 2026 at 6:00 PM.",
        model.status());

    presenter.presentNoLocation();
    assertEquals("Choose a plan file to open.", model.status());

    presenter.presentUnsupportedVersion("future.json", 2);
    assertEquals("future.json uses plan version 2, which this app cannot open. "
        + "Your current timetable is still open.", model.status());

    presenter.presentOpenFailed(null, "The plan file does not exist.");
    assertEquals("Could not open the plan. The plan file does not exist. "
        + "Your current timetable is still open.", model.status());
  }
}
