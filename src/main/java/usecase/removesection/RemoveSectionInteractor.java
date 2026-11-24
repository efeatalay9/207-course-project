package usecase.removesection;

import domain.Timetable;
import java.util.Objects;

/**
 * Removes a selected section, and therefore all of its meetings, from the timetable.
 *
 * <p>The interactor only changes the timetable. Refreshing the weekly grid afterwards is
 * the job of the view-timetable use case, so each use case keeps one responsibility.
 */
public final class RemoveSectionInteractor implements RemoveSectionInputBoundary {
    private final Timetable timetable;
    private final RemoveSectionOutputBoundary output;

    /**
     * Creates the interactor.
     *
     * @param timetable the timetable shared with the add and view use cases
     * @param output where the outcome is sent
     */
    public RemoveSectionInteractor(Timetable timetable, RemoveSectionOutputBoundary output) {
        this.timetable = Objects.requireNonNull(timetable, "timetable");
        this.output = Objects.requireNonNull(output, "output");
    }

    @Override
    public void execute(RemoveSectionRequest request) {
        if (request == null || isBlank(request.offeringId()) || isBlank(request.sectionName())) {
            output.presentNoSelection();
            return;
        }

        RemoveSectionResponse response = new RemoveSectionResponse(request.offeringId(), request.sectionName());
        if (timetable.removeSection(request.offeringId(), request.sectionName())) {
            output.presentRemoved(response);
        } else {
            output.presentNotSelected(response);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
