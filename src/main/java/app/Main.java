package app;

import dataaccess.catalog.CatalogLoadException;
import dataaccess.catalog.InMemoryCourseCatalog;
import dataaccess.catalog.InMemoryOfferingLookup;
import dataaccess.catalog.JsonCatalogLoader;
import domain.CourseOffering;
import domain.Timetable;
import interfaceadapter.addsection.AddSectionController;
import interfaceadapter.removesection.RemoveSectionController;
import interfaceadapter.removesection.RemoveSectionPresenter;
import interfaceadapter.viewtimetable.TimetableViewModel;
import interfaceadapter.viewtimetable.ViewTimetableController;
import interfaceadapter.viewtimetable.ViewTimetablePresenter;
import interfaceadapter.searchcourses.SearchCoursesController;
import interfaceadapter.searchcourses.SearchCoursesPresenter;
import interfaceadapter.searchcourses.SearchCoursesViewModel;
import interfaceadapter.suggestcoursecodes.SuggestCourseCodesController;
import interfaceadapter.suggestcoursecodes.SuggestCourseCodesPresenter;
import interfaceadapter.suggestcoursecodes.SuggestCourseCodesViewModel;
import view.ConsoleSearchView;
import view.SearchView;
import view.WeeklyTimetablePanel;
import usecase.addsection.AddSectionInteractor;
import usecase.removesection.RemoveSectionInteractor;
import usecase.viewtimetable.ViewTimetableInteractor;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import javax.swing.SwingUtilities;

// Composition root: chooses adapters, loads data, and connects the application.
public final class Main {
    private static final String DEFAULT_CATALOGUE = "uoft-ttb-raw-catalogue-2026-09-05.zip";

    private Main() { }

    // Shell failures receive a nonzero exit code. A successful GUI stays open on Swing's thread.
    public static void main(String[] args) {
        int status = run(args, new InputStreamReader(System.in, StandardCharsets.UTF_8),
                new PrintWriter(System.out, true), new PrintWriter(System.err, true));
        if (status != 0) {
            System.exit(status);
        }
    }

    // Streams are injected so startup and the console flow can be exercised without a process.
    public static int run(String[] args, Reader input, PrintWriter output, PrintWriter errors) {
        LaunchOptions options;
        try {
            options = LaunchOptions.parse(args);
        } catch (IllegalArgumentException exception) {
            errors.println(exception.getMessage());
            errors.println("Use --help for launch options.");
            errors.flush();
            return 2;
        }
        if (options.help()) {
            output.println("Course search (Java 17+)\n"
                    + "  --sample          Use the small fictional test catalogue\n"
                    + "  --catalog PATH    Use a JSON file or ZIP containing courses.json\n"
                    + "  --console         Run an interactive console instead of the desktop window\n"
                    + "  --help            Show this help\n"
                    + "Default catalogue: " + DEFAULT_CATALOGUE);
            output.flush();
            return 0;
        }
        if (!options.console() && GraphicsEnvironment.isHeadless()) {
            errors.println("No desktop display is available. Run with --console.");
            errors.flush();
            return 2;
        }
        String source = options.sample() ? "SAMPLE DATA — fictional offerings for testing"
                : "Catalogue: " + options.catalogue();
        output.println("Loading " + source + " ...");
        output.flush();
        try {
            List<CourseOffering> offerings = load(options);
            InMemoryCourseCatalog catalog = new InMemoryCourseCatalog(offerings);
            SearchCoursesViewModel viewModel = new SearchCoursesViewModel();
            SearchCoursesPresenter presenter = new SearchCoursesPresenter(viewModel);
            SuggestCourseCodesViewModel suggestionsModel = new SuggestCourseCodesViewModel();
            SuggestCourseCodesPresenter suggestionsPresenter = new SuggestCourseCodesPresenter(suggestionsModel);
            SearchApplication application = SearchApplication.connect(catalog, catalog, presenter, suggestionsPresenter);
            SearchCoursesController controller = new SearchCoursesController(application.search());
            output.println("Loaded " + offerings.size() + " offerings. Catalogue data is a snapshot, not live enrolment.");
            output.flush();
            if (options.console()) {
                new ConsoleSearchView(controller, viewModel, input, output).run();
            } else {
                SuggestCourseCodesController suggestions = new SuggestCourseCodesController(application.suggestions());
                Timetable timetable = new Timetable();
                AddSectionInteractor addSectionInteractor = new AddSectionInteractor(timetable);
                AddSectionController addSectionController = new AddSectionController(addSectionInteractor);
                // View and remove share the timetable with add, and one view model for the grid panel.
                TimetableViewModel timetableModel = new TimetableViewModel();
                ViewTimetableController viewTimetableController = new ViewTimetableController(
                        new ViewTimetableInteractor(timetable, new InMemoryOfferingLookup(offerings),
                                new ViewTimetablePresenter(timetableModel)));
                RemoveSectionController removeSectionController = new RemoveSectionController(
                        new RemoveSectionInteractor(timetable, new RemoveSectionPresenter(timetableModel)));
                SwingUtilities.invokeLater(() -> {
                    WeeklyTimetablePanel timetablePanel = new WeeklyTimetablePanel(
                            viewTimetableController, removeSectionController, timetableModel);
                    SearchView desktopView = new SearchView(controller, viewModel, suggestions, suggestionsModel,
                            source, addSectionController, timetablePanel);
                    desktopView.show();
                    output.println("Desktop search window opened. Type CSC to try the course-code dropdown.");
                    output.flush();
                });
            }
            return 0;
        } catch (CatalogLoadException | IOException exception) {
            errors.println("Could not start course search: " + exception.getMessage());
            errors.println("Check the catalogue path and format, or use --sample to test the application.");
            errors.flush();
            return 1;
        }
    }

    // Sample data is bundled in the JAR; real data is read directly from its existing ZIP.
    private static List<CourseOffering> load(LaunchOptions options) throws CatalogLoadException, IOException {
        JsonCatalogLoader loader = new JsonCatalogLoader();
        if (!options.sample()) {
            return loader.load(options.catalogue());
        }
        InputStream resource = Main.class.getResourceAsStream("/sample-courses.json");
        if (resource == null) {
            throw new IOException("Bundled sample catalogue is missing.");
        }
        try (Reader reader = new InputStreamReader(resource, StandardCharsets.UTF_8)) {
            return loader.load(reader);
        }
    }

    // Startup options are separate from search requests and do not enter the use case.
    private record LaunchOptions(boolean console, boolean sample, boolean help, Path catalogue) {
        // Reject conflicting data sources and unknown arguments instead of silently ignoring them.
        static LaunchOptions parse(String[] args) {
            boolean console = false;
            boolean sample = false;
            boolean help = false;
            Path catalogue = null;
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "--console" -> console = true;
                    case "--sample" -> sample = true;
                    case "--help" -> help = true;
                    case "--catalog" -> {
                        if (++i >= args.length || args[i].startsWith("--") || catalogue != null) {
                            throw new IllegalArgumentException("Supply one path after --catalog.");
                        }
                        catalogue = Path.of(args[i]);
                    }
                    default -> throw new IllegalArgumentException("Unknown option: " + args[i]);
                }
            }
            if (sample && catalogue != null) {
                throw new IllegalArgumentException("Choose either --sample or --catalog, not both.");
            }
            return new LaunchOptions(console, sample, help,
                    catalogue == null ? Path.of(DEFAULT_CATALOGUE) : catalogue);
        }
    }
}
