package view;

import interfaceadapter.checkconflicts.CheckConflictsController;
import interfaceadapter.checkconflicts.ConflictsViewModel;
import java.awt.Component;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Objects;
import java.util.function.Supplier;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * A "Timetable" menu with Check Overlaps.
 *
 * <p>It asks the check-conflicts use case to check the session the student is looking at, then
 * shows whatever the presenter put in the view model. It holds no timetable data itself.
 */
public final class ConflictsMenu extends JMenu {
  private static final long serialVersionUID = 1L;

  private final transient CheckConflictsController controller;
  private final transient ConflictsViewModel viewModel;
  private final transient Supplier<String> currentSession;
  private final JMenuItem checkItem = new JMenuItem("Check Overlaps...");

  /**
   * Builds the menu. Construct it on Swing's event thread.
   *
   * @param controller triggers the check-conflicts use case
   * @param viewModel the result written by the presenter
   * @param currentSession the session code currently shown in My Timetable
   */
  public ConflictsMenu(CheckConflictsController controller, ConflictsViewModel viewModel,
      Supplier<String> currentSession) {
    super("Timetable");
    this.controller = Objects.requireNonNull(controller, "controller");
    this.viewModel = Objects.requireNonNull(viewModel, "viewModel");
    this.currentSession = Objects.requireNonNull(currentSession, "currentSession");
    setMnemonic(KeyEvent.VK_T);
    checkItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, shortcutKey()));
    checkItem.setToolTipText("List the selected meetings that overlap in the shown session");
    checkItem.addActionListener(event -> showResult());
    add(checkItem);
  }

  /**
   * Adds this menu to the menu bar of the window that contains the given component, creating
   * the menu bar if the window has none.
   *
   * @param anyComponentInWindow a component already placed in the main window
   */
  public void attachTo(Component anyComponentInWindow) {
    if (SwingUtilities.getWindowAncestor(anyComponentInWindow) instanceof JFrame frame) {
      JMenuBar bar = frame.getJMenuBar();
      if (bar == null) {
        bar = new JMenuBar();
        frame.setJMenuBar(bar);
      }
      bar.add(this);
      frame.revalidate();
    }
  }

  /**
   * Runs the check and returns the text the dialog shows.
   *
   * @return the heading, conflict lines, and notes
   */
  String check() {
    controller.check(currentSession.get());
    ConflictsViewModel.State state = viewModel.state();
    StringBuilder text = new StringBuilder(state.heading());
    if (!state.conflicts().isEmpty()) {
      text.append("\n\n").append(String.join("\n", state.conflicts()));
    }
    if (!state.notes().isEmpty()) {
      text.append("\n\n").append(String.join("\n", state.notes()));
    }
    return text.toString();
  }

  /**
   * Picks the dialog icon for the latest result.
   *
   * @return a JOptionPane message type
   */
  int messageType() {
    return switch (viewModel.state().level()) {
      case WARNING -> JOptionPane.WARNING_MESSAGE;
      case ERROR -> JOptionPane.ERROR_MESSAGE;
      default -> JOptionPane.INFORMATION_MESSAGE;
    };
  }

  private void showResult() {
    JTextArea text = new JTextArea(check());
    text.setEditable(false);
    text.setLineWrap(true);
    text.setWrapStyleWord(true);
    text.setColumns(48);
    text.setRows(Math.min(14, text.getLineCount() + 1));
    text.setFont(text.getFont().deriveFont(Font.PLAIN));
    text.setOpaque(false);
    Component parent = SwingUtilities.getWindowAncestor(this);
    JOptionPane.showMessageDialog(parent == null ? this : parent, new JScrollPane(text),
        "Overlap check", messageType());
  }

  // Cmd on macOS, Ctrl elsewhere; tests run without a display, so fall back to Ctrl there.
  private static int shortcutKey() {
    try {
      return Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
    } catch (HeadlessException exception) {
      return InputEvent.CTRL_DOWN_MASK;
    }
  }
}
