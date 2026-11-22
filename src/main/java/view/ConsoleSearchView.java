package view;

import interfaceadapter.searchcourses.SearchCoursesController;
import interfaceadapter.searchcourses.SearchCoursesViewModel;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;

// An interactive console adapter that also makes the full flow easy to test with scripted input.
public final class ConsoleSearchView {
    private final SearchCoursesController controller;
    private final SearchCoursesViewModel viewModel;
    private final BufferedReader input;
    private final PrintWriter output;

    // The caller owns the streams; this view never closes System.in or System.out.
    public ConsoleSearchView(SearchCoursesController controller, SearchCoursesViewModel viewModel,
                             Reader input, PrintWriter output) {
        this.controller = controller;
        this.viewModel = viewModel;
        this.input = new BufferedReader(input);
        this.output = output;
    }

    // Continue until :quit or end of input. Blank lines exercise normal validation.
    public void run() throws IOException {
        output.println("Enter a full course code (example: CSC108H1). Type :quit to exit.");
        while (true) {
            output.print("\nCourse code > ");
            output.flush();
            String code = input.readLine();
            if (code == null || code.strip().equalsIgnoreCase(":quit")) {
                return;
            }
            controller.search(code);
            output.println(viewModel.state().heading());
            output.println(viewModel.state().details());
        }
    }
}
