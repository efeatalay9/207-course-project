package app;

import dataaccess.catalog.CatalogLoadException;
import dataaccess.catalog.InMemoryCourseCatalog;
import dataaccess.catalog.InMemoryOfferingLookup;
import dataaccess.catalog.JsonCatalogLoader;
import dataaccess.catalog.TtbCourseCatalog;
import domain.CourseOffering;
import domain.Timetable;
import interfaceadapter.addsection.AddSectionController;
import interfaceadapter.addsection.AddSectionPresenter;
import interfaceadapter.removesection.RemoveSectionController;
import interfaceadapter.removesection.RemoveSectionPresenter;
import interfaceadapter.searchcourses.SearchCoursesController;
import interfaceadapter.searchcourses.SearchCoursesPresenter;
import interfaceadapter.searchcourses.SearchCoursesViewModel;
import interfaceadapter.suggestcoursecodes.SuggestCourseCodesController;
import interfaceadapter.suggestcoursecodes.SuggestCourseCodesPresenter;
import interfaceadapter.suggestcoursecodes.SuggestCourseCodesViewModel;
import interfaceadapter.viewtimetable.TimetableViewModel;
import interfaceadapter.viewtimetable.ViewTimetableController;
import interfaceadapter.viewtimetable.ViewTimetablePresenter;
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
import usecase.addsection.AddSectionInteractor;
import usecase.common.CatalogAccessException;
import usecase.removesection.RemoveSectionInteractor;
import usecase.searchcourses.CourseCatalog;
import usecase.suggestcoursecodes.CourseCodeIndex;
import usecase.viewtimetable.OfferingLookup;
import usecase.viewtimetable.ViewTimetableInteractor;
import view.ConsoleSearchView;
import view.SearchView;
import view.WeeklyTimetablePanel;

/** Chooses the catalogue source and wires the desktop or console application. */
public final class Main {
  private static final String DEFAULT_CATALOGUE = "uoft-ttb-raw-catalogue-2026-09-05.zip";

  private Main() {}

  /**
   * Launches course search using the requested catalogue and reports startup failures to the shell.
   *
   * @param args command-line options, listed by {@code --help}
   */
  public static void main(String[] args) {
    int status =
        run(
            args,
            new InputStreamReader(System.in, StandardCharsets.UTF_8),
            new PrintWriter(System.out, true),
            new PrintWriter(System.err, true));
    if (status != 0) {
      System.exit(status);
    }
  }

