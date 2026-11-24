package view;

import interfaceadapter.searchcourses.SearchCoursesController;
import interfaceadapter.searchcourses.SearchCoursesViewModel;
import interfaceadapter.suggestcoursecodes.SuggestCourseCodesController;
import interfaceadapter.suggestcoursecodes.SuggestCourseCodesViewModel;
import interfaceadapter.searchcourses.SearchCoursesViewModel.SectionChoice;
import interfaceadapter.addsection.AddSectionController;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;

// A minimal desktop tester. It renders display state and delegates all search behaviour.
public final class SearchView {
    private final SearchCoursesController controller;
    private final AddSectionController addSectionController;
    private final SearchCoursesViewModel viewModel;
    private final CourseCodeDropdown dropdown;
    private final JFrame frame = new JFrame("Course search — CSC207");
    private final JTextField codeField = new JTextField(22);
    private final JLabel heading = new JLabel();
    private final JTextArea results = new JTextArea();
    private final WeeklyTimetablePanel timetablePanel;
    private final DefaultListModel<SectionChoice> sectionChoices = new DefaultListModel<>();
    private final JList<SectionChoice> sectionList = new JList<>(sectionChoices);

    // Construct on Swing's event thread; catalogue loading has already finished in Main.
    public SearchView(SearchCoursesController controller, SearchCoursesViewModel viewModel,
                      SuggestCourseCodesController suggestions, SuggestCourseCodesViewModel suggestionsModel,
                      String sourceDescription, AddSectionController addSectionController,
                      WeeklyTimetablePanel timetablePanel) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Create the search view on Swing's event thread.");
        }
        this.controller = controller;
        this.addSectionController = addSectionController;
        this.timetablePanel = timetablePanel;
        this.viewModel = viewModel;
        dropdown = new CourseCodeDropdown(codeField, suggestions, suggestionsModel, this::search);
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        content.add(searchControls(sourceDescription), BorderLayout.NORTH);
        content.add(resultPanel(), BorderLayout.CENTER);
        frame.setContentPane(content);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setMinimumSize(new Dimension(800, 600));
        frame.setSize(1200, 900);
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
        sectionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane sectionScroll = new JScrollPane(sectionList);

        JButton addButton = new JButton("Add to Timetable");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                addSelectedSection();
            }
        });

        JPanel sectionsPanel = new JPanel(new BorderLayout(8, 8));
        sectionsPanel.setBorder(BorderFactory.createTitledBorder("Available sections"));
        sectionsPanel.add(sectionScroll, BorderLayout.CENTER);
        sectionsPanel.add(addButton, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(results), sectionsPanel);
        split.setResizeWeight(0.65);
        // Search results on top, the weekly timetable below; the divider can be dragged.
        JSplitPane vertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, split, timetablePanel);
        vertical.setResizeWeight(0.4);
        panel.add(vertical, BorderLayout.CENTER);

        return panel;
    }

    private void addSelectedSection() {
        SectionChoice selected = sectionList.getSelectedValue();

        if (selected == null) {
            return;
        }

        boolean added = addSectionController.addSection(selected.offeringId(), selected.sectionName());

        if (added) {
            // The weekly grid redraws from the timetable, so it shows every meeting of the new section.
            timetablePanel.refresh();
        } else {
            System.out.println("Already in timetable: " + selected);
        }
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
        sectionChoices.clear();

        for (SectionChoice choice : viewModel.state().sectionChoices()) {
            sectionChoices.addElement(choice);
        }
    }
}
