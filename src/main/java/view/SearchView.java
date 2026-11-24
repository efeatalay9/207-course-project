package view;

import interfaceadapter.addsection.AddSectionController;
import interfaceadapter.searchcourses.SearchCoursesController;
import interfaceadapter.searchcourses.SearchCoursesViewModel;
import interfaceadapter.searchcourses.SearchCoursesViewModel.SectionChoice;
import interfaceadapter.suggestcoursecodes.SuggestCourseCodesController;
import interfaceadapter.suggestcoursecodes.SuggestCourseCodesViewModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/** Displays course search results and lets students add sections to the weekly timetable. */
public final class SearchView {
  private final SearchCoursesController controller;
  private final AddSectionController addSectionController;
  private final SearchCoursesViewModel viewModel;
  private final CourseCodeDropdown dropdown;
  private final JFrame frame = new JFrame("Course search — CSC207");
  private final JTextField codeField = new JTextField(22);
  private final JButton searchButton = new JButton("Search");
  private final JComboBox<FilterChoice> sessionPicker = new JComboBox<>();
  private final JComboBox<FilterChoice> divisionPicker = new JComboBox<>();
  private final boolean live;
  private final JLabel heading = new JLabel();
  private final JTextArea results = new JTextArea();
  private final WeeklyTimetablePanel timetablePanel;
  private final DefaultListModel<SectionChoice> sectionChoices = new DefaultListModel<>();
  private final JList<SectionChoice> sectionList = new JList<>(sectionChoices);
  private boolean searching;

  /**
   * Creates an offline search window on Swing's event dispatch thread.
   *
   * @param controller starts exact-code searches
   * @param viewModel provides the formatted search results
   * @param suggestions requests autocomplete suggestions
   * @param suggestionsModel provides the suggested course codes
   * @param sourceDescription identifies the loaded catalogue
   * @param addSectionController handles the selected section
   * @param timetablePanel displays the active timetable
   */
  public SearchView(
      SearchCoursesController controller,
      SearchCoursesViewModel viewModel,
      SuggestCourseCodesController suggestions,
      SuggestCourseCodesViewModel suggestionsModel,
      String sourceDescription,
      AddSectionController addSectionController,
      WeeklyTimetablePanel timetablePanel) {
    this(
        controller,
        viewModel,
        suggestions,
        suggestionsModel,
        sourceDescription,
        addSectionController,
        timetablePanel,
        null);
  }

  /**
   * Creates a search window on Swing's event dispatch thread for the selected catalogue source.
   *
   * @param controller starts exact-code searches
   * @param viewModel provides the formatted search results
   * @param suggestions requests autocomplete suggestions
   * @param suggestionsModel provides the suggested course codes
   * @param sourceDescription identifies the catalogue
   * @param addSectionController handles the selected section
   * @param timetablePanel displays the active timetable
   * @param filters available term and division choices, or null for offline search
   */
  public SearchView(
      SearchCoursesController controller,
      SearchCoursesViewModel viewModel,
      SuggestCourseCodesController suggestions,
      SuggestCourseCodesViewModel suggestionsModel,
      String sourceDescription,
      AddSectionController addSectionController,
      WeeklyTimetablePanel timetablePanel,
      Filters filters) {
    if (!SwingUtilities.isEventDispatchThread()) {
      throw new IllegalStateException("Create the search view on Swing's event thread.");
    }
    this.controller = controller;
    this.addSectionController = addSectionController;
    this.timetablePanel = timetablePanel;
    this.viewModel = viewModel;
    live = filters != null;
    dropdown =
        new CourseCodeDropdown(
            codeField, suggestions, suggestionsModel, this::search, live ? this::selection : null);
    JPanel content = new JPanel(new BorderLayout(12, 12));
    content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    content.add(searchControls(sourceDescription, filters), BorderLayout.NORTH);
    content.add(resultPanel(), BorderLayout.CENTER);
    frame.setContentPane(content);
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.setMinimumSize(new Dimension(800, 600));
    frame.setSize(1200, 900);
    frame.setLocationRelativeTo(null);
    if (live) {
      filterChanged();
    } else {
      render();
    }
  }

  /** Opens the window and focuses the first control needed for a search. */
  public void show() {
    frame.setVisible(true);
    if (live) {
      sessionPicker.requestFocusInWindow();
    } else {
      codeField.requestFocusInWindow();
    }
  }

