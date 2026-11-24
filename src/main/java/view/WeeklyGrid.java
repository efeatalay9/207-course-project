package view;

import interfaceadapter.viewtimetable.TimetableViewModel;
import interfaceadapter.viewtimetable.TimetableViewModel.Block;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import javax.swing.JComponent;
import javax.swing.ToolTipManager;

/**
 * Draws a weekly timetable: one column per day, one row per hour, and a coloured block per meeting.
 *
 * <p>All positions come from the view model, so this class only decides pixel layout. When two
 * meetings in the same column overlap in time, they are drawn side by side so neither is hidden.
 */
final class WeeklyGrid extends JComponent {
  private static final int HEADER_HEIGHT = 28;
  private static final int GUTTER_WIDTH = 52;
  private static final int HOUR_HEIGHT = 46;
  private static final Color GRID_LINE = new Color(0xDD, 0xDD, 0xDD);
  private static final Color HEADER_BACKGROUND = new Color(0xF4, 0xF4, 0xF4);
  private static final Color TEXT = new Color(0x22, 0x22, 0x22);
  private static final Color[] PALETTE = {
    new Color(0xCF, 0xE2, 0xFF), new Color(0xD7, 0xF2, 0xD0), new Color(0xFF, 0xE3, 0xC2),
    new Color(0xF2, 0xD4, 0xF7), new Color(0xFF, 0xD6, 0xD6), new Color(0xD3, 0xF0, 0xF2),
    new Color(0xF5, 0xEE, 0xC4), new Color(0xE1, 0xDC, 0xFA),
  };

  private TimetableViewModel.State state = TimetableViewModel.State.empty("");
  private String selectedOfferingId = "";
  private String selectedSectionName = "";
  private final List<PlacedBlock> placed = new ArrayList<>();
  private final Map<String, Color> courseColours = new LinkedHashMap<>();
  private BiConsumer<String, String> onBlockClicked = (offeringId, sectionName) -> {};

