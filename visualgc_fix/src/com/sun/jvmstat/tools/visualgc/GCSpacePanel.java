package com.sun.jvmstat.tools.visualgc;

import com.sun.jvmstat.graph.FIFOList;
import com.sun.jvmstat.graph.Line;
import com.sun.jvmstat.util.Converter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public class GCSpacePanel extends JPanel
{
  private static final int DATASET_SIZE = 1000;
  private Line line;
  private String borderString;
  private FIFOList dataset;
  private boolean reservedMode;

  public GCSpacePanel(String paramString, long paramLong1, long paramLong2, Color paramColor)
  {
    this(paramString, paramLong1, paramLong2, paramColor, false);
  }

  public GCSpacePanel(String paramString, long paramLong1, long paramLong2, Color paramColor, boolean paramBoolean)
  {
    this.reservedMode = paramBoolean;

    this.borderString = (paramString + " (" + Converter.longToKMGString(paramLong1) + ", ");

    if (paramBoolean) {
      this.dataset = new FIFOList(1000, 0.0D, paramLong1);
    }
    else {
      this.dataset = new FIFOList(1000, 0.0D, paramLong2);
    }
    this.line = new Line(this.dataset, paramColor);

    if (paramBoolean) {
      this.line.updateGrayLevel(1.0D - paramLong2 / paramLong1);
    }

    String str = this.borderString + Converter.longToKMGString(paramLong2) + "): " + " Used = ";

    Font localFont = new Font("Dialog", 1, 12);
    Border localBorder = BorderFactory.createEtchedBorder(paramColor, Color.GRAY);
    TitledBorder localTitledBorder = BorderFactory.createTitledBorder(localBorder, str, 0, 0, localFont, paramColor);

    setBorder(localTitledBorder);
    setForeground(paramColor);
    setBackground(Color.BLACK);
    setPreferredSize(new Dimension(600, 100));
    setLayout(new BorderLayout());
    add(this.line, "Center");

    addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent paramMouseEvent) {
        maybeShowPopup(paramMouseEvent);
      }

      public void mouseReleased(MouseEvent paramMouseEvent) {
        maybeShowPopup(paramMouseEvent);
      }

      private void maybeShowPopup(MouseEvent paramMouseEvent) {
        if (paramMouseEvent.isPopupTrigger()) {
          JPopupMenu localJPopupMenu = new JPopupMenu();
          JCheckBoxMenuItem localJCheckBoxMenuItem = new JCheckBoxMenuItem("Show Reserved Space", GCSpacePanel.this.isShowReservedSpace());

          localJCheckBoxMenuItem.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent paramItemEvent) {
              GCSpacePanel.this.setShowReservedSpace(paramItemEvent.getStateChange() == 1);
            }
          });
          localJPopupMenu.add(localJCheckBoxMenuItem);
          localJPopupMenu.show(GCSpacePanel.this, paramMouseEvent.getX(), paramMouseEvent.getY());
        }
      } } );
  }

  public Line getLine() {
    return this.line;
  }

  public void setShowReservedSpace(boolean paramBoolean) {
    this.reservedMode = paramBoolean;
  }

  public boolean isShowReservedSpace() {
    return this.reservedMode;
  }

  public void updateGraph(long paramLong1, long paramLong2, long paramLong3) {
    assert (SwingUtilities.isEventDispatchThread());
    this.dataset.add(new Double(paramLong3));
    if (isShowReservedSpace()) {
      this.dataset.setMaxValue(paramLong1);
      this.line.updateGrayLevel(1.0D - paramLong2 / paramLong1);
    }
    else {
      this.dataset.setMaxValue(paramLong2);
      this.line.updateGrayLevel(0.0D);
    }
  }

  public void updateGraph(long paramLong1, long paramLong2, long paramLong3, long paramLong4)
  {
    assert (SwingUtilities.isEventDispatchThread());
    this.dataset.add(new Double(paramLong3));
    if (isShowReservedSpace()) {
      long l = paramLong1 + paramLong4;
      this.dataset.setMaxValue(l);
      this.line.updateGrayLevel(1.0D - paramLong2 / l);
    }
    else {
      this.dataset.setMaxValue(paramLong2);
      this.line.updateGrayLevel(0.0D);
    }
  }

  public void updateTextComponents(long paramLong1, long paramLong2) {
    TitledBorder localTitledBorder = (TitledBorder)getBorder();
    Color localColor = getForeground();
    if (localTitledBorder.getTitleColor() != localColor) {
      Border localObject = BorderFactory.createEtchedBorder(localColor, Color.GRAY);
      localTitledBorder.setTitleColor(localColor);
      localTitledBorder.setBorder((Border)localObject);
    }
    Object localObject = this.borderString + Converter.longToKMGString(paramLong1) + "): " + Converter.longToKMGString(paramLong2);

    localTitledBorder.setTitle((String)localObject);
  }

  public void updateTextComponents(long paramLong1, long paramLong2, long paramLong3, long paramLong4, long paramLong5)
  {
    TitledBorder localTitledBorder = (TitledBorder)getBorder();
    Color localColor = getForeground();
    if (localTitledBorder.getTitleColor() != localColor) {
    	Border localObject = BorderFactory.createEtchedBorder(localColor, Color.GRAY);
      localTitledBorder.setTitleColor(localColor);
      localTitledBorder.setBorder((Border)localObject);
    }
    Object localObject = this.borderString + Converter.longToKMGString(paramLong1) + "): " + Converter.longToKMGString(paramLong2) + ", " + paramLong3 + " collections, " + Converter.longToTimeString(paramLong4, paramLong5);

    localTitledBorder.setTitle((String)localObject);
  }
}