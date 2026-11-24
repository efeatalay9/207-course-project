package view;

import interfaceadapter.removesection.RemoveSectionController;
import interfaceadapter.viewtimetable.TimetableViewModel;
import interfaceadapter.viewtimetable.TimetableViewModel.SectionItem;
import interfaceadapter.viewtimetable.TimetableViewModel.SessionOption;
import interfaceadapter.viewtimetable.ViewTimetableController;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;

/**
 * The "My Timetable" panel: a weekly grid, a session chooser, and a list of selected sections with
 * a Remove button.
 *
 * <p>It triggers the view-timetable and remove-section use cases through their controllers and
 * renders whatever the presenters put in the view model. It holds no timetable data itself.
 */
public final class WeeklyTimetablePanel extends JPanel {
  private final ViewTimetableController viewController;
  private final RemoveSectionController removeController;
  private final TimetableViewModel viewModel;

  private final JLabel summary = new JLabel();
  private final DefaultComboBoxModel<SessionOption> sessionModel = new DefaultComboBoxModel<>();
  private final JComboBox<SessionOption> sessionChooser = new JComboBox<>(sessionModel);
  private final WeeklyGrid grid = new WeeklyGrid();
  private final DefaultListModel<SectionItem> sectionModel = new DefaultListModel<>();
  private final JList<SectionItem> sectionList = new JList<>(sectionModel);
  private final JButton removeButton = new JButton("Remove");
  private final JTextArea notes = new JTextArea(3, 20);
  private final JLabel status = new JLabel(" ");
  private boolean rendering;
  private Runnable onSelectionsChanged = () -> {};

  /**
   * Builds the panel. Construct it on Swing's event thread.
   *
   * @param viewController triggers the view-timetable use case
   * @param removeController triggers the remove-section use case
   * @param viewModel the timetable and selection feedback written by the presenters
   */
  public WeeklyTimetablePanel(
      ViewTimetableController viewController,
      RemoveSectionController removeController,
      TimetableViewModel viewModel) {
    super(new BorderLayout(8, 8));
    this.viewController = viewController;
    this.removeController = removeController;
    this.viewModel = viewModel;
    setBorder(BorderFactory.createTitledBorder("My Timetable"));

    add(header(), BorderLayout.NORTH);
    JScrollPane gridScroll = new JScrollPane(grid);
    gridScroll.getVerticalScrollBar().setUnitIncrement(16);
    add(gridScroll, BorderLayout.CENTER);
    add(sidebar(), BorderLayout.EAST);
    status.putClientProperty("html.disable", Boolean.TRUE);
    add(status, BorderLayout.SOUTH);

    grid.setOnBlockClicked(this::selectInList);
    sectionList.addListSelectionListener(event -> highlightSelection());
    sessionChooser.addActionListener(event -> sessionChanged());
    removeButton.addActionListener(event -> removeSelected());
    refresh();
  }

  /** Lets the search view refresh candidate warnings after a section is removed. */
  public void setOnSelectionsChanged(Runnable listener) {
    onSelectionsChanged = java.util.Objects.requireNonNull(listener);
  }

  /** Redraws the timetable and the status set by the latest section action. */
  public void refresh() {
    viewController.view(currentSession());
    render();
  }

  private JPanel header() {
    JPanel panel = new JPanel(new BorderLayout(8, 0));
    summary.putClientProperty("html.disable", Boolean.TRUE);
    summary.setFont(summary.getFont().deriveFont(Font.BOLD));
    panel.add(summary, BorderLayout.CENTER);
    JPanel chooser = new JPanel(new BorderLayout(6, 0));
    JLabel label = new JLabel("Session:");
    label.setLabelFor(sessionChooser);
    chooser.add(label, BorderLayout.WEST);
    chooser.add(sessionChooser, BorderLayout.CENTER);
    panel.add(chooser, BorderLayout.EAST);
    return panel;
  }

  private JPanel sidebar() {
    JPanel panel = new JPanel(new BorderLayout(6, 6));
    panel.setPreferredSize(new Dimension(250, 10));
    sectionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    sectionList.getAccessibleContext().setAccessibleName("Selected sections");
    JScrollPane listScroll = new JScrollPane(sectionList);
    listScroll.setBorder(BorderFactory.createTitledBorder("Selected sections"));
    panel.add(listScroll, BorderLayout.CENTER);

    JPanel bottom = new JPanel(new BorderLayout(6, 6));
    removeButton.setToolTipText("Remove the chosen section and all of its meetings");
    bottom.add(removeButton, BorderLayout.NORTH);
    notes.setEditable(false);
    notes.setLineWrap(true);
    notes.setWrapStyleWord(true);
    notes.setOpaque(false);
    notes.setFont(summary.getFont().deriveFont(Font.PLAIN, 11f));
    bottom.add(notes, BorderLayout.CENTER);
    panel.add(bottom, BorderLayout.SOUTH);
    return panel;
  }

  private void sessionChanged() {
    if (rendering) {
      return;
    }
    viewController.view(currentSession());
    render();
  }

  private void removeSelected() {
    SectionItem selected = sectionList.getSelectedValue();
    if (selected == null) {
      removeController.remove(null, null);
    } else {
      removeController.remove(selected.offeringId(), selected.sectionName());
    }
    refresh();
    onSelectionsChanged.run();
  }

  private void selectInList(String offeringId, String sectionName) {
    for (int i = 0; i < sectionModel.size(); i++) {
      SectionItem item = sectionModel.get(i);
      if (item.offeringId().equals(offeringId) && item.sectionName().equals(sectionName)) {
        sectionList.setSelectedIndex(i);
        sectionList.ensureIndexIsVisible(i);
        return;
      }
    }
  }

  private void highlightSelection() {
    SectionItem selected = sectionList.getSelectedValue();
    if (selected == null) {
      grid.setSelected("", "");
    } else {
      grid.setSelected(selected.offeringId(), selected.sectionName());
    }
  }

  private String currentSession() {
    SessionOption selected = (SessionOption) sessionModel.getSelectedItem();
    return selected == null ? "" : selected.code();
  }

  // Copy the view model into Swing widgets; the flag stops the chooser from re-triggering a view.
  private void render() {
    TimetableViewModel.State state = viewModel.state();
    rendering = true;
    try {
      summary.setText(state.summary());

      sessionModel.removeAllElements();
      for (SessionOption option : state.sessions()) {
        sessionModel.addElement(option);
        if (option.code().equals(state.selectedSession())) {
          sessionModel.setSelectedItem(option);
        }
      }
      sessionChooser.setEnabled(state.sessions().size() > 1);

      final SectionItem previous = sectionList.getSelectedValue();
      sectionModel.clear();
      for (SectionItem item : state.sections()) {
        sectionModel.addElement(item);
      }
      removeButton.setEnabled(!state.sections().isEmpty());
      notes.setText(String.join("\n", state.notes()));

      grid.setState(state);
      if (previous != null) {
        selectInList(previous.offeringId(), previous.sectionName());
      }
      highlightSelection();
      status.setText(viewModel.status().isEmpty() ? " " : viewModel.status());
    } finally {
      rendering = false;
    }
  }
}
