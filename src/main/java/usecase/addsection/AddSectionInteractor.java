package usecase.addsection;

import domain.Timetable;

/** Handles requests to add sections to the current timetable. */
public final class AddSectionInteractor implements AddSectionInputBoundary {
    private final Timetable timetable;

    /**
     * Creates an interactor that uses the current timetable.
     *
     * @param timetable the timetable to update
     */
    public AddSectionInteractor(Timetable timetable) {
        this.timetable = timetable;
    }

    @Override
    public boolean execute(String offeringId, String sectionName) {
        return timetable.addSection(offeringId, sectionName);
    }
}