  /** Creates an empty grid that reports clicks on meeting blocks. */
  WeeklyGrid() {
    setOpaque(true);
    setBackground(Color.WHITE);
    ToolTipManager.sharedInstance().registerComponent(this);
    addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent event) {
            PlacedBlock hit = blockAt(event.getX(), event.getY());
            if (hit != null) {
              onBlockClicked.accept(hit.block().offeringId(), hit.block().sectionName());
            }
          }
        });
  }

  /**
   * Sets what happens when the student clicks a meeting block.
   *
   * @param listener receives the offering ID and section name of the clicked block
   */
  void setOnBlockClicked(BiConsumer<String, String> listener) {
    onBlockClicked = listener;
  }

  /**
   * Replaces the displayed timetable.
   *
   * @param state the presenter's grid state
   */
  void setState(TimetableViewModel.State state) {
    this.state = state;
    // Give each course its own colour, in the order courses first appear.
    courseColours.clear();
    for (Block block : state.blocks()) {
      if (!courseColours.containsKey(block.courseCode())) {
        courseColours.put(block.courseCode(), PALETTE[courseColours.size() % PALETTE.length]);
      }
    }
    revalidate();
    repaint();
  }

  /**
   * Highlights every block of one section, for example the one chosen in the list.
   *
   * @param offeringId the section's offering, or empty for none
   * @param sectionName the section name, or empty for none
   */
  void setSelected(String offeringId, String sectionName) {
    selectedOfferingId = offeringId;
    selectedSectionName = sectionName;
    repaint();
  }

  @Override
  public Dimension getPreferredSize() {
    int hours = Math.max(1, state.lastHour() - state.firstHour());
    return new Dimension(
        GUTTER_WIDTH + state.dayLabels().size() * 110, HEADER_HEIGHT + hours * HOUR_HEIGHT + 1);
  }

  @Override
  public String getToolTipText(MouseEvent event) {
    PlacedBlock hit = blockAt(event.getX(), event.getY());
    return hit == null
        ? null
        : hit.block().title()
            + " · "
            + hit.block().detail()
            + (hit.block().warning().isEmpty() ? "" : " · " + hit.block().warning());
  }

  @Override
  protected void paintComponent(Graphics graphics) {
    Graphics2D g = (Graphics2D) graphics.create();
    try {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g.setRenderingHint(
          RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      g.setColor(getBackground());
      g.fillRect(0, 0, getWidth(), getHeight());
      paintGrid(g);
      layoutBlocks();
      for (PlacedBlock block : placed) {
        paintBlock(g, block);
      }
    } finally {
      g.dispose();
    }
  }

  private double columnWidth() {
    return (getWidth() - GUTTER_WIDTH) / (double) Math.max(1, state.dayLabels().size());
  }

  private int verticalPositionFor(int minuteOfDay) {
    return HEADER_HEIGHT
        + (int) Math.round((minuteOfDay - state.firstHour() * 60) * HOUR_HEIGHT / 60.0);
  }

  private void paintGrid(Graphics2D g) {
    final int days = state.dayLabels().size();
    final double width = columnWidth();
    final int bottom = verticalPositionFor(state.lastHour() * 60);
    g.setColor(HEADER_BACKGROUND);
    g.fillRect(0, 0, getWidth(), HEADER_HEIGHT);

    Font base = getFont() == null ? new Font(Font.SANS_SERIF, Font.PLAIN, 12) : getFont();
    g.setFont(base.deriveFont(Font.BOLD, 12f));
    FontMetrics metrics = g.getFontMetrics();
    g.setColor(TEXT);
    for (int day = 0; day < days; day++) {
      String label = state.dayLabels().get(day);
      int x = GUTTER_WIDTH + (int) (day * width + (width - metrics.stringWidth(label)) / 2);
      g.drawString(label, x, (HEADER_HEIGHT + metrics.getAscent()) / 2 - 1);
    }

    g.setFont(base.deriveFont(Font.PLAIN, 11f));
    metrics = g.getFontMetrics();
    for (int hour = state.firstHour(); hour <= state.lastHour(); hour++) {
      int y = verticalPositionFor(hour * 60);
      g.setColor(GRID_LINE);
      g.drawLine(GUTTER_WIDTH, y, getWidth(), y);
      if (hour < state.lastHour()) {
        g.setColor(Color.GRAY);
        g.drawString(String.format("%02d:00", hour), 8, y + metrics.getAscent() + 2);
      }
    }
    g.setColor(GRID_LINE);
    for (int day = 0; day <= days; day++) {
      int x = GUTTER_WIDTH + (int) Math.round(day * width);
      g.drawLine(x, 0, x, bottom);
    }
  }

  // Groups blocks that overlap in the same column and gives each a lane within its group.
  private void layoutBlocks() {
    placed.clear();
    final double width = columnWidth();
    for (int column = 0; column < state.dayLabels().size(); column++) {
      List<Block> blocks = new ArrayList<>();
      for (Block block : state.blocks()) {
        if (block.column() == column) {
          blocks.add(block);
        }
      }
      blocks.sort(Comparator.comparingInt(Block::startMinute).thenComparingInt(Block::endMinute));

      int index = 0;
      while (index < blocks.size()) {
        List<Block> group = new ArrayList<>();
        List<Integer> laneEnds = new ArrayList<>();
        List<Integer> lanes = new ArrayList<>();
        int groupEnd = blocks.get(index).endMinute();
        while (index < blocks.size()
            && (group.isEmpty() || blocks.get(index).startMinute() < groupEnd)) {
          Block block = blocks.get(index);
          int lane = 0;
          while (lane < laneEnds.size() && laneEnds.get(lane) > block.startMinute()) {
            lane++;
          }
          if (lane == laneEnds.size()) {
            laneEnds.add(block.endMinute());
          } else {
            laneEnds.set(lane, block.endMinute());
          }
          group.add(block);
          lanes.add(lane);
          groupEnd = Math.max(groupEnd, block.endMinute());
          index++;
        }
        double laneWidth = (width - 6) / laneEnds.size();
        for (int i = 0; i < group.size(); i++) {
          Block block = group.get(i);
          int x = GUTTER_WIDTH + (int) Math.round(column * width + 3 + lanes.get(i) * laneWidth);
          int y = verticalPositionFor(block.startMinute()) + 1;
          int height = Math.max(14, verticalPositionFor(block.endMinute()) - y - 1);
          placed.add(new PlacedBlock(block, new Rectangle(x, y, (int) laneWidth - 2, height)));
        }
      }
    }
  }

  private void paintBlock(Graphics2D g, PlacedBlock placedBlock) {
    Block block = placedBlock.block();
    Rectangle bounds = placedBlock.bounds();
    Color fill =
        block.warning().isEmpty()
            ? courseColours.getOrDefault(block.courseCode(), PALETTE[0])
            : new Color(255, 210, 190);
    boolean selected =
        block.offeringId().equals(selectedOfferingId)
            && block.sectionName().equals(selectedSectionName);

    g.setColor(fill);
    g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 8, 8);
    g.setColor(selected ? TEXT : block.warning().isEmpty() ? fill.darker() : new Color(160, 45, 0));
    g.setStroke(new BasicStroke(selected ? 2.5f : 1f));
    g.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 8, 8);

    Shape oldClip = g.getClip();
    g.clipRect(bounds.x + 4, bounds.y + 2, bounds.width - 8, bounds.height - 4);
    Font base = getFont() == null ? new Font(Font.SANS_SERIF, Font.PLAIN, 12) : getFont();
    g.setColor(TEXT);
    g.setFont(base.deriveFont(Font.BOLD, 11f));
    FontMetrics metrics = g.getFontMetrics();
    int y = bounds.y + 2 + metrics.getAscent();
    g.drawString(block.title(), bounds.x + 5, y);
    g.setFont(base.deriveFont(Font.PLAIN, 10.5f));
    g.drawString(block.detail(), bounds.x + 5, y + g.getFontMetrics().getHeight());
    g.setClip(oldClip);
  }

  private PlacedBlock blockAt(int x, int y) {
    for (PlacedBlock block : placed) {
      if (block.bounds().contains(x, y)) {
        return block;
      }
    }
    return null;
  }

  // A block together with the rectangle it occupies on screen, used for painting and clicks.
  private record PlacedBlock(Block block, Rectangle bounds) {}
}
