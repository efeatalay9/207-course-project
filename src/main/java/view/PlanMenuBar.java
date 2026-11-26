package view;

import interfaceadapter.openplan.OpenPlanController;
import interfaceadapter.saveplan.SavePlanController;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Locale;
import java.util.Objects;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * A "Plan" menu with Save Plan and Open Plan.
 *
 * <p>The menu only picks a file and calls a controller. Feedback is written by the presenters into
 * the timetable's status line, and {@code afterAction} redraws the timetable so an opened plan
 * appears on the grid straight away.
 */
public final class PlanMenuBar extends JMenuBar {
  private static final long serialVersionUID = 1L;
  private static final String EXTENSION = "json";

  private final transient SavePlanController saveController;
  private final transient OpenPlanController openController;
  private final transient Runnable afterAction;
  private final JFileChooser chooser = new JFileChooser();
  private final JMenuItem saveItem = new JMenuItem("Save Plan...");
  private final JMenuItem openItem = new JMenuItem("Open Plan...");

  /**
   * Builds the menu bar. Construct it on Swing's event thread.
   *
   * @param saveController triggers the save-plan use case
   * @param openController triggers the open-plan use case
   * @param afterAction redraws the timetable after either action
   */
  public PlanMenuBar(
      SavePlanController saveController, OpenPlanController openController, Runnable afterAction) {
    this.saveController = Objects.requireNonNull(saveController, "saveController");
    this.openController = Objects.requireNonNull(openController, "openController");
    this.afterAction = Objects.requireNonNull(afterAction, "afterAction");

    chooser.setFileFilter(new FileNameExtensionFilter("Timetable plans (*.json)", EXTENSION));
    int shortcut = shortcutKey();
    saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, shortcut));
    openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, shortcut));
    saveItem.setToolTipText("Save the sections in My Timetable to a file");
    openItem.setToolTipText("Replace My Timetable with a saved plan");
    saveItem.addActionListener(event -> chooseAndSave());
    openItem.addActionListener(event -> chooseAndOpen());

    JMenu menu = new JMenu("Plan");
    menu.setMnemonic(KeyEvent.VK_P);
    menu.add(saveItem);
    menu.add(openItem);
    add(menu);
  }

  /**
   * Shows this menu bar on the window that contains the given component.
   *
   * @param anyComponentInWindow a component already placed in the main window
   */
  public void attachTo(Component anyComponentInWindow) {
    if (SwingUtilities.getWindowAncestor(anyComponentInWindow) instanceof JFrame frame) {
      frame.setJMenuBar(this);
      frame.revalidate();
    }
  }

  /**
   * Saves to a known file without a dialog. The dialog path also ends here.
   *
   * @param file the file to write
   */
  void save(File file) {
    saveController.save(file.getPath());
    afterAction.run();
  }

  /**
   * Opens a known file without a dialog. The dialog path also ends here.
   *
   * @param file the file to read
   */
  void open(File file) {
    openController.open(file.getPath());
    afterAction.run();
  }

  // A cancelled dialog does nothing, so the active plan stays as it was.
  private void chooseAndSave() {
    if (chooser.showSaveDialog(parent()) != JFileChooser.APPROVE_OPTION) {
      return;
    }
    File file = withExtension(chooser.getSelectedFile());
    if (file.exists() && !confirmOverwrite(file)) {
      return;
    }
    save(file);
  }

  private void chooseAndOpen() {
    if (chooser.showOpenDialog(parent()) == JFileChooser.APPROVE_OPTION) {
      open(chooser.getSelectedFile());
    }
  }

  private boolean confirmOverwrite(File file) {
    int answer = JOptionPane.showConfirmDialog(parent(),
        file.getName() + " already exists. Replace it?", "Save Plan",
        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
    return answer == JOptionPane.YES_OPTION;
  }

  private Component parent() {
    Component window = SwingUtilities.getWindowAncestor(this);
    return window == null ? this : window;
  }

  // Cmd on macOS, Ctrl elsewhere; tests run without a display, so fall back to Ctrl there.
  private static int shortcutKey() {
    try {
      return Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
    } catch (HeadlessException exception) {
      return InputEvent.CTRL_DOWN_MASK;
    }
  }

  static File withExtension(File file) {
    String name = file.getName().toLowerCase(Locale.ROOT);
    return name.endsWith("." + EXTENSION) ? file : new File(file.getPath() + "." + EXTENSION);
  }
}
