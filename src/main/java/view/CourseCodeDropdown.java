package view;

import interfaceadapter.suggestcoursecodes.SuggestCourseCodesController;
import interfaceadapter.suggestcoursecodes.SuggestCourseCodesViewModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

// Swing-only autocomplete behaviour. Lookup and normalization stay behind the controller.
final class CourseCodeDropdown {
  private static final int VISIBLE_ROWS = 8;
  private static final int ROW_HEIGHT = 24;
  private final JTextField field;
  private final SuggestCourseCodesController controller;
  private final SuggestCourseCodesViewModel viewModel;
  private final Runnable search;
  private final JPopupMenu popup = new JPopupMenu();
  private final DefaultListModel<String> codes = new DefaultListModel<>();
  private final JList<String> choices = new JList<>(codes);
  private final JScrollPane scroll = new JScrollPane(choices);
  private final JLabel status = new JLabel();
  private final Supplier<SearchView.Selection> scope;
  private final Timer delay = new Timer(300, event -> loadLiveSuggestions());
  private boolean acceptingSelection;
  private boolean loadingSuggestions;
  private boolean queuedSuggestions;
  private int inputVersion;

  // Attach to an existing field so its accessible label and normal editing still work.
  CourseCodeDropdown(
      JTextField field,
      SuggestCourseCodesController controller,
      SuggestCourseCodesViewModel viewModel,
      Runnable search,
      Supplier<SearchView.Selection> scope) {
    this.field = field;
    this.controller = controller;
    this.viewModel = viewModel;
    this.search = search;
    this.scope = scope;
    delay.setRepeats(false);
    configurePopup();
    field
        .getDocument()
        .addDocumentListener(
            new DocumentListener() {
              @Override
              public void insertUpdate(DocumentEvent event) {
                refresh();
              }

              @Override
              public void removeUpdate(DocumentEvent event) {
                refresh();
              }

              @Override
              public void changedUpdate(DocumentEvent event) {
                refresh();
              }
            });
    field.addFocusListener(
        new FocusAdapter() {
          @Override
          public void focusLost(FocusEvent event) {
            dismiss();
          }
        });
    bind("DOWN", "next-course", () -> moveSelection(1));
    bind("UP", "previous-course", () -> moveSelection(-1));
    bind("ESCAPE", "dismiss-courses", this::dismiss);
  }

  // Enter accepts the highlighted suggestion; without an open list it searches raw input.
  void submit() {
    if (popup.isVisible() && choices.getSelectedIndex() >= 0) {
      acceptSelection();
    } else {
      dismiss();
      search.run();
    }
  }

  // Keep focus in the text field while the scrollable list displays every matching code.
  private void configurePopup() {
    popup.setFocusable(false);
    popup.setLayout(new BorderLayout());
    choices.setFocusable(false);
    choices.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    choices.setFixedCellHeight(ROW_HEIGHT);
    choices.getAccessibleContext().setAccessibleName("Matching course codes");
    scroll.setFocusable(false);
    scroll.getVerticalScrollBar().setFocusable(false);
    scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    status.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
    popup.add(scroll, BorderLayout.CENTER);
    popup.add(status, BorderLayout.SOUTH);
    choices.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseReleased(MouseEvent event) {
            int index = choices.locationToIndex(event.getPoint());
            Rectangle bounds = index < 0 ? null : choices.getCellBounds(index, index);
            if (event.getButton() == MouseEvent.BUTTON1
                && bounds != null
                && bounds.contains(event.getPoint())) {
              choices.setSelectedIndex(index);
              acceptSelection();
            }
          }
        });
  }

  // Replaces matches on typing/deletion. Blank input closes the dropdown instead of listing
  // everything.
  private void refresh() {
    if (acceptingSelection) {
      return;
    }
    if (scope != null) {
      dismiss();
      if (field.hasFocus() && field.getText().strip().length() >= 3 && scope.get().complete()) {
        delay.restart();
      }
      return;
    }
    controller.suggest(field.getText());
    showSuggestions(viewModel.state());
  }

  void dismiss() {
    popup.setVisible(false);
    delay.stop();
    queuedSuggestions = false;
    inputVersion++;
  }

  // Keep a single live lookup active and ignore its result after a newer edit or search.
  private void loadLiveSuggestions() {
    if (loadingSuggestions) {
      queuedSuggestions = true;
      return;
    }
    SearchView.Selection selection = scope.get();
    String prefix = field.getText();
    if (!field.hasFocus() || !selection.complete() || prefix.strip().length() < 3) {
      return;
    }
    int lookupVersion = inputVersion;
    loadingSuggestions = true;
    new SwingWorker<SuggestCourseCodesViewModel.State, Void>() {
      @Override
      protected SuggestCourseCodesViewModel.State doInBackground() {
        controller.suggest(prefix, selection.session(), selection.division());
        return viewModel.state();
      }

      @Override
      protected void done() {
        loadingSuggestions = false;
        if (lookupVersion == inputVersion && field.hasFocus()) {
          try {
            showSuggestions(get());
          } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
          } catch (ExecutionException exception) {
            showSuggestions(
                new SuggestCourseCodesViewModel.State(
                    List.of(), "Suggestions are unavailable. Enter a full course code."));
          }
        }
        if (queuedSuggestions) {
          queuedSuggestions = false;
          loadLiveSuggestions();
        }
      }
    }.execute();
  }

  private void showSuggestions(SuggestCourseCodesViewModel.State state) {
    codes.clear();
    codes.addAll(state.codes());
    status.setText(state.message());
    scroll.setVisible(!codes.isEmpty());
    if (!codes.isEmpty()) {
      choices.setSelectedIndex(0);
      choices.ensureIndexIsVisible(0);
    }
    if (field.getText().isBlank()) {
      popup.setVisible(false);
    } else {
      showPopup();
    }
  }

  // At most eight rows are visible, but scrolling can reach every code in the result set.
  private void showPopup() {
    if (!field.isShowing() || !field.hasFocus()) {
      return;
    }
    int height = Math.min(VISIBLE_ROWS, codes.size()) * ROW_HEIGHT;
    int width = Math.max(field.getWidth(), status.getPreferredSize().width + 16);
    scroll.setPreferredSize(new Dimension(width, height));
    popup.setPopupSize(width, height + status.getPreferredSize().height + 8);
    if (!popup.isVisible()) {
      popup.show(field, 0, field.getHeight());
    }
    popup.revalidate();
  }

  // Arrow keys move through suggestions without replacing the prefix being edited.
  private void moveSelection(int delta) {
    if (!popup.isVisible()) {
      refresh();
      return;
    }
    if (!codes.isEmpty()) {
      int index = Math.max(0, Math.min(codes.size() - 1, choices.getSelectedIndex() + delta));
      choices.setSelectedIndex(index);
      choices.ensureIndexIsVisible(index);
    }
  }

  // Suppress document callbacks while inserting a chosen full code, then run exact offering lookup.
  private void acceptSelection() {
    String selected = choices.getSelectedValue();
    if (selected == null) {
      return;
    }
    acceptingSelection = true;
    try {
      field.setText(selected);
    } finally {
      acceptingSelection = false;
    }
    dismiss();
    search.run();
    field.requestFocusInWindow();
  }

  // Install focused-field actions rather than global key listeners.
  private void bind(String key, String name, Runnable action) {
    field.getInputMap().put(KeyStroke.getKeyStroke(key), name);
    field
        .getActionMap()
        .put(
            name,
            new AbstractAction() {
              @Override
              public void actionPerformed(ActionEvent event) {
                action.run();
              }
            });
  }
}
