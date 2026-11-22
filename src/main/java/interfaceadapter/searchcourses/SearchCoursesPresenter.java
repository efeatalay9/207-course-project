package interfaceadapter.searchcourses;

import domain.StatusFlag;
import usecase.searchcourses.OfferingResult;
import usecase.searchcourses.SearchCoursesOutputBoundary;
import usecase.searchcourses.SearchCoursesResponse;
import java.time.DateTimeException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

// Formats use-case responses without depending on a specific UI toolkit.
public final class SearchCoursesPresenter implements SearchCoursesOutputBoundary {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
    private static final DateTimeFormatter SESSION = DateTimeFormatter.ofPattern("MMM uuuu", Locale.ENGLISH);
    private final SearchCoursesViewModel viewModel;

    // A single view model is connected to the presenter and view by startup wiring.
    public SearchCoursesPresenter(SearchCoursesViewModel viewModel) {
        this.viewModel = Objects.requireNonNull(viewModel);
    }

    // Every outcome replaces both heading and details, including failures and empty results.
    @Override
    public void present(SearchCoursesResponse response) {
        switch (response.outcome()) {
            case INVALID_INPUT -> update("Enter a course code", "The course code cannot be blank.");
            case CATALOGUE_FAILURE -> update("Catalogue unavailable",
                    "The catalogue could not be searched. Please try again or restart the application.");
            case SUCCESS -> presentSuccess(response);
        }
    }

    // Format all offerings, not just the first match for a code.
    private void presentSuccess(SearchCoursesResponse response) {
        if (response.offerings().isEmpty()) {
            update("No matching offerings", "No offerings found for " + response.normalizedCode()
                    + ". Check the full course code, for example CSC108H1.");
            return;
        }
        String details = response.offerings().stream().map(this::offering)
                .collect(Collectors.joining("\n\n"));
        int count = response.offerings().size();
        update(count + (count == 1 ? " offering" : " offerings") + " for " + response.normalizedCode(), details);
    }

    // Sessions identify actual calendar periods; F/S designations can also occur in summer.
    private String offering(OfferingResult offering) {
        StringBuilder text = new StringBuilder(offering.code()).append(" — ")
                .append(known(offering.title(), "Title unavailable")).append('\n')
                .append("Campus: ").append(known(offering.campus(), "Unknown"))
                .append(" | Term designation: ").append(known(offering.termDesignation(), "Unknown"))
                .append("\nSessions: ").append(offering.sessions().isEmpty() ? "Unknown"
                        : offering.sessions().stream().map(this::session).collect(Collectors.joining(", ")))
                .append("\nOffering ID: ").append(offering.id()).append('\n');
        appendCancellation(text, offering.cancelled(), "Offering");
        if (offering.sections().isEmpty()) {
            text.append("No section information available.\n");
        }
        for (OfferingResult.SectionResult section : offering.sections()) {
            appendSection(text, section);
        }
        return text.toString().stripTrailing();
    }

    // Unknown times and cancellation remain visible even when other meetings are known.
    private void appendSection(StringBuilder text, OfferingResult.SectionResult section) {
        text.append("\n  ").append(section.name()).append(" — ")
                .append(teachingMethod(section.teachingMethod())).append('\n');
        appendCancellation(text, section.cancelled(), "  Section");
        if (section.timeToBeAnnounced() == StatusFlag.YES) {
            text.append("    Times to be announced (TBA).\n");
        } else if (section.timeToBeAnnounced() == StatusFlag.UNKNOWN) {
            text.append("    TBA status unknown.\n");
        }
        if (section.meetings().isEmpty()) {
            text.append("    Meeting times unknown; do not assume this section fits your schedule.\n");
        }
        for (OfferingResult.MeetingResult meeting : section.meetings()) {
            text.append("    ").append(meeting.day().getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                    .append(' ').append(TIME.format(meeting.start())).append("–").append(TIME.format(meeting.end()))
                    .append(" | ").append(session(meeting.session()))
                    .append(" | ").append(known(meeting.location(), "Location unknown"))
                    .append("\n      ").append(recurrence(meeting)).append('\n');
        }
        if (!section.links().isEmpty()) {
            text.append("    Linked sections: ").append(section.links().stream()
                    .map(link -> link.teachingMethod() + link.sectionNumber()).collect(Collectors.joining(", ")))
                    .append('\n');
        }
    }

    // Unknown flags remain unknown instead of implying an active offering or section.
    private void appendCancellation(StringBuilder text, StatusFlag flag, String subject) {
        if (flag == StatusFlag.YES) {
            text.append(subject).append(" CANCELLED.\n");
        } else if (flag == StatusFlag.UNKNOWN) {
            text.append(subject).append(" cancellation status unknown.\n");
        }
    }

    // Do not infer exact dates for alternate-week or manual recurrence patterns.
    private String recurrence(OfferingResult.MeetingResult meeting) {
        if (meeting.repetition().equals("WEEKLY") && meeting.repetitionPattern().equals("ONCE_A_WEEK")) {
            return "Weekly";
        }
        if (meeting.repetition().equals("MANUAL")) {
            return "Manual schedule; exact dates unavailable (" + known(meeting.repetitionPattern(), "unknown") + ").";
        }
        return "Recurrence: " + known(meeting.repetition(), "unknown") + " / "
                + known(meeting.repetitionPattern(), "unknown");
    }

    // Session codes use a four-digit year followed by a month; retain unrecognized codes.
    private String session(String code) {
        if (code.matches("[0-9]{5,6}")) {
            try {
                YearMonth month = YearMonth.of(Integer.parseInt(code.substring(0, 4)),
                        Integer.parseInt(code.substring(4)));
                return SESSION.format(month) + " (" + code + ")";
            } catch (DateTimeException exception) {
                // Unknown source values should remain visible rather than fail a search.
            }
        }
        return known(code, "Session unknown");
    }

    // Keep unfamiliar teaching methods visible for future catalogue changes.
    private String teachingMethod(String method) {
        return switch (method) {
            case "LEC" -> "Lecture";
            case "TUT" -> "Tutorial";
            case "PRA" -> "Practical";
            default -> method;
        };
    }

    // Missing optional text receives a label only at the presentation boundary.
    private String known(String value, String fallback) {
        return value.isBlank() ? fallback : value;
    }

    // Centralize complete state replacement for every outcome.
    private void update(String heading, String details) {
        viewModel.update(new SearchCoursesViewModel.State(heading, details));
    }
}
