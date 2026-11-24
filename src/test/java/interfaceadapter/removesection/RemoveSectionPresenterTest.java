package interfaceadapter.removesection;

import interfaceadapter.viewtimetable.TimetableViewModel;
import interfaceadapter.viewtimetable.TimetableViewModel.SectionItem;
import org.junit.jupiter.api.Test;
import usecase.removesection.RemoveSectionResponse;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class RemoveSectionPresenterTest {
    @Test
    void usesTheOnScreenLabelInMessages() {
        TimetableViewModel model = new TimetableViewModel();
        model.update(new TimetableViewModel.State(List.of(), "", List.of("Mon"), 9, 18, List.of(),
                List.of(new SectionItem("fall", "LEC0101", "CSC207H1 LEC0101 (LEC)")), List.of(), ""));
        RemoveSectionPresenter presenter = new RemoveSectionPresenter(model);

        presenter.presentRemoved(new RemoveSectionResponse("fall", "LEC0101"));
        assertEquals("Removed CSC207H1 LEC0101 from your timetable.", model.status());

        presenter.presentNotSelected(new RemoveSectionResponse("winter", "TUT0101"));
        assertEquals("TUT0101 is not in your timetable.", model.status());

        presenter.presentNoSelection();
        assertEquals("Choose a section in the list before clicking Remove.", model.status());
    }
}
