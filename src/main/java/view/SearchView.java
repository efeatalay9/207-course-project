package view;

import interfaceadapter.searchcourses.SearchCoursesController;
import interfaceadapter.searchcourses.SearchCoursesViewModel;
import interfaceadapter.suggestcoursecodes.SuggestCourseCodesController;
import interfaceadapter.suggestcoursecodes.SuggestCourseCodesViewModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

// A minimal desktop tester. It renders display state and delegates all search behaviour.
public final class SearchView {
    private final SearchCoursesController controller;
    private final SearchCoursesViewModel viewModel;
    private final CourseCodeDropdown dropdown;
    private final JFrame frame = new JFrame("Course search — CSC207");
    private final JTextField codeField = new JTextField(22);
    private final JLabel heading = new JLabel();
    private final JTextArea results = new JTextArea();

    // Construct on Swing's event thread; catalogue loading has already finished in Main.
    public SearchView(SearchCoursesController controller, SearchCoursesViewModel viewModel,
                      SuggestCourseCodesController suggestions, SuggestCourseCodesViewModel suggestionsModel,
                      String sourceDescription) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Create the search view on Swing's event thread.");
        }
        this.controller = controller;
        this.viewModel = viewModel;
        dropdown = new CourseCodeDropdown(codeField, suggestions, suggestionsModel, this::search);
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        content.add(searchControls(sourceDescription), BorderLayout.NORTH);
        content.add(resultPanel(), BorderLayout.CENTER);
        frame.setContentPane(content);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setMinimumSize(new Dimension(650, 450));
        frame.setSize(980, 720);
        frame.setLocationRelativeTo(null);
        render();
    }

    // Display the tester and put keyboard focus in the course-code field.
    public void show() {
        frame.setVisible(true);
        codeField.requestFocusInWindow();
    }

    // Enter accepts a highlighted suggestion; the button searches the code currently in the field.
    private JPanel searchControls(String sourceDescription) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JLabel title = new JLabel("Find course offerings");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        panel.add(title, BorderLayout.NORTH);
        JPanel controls = new JPanel(new BorderLayout(8, 0));
        JLabel codeLabel = new JLabel("Course code:");
        codeLabel.setLabelFor(codeField);
        controls.add(codeLabel, BorderLayout.WEST);
        controls.add(codeField, BorderLayout.CENTER);
        JButton search = new JButton("Search");
        controls.add(search, BorderLayout.EAST);
        codeField.setToolTipText("Start typing a course code, for example CSC; choose a suggestion or enter a full code");
        search.setToolTipText("Search for the full course code currently entered");
        search.addActionListener(event -> search());
        codeField.addActionListener(event -> dropdown.submit());
        frame.getRootPane().setDefaultButton(search);
        panel.add(controls, BorderLayout.CENTER);
        // A text area treats arbitrary filenames as text, never as Swing HTML.
        JTextArea source = new JTextArea(sourceDescription + "\nCatalogue snapshot — enrolment is not live.");
        source.setEditable(false);
        source.setFocusable(false);
        source.setLineWrap(true);
        source.setWrapStyleWord(true);
        source.setBackground(panel.getBackground());
        source.setFont(codeLabel.getFont());
        panel.add(source, BorderLayout.SOUTH);
        return panel;
    }

    // Read-only, selectable text lets testers copy results; scrolling handles large offerings.
    private JPanel resultPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 16f));
        panel.add(heading, BorderLayout.NORTH);
        results.setEditable(false);
        results.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        results.setLineWrap(true);
        results.setWrapStyleWord(true);
        results.setMargin(new java.awt.Insets(12, 12, 12, 12));
        results.getAccessibleContext().setAccessibleName("Course search results");
        panel.add(new JScrollPane(results), BorderLayout.CENTER);
        return panel;
    }

    // In-memory lookup is small and synchronous; a remote adapter would need a UI worker.
    private void search() {
        controller.search(codeField.getText());
        render();
    }

    // Replace old results completely so errors and empty searches never show stale courses.
    private void render() {
        // Disable HTML parsing of user-supplied query text in the heading.
        heading.putClientProperty("html.disable", Boolean.TRUE);
        heading.setText(viewModel.state().heading());
        results.setText(viewModel.state().details());
        results.setCaretPosition(0);
    }
}
