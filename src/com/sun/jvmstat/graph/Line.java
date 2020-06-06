package com.sun.jvmstat.graph;

import com.sun.jvmstat.tools.visualgc.GCSample;
import com.sun.jvmstat.tools.visualgc.resource.Res;
import com.sun.jvmstat.util.Converter;
import org.graalvm.visualvm.core.ui.components.NotSupportedDisplayer;

import java.awt.*;
import java.awt.event.*;
import java.sql.Timestamp;
import java.util.Date;
import javax.swing.*;

public class Line extends JComponent {
  public static final int DEFAULT_DATASET_SIZE = 1000;
  private static final int XSCALE = 2;
  private static final Color SPLIT_GRID_COLOR;
  private Color color;
  private GridDrawer gridDrawer;
  private FIFOList dataset;

  private boolean timeMode = false;

  java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat(
          "HH:mm:ss.SSS");

  private MouseListener mouseListener = new MouseListener();

  public Line(Color color) {
    this(new FIFOList(DEFAULT_DATASET_SIZE), color);
  }

  public Line(FIFOList dataset) {
    this(dataset, Color.LIGHT_GRAY);
  }

  public Line(FIFOList dataset, Color color) {
    this.dataset = dataset;
    this.color = color;
    this.gridDrawer = new GridDrawer(10, 10);
    this.gridDrawer.setSecondaryColor(SPLIT_GRID_COLOR);
    this.setPreferredSize(new Dimension(600, 100));
    this.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent var1) {
        super.componentResized(var1);
        Line.this.validate();
      }
    });

    this.addMouseListener(mouseListener);
    this.addMouseMotionListener(mouseListener);
  }

  public boolean isTimeMode() {
    return timeMode;
  }

  public void setTimeMode(boolean timeMode) {
    this.timeMode = timeMode;
  }

  public Color getColor() {
    return this.color;
  }

  public void setColor(Color var1) {
    this.color = var1;
  }

  public Color getGridPrimaryColor() {
    return this.gridDrawer.getPrimaryColor();
  }

  public void setGridPrimaryColor(Color var1) {
    this.gridDrawer.setPrimaryColor(var1);
  }

  public Color getGridSecondaryColor() {
    return this.gridDrawer.getSecondaryColor();
  }

  public void setGridSecondaryColor(Color var1) {
    this.gridDrawer.setSecondaryColor(var1);
  }

  protected void drawGraph(Graphics g) {
    if (!this.dataset.isEmpty()) {
      int width = this.getWidth();
      int height = this.getHeight();
      double maxValue = this.dataset.getMaxValue();
      int size = this.dataset.size();
      int pointsToDisplay = Math.min(size, width / XSCALE);
      double yRatio = (double)height / maxValue;
      int[] xPoints = new int[pointsToDisplay + 2];
      int[] yPoints = new int[pointsToDisplay + 2];
      int startX = width - pointsToDisplay * XSCALE;
      int dataStartX = size - pointsToDisplay;
//      System.out.println("size=" + size + " pointsToDisplay=" + pointsToDisplay +" startX=" + startX + ", dataStartX=" + dataStartX);


      xPoints[0] = startX;// Polygon first point
      yPoints[0] = height;

      for(int i = 0; i < pointsToDisplay; ++i) {
        double value = this.dataset.get(dataStartX + i).doubleValue();
        int y = height - (int)(value * yRatio);
        if (value > 0.0D && y < 0) {
          y = 0;
        }

        if (value > 0.0D && y >= height) {
          y = height;
        }

        xPoints[i + 1] = startX + i * XSCALE;
        yPoints[i + 1] = y;
      }

      xPoints[pointsToDisplay + 1] = width;// Polygon close chart
      yPoints[pointsToDisplay + 1] = height;
      g.setColor(this.color);
      g.fillPolygon(xPoints, yPoints, pointsToDisplay + 2);// Fill a polygon chart as a area chart

      if(inChart ) { //&& mouseX >= startX - XSCALE
        int idx = dataStartX;
        int posIdx = 0;
        int valueX = 0;

        if (mouseX >= xPoints[xPoints.length - 2]) {
//          System.out.println("right empty");
          idx = dataStartX + pointsToDisplay - 1;
          posIdx = xPoints.length - 2;
          valueX = xPoints[posIdx];
        } else if(mouseX <= xPoints[1]) {
//          System.out.println("left empty");
          idx = 0;
          posIdx = 1;
          valueX = xPoints[posIdx];
          dataX = valueX - 100;
        } else {
          for(int i = 1; i < pointsToDisplay; ++i) {
            int curIdx = dataStartX + i;
            int xPointsPos = i + 1;
            int xPointsPosNext = i + 2;

            long deltaLeft = xPoints[xPointsPos] - mouseX;
            long deltaRight = xPoints[xPointsPosNext] - mouseX;

//            System.out.println("deltaLeft=" + deltaLeft + " deltaRight=" + deltaRight + " curIdx=" + curIdx);

            if( deltaLeft >= 0 && deltaRight >= 0) {
              if(deltaLeft > deltaRight) {
                idx = curIdx + 1;
                posIdx = xPointsPosNext;
                valueX = xPoints[posIdx];
              } else {
                idx = curIdx;
                posIdx = xPointsPos;
                valueX = xPoints[posIdx];
              }

              break;
            }
        }

//          else if (deltaLeft < 0) {
//            idx = curIdx;
//            posIdx = xPointsPos;
//            dataX = xPoints[xPointsPos];
//            break;
//          }
        }

//        int idx = (mouseX - startX) / XSCALE;
        //int idx = (int) Math.round((mouseX - startX) * 1.0d / pointsToDisplay / 2 *  size);
//        if(idx >= size) {
//          idx = size - 1;
//        }

//        System.out.println("idx-pointsToDisplay=" + (idx-pointsToDisplay*XSCALE) + " idx=" + idx + " xPoints.length=" + xPoints.length + " yPoints.length=" + yPoints.length);

        long value = this.dataset.get( idx).longValue();
//
        int y = yPoints[posIdx];
//        System.out.println("value=" + value + ",y=" + y);
        int markRadius = 2;
        g.setColor(Color.red);
//        g.fillOval(mouseX - markRadius, y - markRadius,
//                markRadius * 2, markRadius * 2);
        g.drawLine(valueX, 0, valueX, height);

        g.setColor(Color.black);
        Long timestamp = dataset.getTimestamp(idx);
        String timeStr = dateFormat.format(new Date(timestamp));

        if(timeMode) {
          g.drawString(timeStr + " = " + Converter.longToTimeString(value, GCSample.osFrequency), dataX, 10);
        } else {
          g.drawString(timeStr + " = " + Converter.longToKMGString(value), dataX, 10);
        }

      }

    }
  }

  public void updateGrayLevel(double splitLevel) {
    this.gridDrawer.splitRange(splitLevel);
  }

  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    this.gridDrawer.draw(g, this.getWidth(), this.getHeight());
    this.drawGraph(g);
  }

  static {
    SPLIT_GRID_COLOR = Color.DARK_GRAY;
  }

  private void updateHighlightedItems() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        if(mouseX + 200 > getWidth()) {
          dataX = getWidth() - 200;
        } else {
          dataX = mouseX;
        }

        repaint(100);
      }
    });
  }


  private int mouseX;
  private int dataX;
  private int mouseY;
  private boolean inChart;

  private class MouseListener extends MouseAdapter implements MouseMotionListener {

    public void mouseEntered(MouseEvent e) {
      inChart = true;
      mouseX = e.getX();
      mouseY = e.getY();
      updateHighlightedItems();
    }

    public void mouseExited(MouseEvent e) {
      inChart = false;

      updateHighlightedItems();
    }

    public void mouseMoved(MouseEvent e) {
      mouseX = e.getX();
      mouseY = e.getY();

      updateHighlightedItems();
    }

  }

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setTitle("GC");
    FIFOList gcActiveDataSet = new FIFOList(2000, 0.0D, 10D);
    for(int i = 0; i < 4; i++) {
      FIFOList.timeStamp = System.currentTimeMillis() + (i * 2) * 10000;
      gcActiveDataSet.add(i);
    }

    Line gcActiveLine = new Line(gcActiveDataSet, Color.yellow);

    frame.getContentPane().add(gcActiveLine, "Center");
    frame.setBounds(0, 0, 600, 100);
    frame.setVisible(true);
    gcActiveLine.repaint();
  }
}