  // Enter accepts a highlighted suggestion; the button searches the code currently in the field.
  private JPanel searchControls(String sourceDescription, Filters filters) {
    JPanel panel = new JPanel(new BorderLayout(8, 8));
    JLabel title = new JLabel("Find course offerings");
    title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
    panel.add(title, BorderLayout.NORTH);
    JPanel controls = new JPanel(new BorderLayout(8, 0));
    JLabel codeLabel = new JLabel("Course code:");
    codeLabel.setLabelFor(codeField);
    controls.add(codeLabel, BorderLayout.WEST);
    controls.add(codeField, BorderLayout.CENTER);
    controls.add(searchButton, BorderLayout.EAST);
    codeField.setToolTipText(
        "Start typing a course code, for example CSC; choose a suggestion or enter a full code");
    searchButton.setToolTipText("Search for the full course code currently entered");
    searchButton.addActionListener(event -> search());
    codeField.addActionListener(event -> dropdown.submit());
    frame.getRootPane().setDefaultButton(searchButton);
    JPanel form = new JPanel(new BorderLayout(8, 8));
    if (live) {
      form.add(filterControls(filters), BorderLayout.NORTH);
    }
    form.add(controls, BorderLayout.CENTER);
    panel.add(form, BorderLayout.CENTER);
    // A text area treats arbitrary filenames as text, never as Swing HTML.
    String sourceNote =
        live
            ? "Choose a term and division before searching. Use ACORN to enrol."
            : "Catalogue snapshot — enrolment is not live.";
    JTextArea source = new JTextArea(sourceDescription + "\n" + sourceNote);
    source.setEditable(false);
    source.setFocusable(false);
    source.setLineWrap(true);
    source.setWrapStyleWord(true);
    source.setBackground(panel.getBackground());
    source.setFont(codeLabel.getFont());
    panel.add(source, BorderLayout.SOUTH);
    return panel;
  }

  private JPanel filterControls(Filters filters) {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    addFilter(panel, "Term:", sessionPicker, "Choose a term", filters.sessions());
    addFilter(panel, "Division:", divisionPicker, "Choose a division", filters.divisions());
    return panel;
  }

  private void addFilter(
      JPanel panel,
      String labelText,
      JComboBox<FilterChoice> picker,
      String placeholder,
      List<FilterChoice> choices) {
    JLabel label = new JLabel(labelText);
    label.setLabelFor(picker);
    picker.addItem(new FilterChoice("", placeholder));
    for (FilterChoice choice : choices) {
      picker.addItem(choice);
    }
    picker.addActionListener(event -> filterChanged());
    panel.add(label);
    panel.add(picker);
  }

  private Selection selection() {
    return new Selection(
        sessionPicker.getItemAt(sessionPicker.getSelectedIndex()).code(),
        divisionPicker.getItemAt(divisionPicker.getSelectedIndex()).code());
  }

  private void filterChanged() {
    dropdown.dismiss();
    boolean complete = selection().complete();
    codeField.setEnabled(complete);
    searchButton.setEnabled(complete);
    if (complete) {
      showStatus(
          "Ready to search", "Enter a full course code or type three characters for suggestions.");
    } else {
      showStatus(
          "Choose a term and division", "Course results are fetched after both are selected.");
    }
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
    addButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent event) {
            addSelectedSection();
          }
        });

    JPanel sectionsPanel = new JPanel(new BorderLayout(8, 8));
    sectionsPanel.setBorder(BorderFactory.createTitledBorder("Available sections"));
    sectionsPanel.add(sectionScroll, BorderLayout.CENTER);
    sectionsPanel.add(addButton, BorderLayout.SOUTH);

    JSplitPane split =
        new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(results), sectionsPanel);
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
      addSectionController.addSection(null, null);
    } else {
      addSectionController.addSection(selected.offeringId(), selected.sectionName());
    }
    timetablePanel.refresh();
  }

  private void search() {
    dropdown.dismiss();
    String code = codeField.getText();
    if (!live) {
      controller.search(code);
      render();
      return;
    }
    Selection selected = selection();
    if (searching || !selected.complete()) {
      return;
    }
    setSearching(true);
    showStatus(
        "Searching the Timetable Builder", "Getting course offerings for the selected filters…");
    new SwingWorker<Void, Void>() {
      @Override
      protected Void doInBackground() {
        controller.search(code, selected.session(), selected.division());
        return null;
      }

      @Override
      protected void done() {
        try {
          get();
          render();
        } catch (InterruptedException exception) {
          Thread.currentThread().interrupt();
          showStatus("Search interrupted", "Try searching again.");
        } catch (ExecutionException exception) {
          showStatus("Search unavailable", "Try searching again.");
        } finally {
          setSearching(false);
        }
      }
    }.execute();
  }

  private void setSearching(boolean active) {
    searching = active;
    codeField.setEnabled(!active);
    searchButton.setEnabled(!active);
    sessionPicker.setEnabled(!active);
    divisionPicker.setEnabled(!active);
  }

  private void showStatus(String title, String detail) {
    heading.setText(title);
    results.setText(detail);
    results.setCaretPosition(0);
    sectionChoices.clear();
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

  /** A visible filter label with the corresponding source code. */
  public record FilterChoice(String code, String label) {
    @Override
    public String toString() {
      return label;
    }
  }

  /** Options for the two required live catalogue filters. */
  public record Filters(List<FilterChoice> sessions, List<FilterChoice> divisions) {}

  record Selection(String session, String division) {
    boolean complete() {
      return !session.isBlank() && !division.isBlank();
    }
  }
}
