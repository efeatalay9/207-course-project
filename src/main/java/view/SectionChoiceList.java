package view;

import interfaceadapter.searchcourses.SearchCoursesViewModel.SectionChoice;
import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ToolTipManager;

/** Displays non-blocking warnings with text, colour, and a tooltip for the actual hovered row. */
final class SectionChoiceList extends JList<SectionChoice> {
  SectionChoiceList(DefaultListModel<SectionChoice> model) {
    super(model);
    ToolTipManager.sharedInstance().registerComponent(this);
    setCellRenderer(
        new DefaultListCellRenderer() {
          @Override
          public Component getListCellRendererComponent(
              JList<?> list, Object value, int index, boolean selected, boolean focus) {
            Component component =
                super.getListCellRendererComponent(list, value, index, selected, focus);
            putClientProperty("html.disable", Boolean.TRUE);
            SectionChoice choice = (SectionChoice) value;
            if (!selected && !choice.warning().isEmpty()) {
              component.setForeground(new Color(130, 45, 0));
              component.setBackground(new Color(255, 237, 213));
            }
            return component;
          }
        });
  }

  @Override
  public String getToolTipText(MouseEvent event) {
    int index = locationToIndex(event.getPoint());
    Rectangle bounds = index < 0 ? null : getCellBounds(index, index);
    if (bounds == null || !bounds.contains(event.getPoint())) {
      return null;
    }
    SectionChoice choice = getModel().getElementAt(index);
    String text = choice.meetingDetails();
    if (!choice.warning().isEmpty()) {
      text += "\n" + choice.warning() + "\nYou can still add this section.";
    }
    return "<html>"
        + text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>")
        + "</html>";
  }
}