  /**
   * Starts the Swing app or runs a console session using the supplied streams.
   *
   * @param args command-line options
   * @param input the reader for console commands
   * @param output where to display results and startup information
   * @param errors where to display launch errors
   * @return zero for success, one for catalogue or I/O errors, or two for invalid launch settings
   */
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
      output.println(
          "Course search (Java 17+)\n"
              + "  --offline         Use the bundled catalogue snapshot\n"
              + "  --sample          Use the small fictional test catalogue\n"
              + "  --catalog PATH    Use a JSON file or ZIP containing courses.json\n"
              + "  --console         Run an interactive console instead of the desktop window\n"
              + "  --help            Show this help\n"
              + "The desktop uses the official Timetable Builder by default.\n"
              + "The offline and console default catalogue is "
              + DEFAULT_CATALOGUE);
      output.flush();
      return 0;
    }
    if (!options.console() && GraphicsEnvironment.isHeadless()) {
      errors.println("No desktop display is available. Run with --console.");
      errors.flush();
      return 2;
    }
    try {
      if (options.live()) {
        output.println("Loading term and division choices from the Timetable Builder...");
        output.flush();
        TtbCourseCatalog catalog = new TtbCourseCatalog();
        TtbCourseCatalog.Filters filters = catalog.fetchFilters();
        SearchView.Filters choices =
            new SearchView.Filters(
                filters.sessions().stream().map(Main::filterChoice).toList(),
                filters.divisions().stream().map(Main::filterChoice).toList());
        display(
            catalog,
            catalog,
            catalog,
            choices,
            "Live catalogue: U of T Timetable Builder",
            false,
            input,
            output);
        return 0;
      }
      String source =
          options.sample()
              ? "SAMPLE DATA — fictional offerings for testing"
              : "Catalogue: " + options.catalogue();
      output.println("Loading " + source + " ...");
      output.flush();
      List<CourseOffering> offerings = load(options);
      InMemoryCourseCatalog catalog = new InMemoryCourseCatalog(offerings);
      output.println(
          "Loaded "
              + offerings.size()
              + " offerings. Catalogue data is a snapshot, not live enrolment.");
      output.flush();
      display(
          catalog,
          catalog,
          new InMemoryOfferingLookup(offerings),
          null,
          source,
          options.console(),
          input,
          output);
      return 0;
    } catch (CatalogLoadException | CatalogAccessException | IOException exception) {
      errors.println("Could not start course search: " + exception.getMessage());
      if (options.live()) {
        errors.println("Check your connection or run with --offline to use the bundled snapshot.");
      } else {
        errors.println(
            "Check the catalogue path and format, or use --sample to test the application.");
      }
      errors.flush();
      return 1;
    }
  }

  private static SearchView.FilterChoice filterChoice(TtbCourseCatalog.Option option) {
    return new SearchView.FilterChoice(option.code(), option.label());
  }

  private static void display(
      CourseCatalog catalog,
      CourseCodeIndex index,
      OfferingLookup offeringLookup,
      SearchView.Filters filters,
      String source,
      boolean console,
      Reader input,
      PrintWriter output)
      throws IOException {
    SearchCoursesViewModel viewModel = new SearchCoursesViewModel();
    SearchCoursesPresenter presenter = new SearchCoursesPresenter(viewModel);
    SuggestCourseCodesViewModel suggestionsModel = new SuggestCourseCodesViewModel();
    SuggestCourseCodesPresenter suggestionsPresenter =
        new SuggestCourseCodesPresenter(suggestionsModel);
    SearchApplication application =
        SearchApplication.connect(catalog, index, presenter, suggestionsPresenter);
    SearchCoursesController controller = new SearchCoursesController(application.search());
    if (console) {
      new ConsoleSearchView(controller, viewModel, input, output).run();
      return;
    }
    SuggestCourseCodesController suggestions =
        new SuggestCourseCodesController(application.suggestions());
    Timetable timetable = new Timetable();
    TimetableViewModel timetableModel = new TimetableViewModel();
    AddSectionController add =
        new AddSectionController(
            new AddSectionInteractor(
                timetable, offeringLookup, new AddSectionPresenter(timetableModel)));
    ViewTimetableController viewTimetable =
        new ViewTimetableController(
            new ViewTimetableInteractor(
                timetable, offeringLookup, new ViewTimetablePresenter(timetableModel)));
    RemoveSectionController remove =
        new RemoveSectionController(
            new RemoveSectionInteractor(timetable, new RemoveSectionPresenter(timetableModel)));
    SwingUtilities.invokeLater(
        () -> {
          WeeklyTimetablePanel timetablePanel =
              new WeeklyTimetablePanel(viewTimetable, remove, timetableModel);
          SearchView desktopView =
              new SearchView(
                  controller,
                  viewModel,
                  suggestions,
                  suggestionsModel,
                  source,
                  add,
                  timetablePanel,
                  filters);
          desktopView.show();
          output.println("Desktop search window opened.");
          output.flush();
        });
  }

  // Sample data is bundled in the JAR; real data is read directly from its existing ZIP.
  private static List<CourseOffering> load(LaunchOptions options)
      throws CatalogLoadException, IOException {
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
  private record LaunchOptions(
      boolean console, boolean sample, boolean help, boolean live, Path catalogue) {
    // Reject conflicting data sources and unknown arguments instead of silently ignoring them.
    static LaunchOptions parse(String[] args) {
      boolean console = false;
      boolean sample = false;
      boolean help = false;
      boolean offline = false;
      Path catalogue = null;
      for (int i = 0; i < args.length; i++) {
        switch (args[i]) {
          case "--console" -> console = true;
          case "--sample" -> sample = true;
          case "--help" -> help = true;
          case "--offline" -> offline = true;
          case "--catalog" -> {
            if (++i >= args.length || args[i].startsWith("--") || catalogue != null) {
              throw new IllegalArgumentException("Supply one path after --catalog.");
            }
            catalogue = Path.of(args[i]);
          }
          default -> throw new IllegalArgumentException("Unknown option: " + args[i]);
        }
      }
      int sourceOptions = (sample ? 1 : 0) + (offline ? 1 : 0) + (catalogue == null ? 0 : 1);
      if (sourceOptions > 1) {
        throw new IllegalArgumentException("Choose one of --offline, --sample, or --catalog.");
      }
      return new LaunchOptions(
          console,
          sample,
          help,
          !console && sourceOptions == 0,
          catalogue == null ? Path.of(DEFAULT_CATALOGUE) : catalogue);
    }
  }
}
