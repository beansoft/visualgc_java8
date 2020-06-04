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

public class GCSpacePanel extends JPanel {
   private static final int DATASET_SIZE = 1000;
   private Line line;
   private String borderString;
   private FIFOList dataset;
   private boolean reservedMode;
   // $FF: synthetic field
   static final boolean $assertionsDisabled;

   public GCSpacePanel(String var1, long var2, long var4, Color var6) {
      this(var1, var2, var4, var6, false);
   }

   public GCSpacePanel(String var1, long var2, long var4, Color var6, boolean var7) {
      this.reservedMode = var7;
      this.borderString = var1 + " (" + Converter.longToKMGString(var2) + ", ";
      if (var7) {
         this.dataset = new FIFOList(1000, 0.0D, (double)var2);
      } else {
         this.dataset = new FIFOList(1000, 0.0D, (double)var4);
      }

      this.line = new Line(this.dataset, var6);
      if (var7) {
         this.line.updateGrayLevel(1.0D - (double)var4 / (double)var2);
      }

      String var8 = this.borderString + Converter.longToKMGString(var4) + "): " + " Used = ";
      Font var9 = new Font("Dialog", 1, 12);
      Border var10 = BorderFactory.createEtchedBorder(var6, Color.GRAY);
      TitledBorder var11 = BorderFactory.createTitledBorder(var10, var8, 0, 0, var9, var6);
      this.setBorder(var11);
      this.setForeground(var6);
      this.setBackground(Color.BLACK);
      this.setPreferredSize(new Dimension(600, 100));
      this.setLayout(new BorderLayout());
      this.add(this.line, "Center");
      this.addMouseListener(new MouseAdapter() {
         public void mousePressed(MouseEvent var1) {
            this.maybeShowPopup(var1);
         }

         public void mouseReleased(MouseEvent var1) {
            this.maybeShowPopup(var1);
         }

         private void maybeShowPopup(MouseEvent var1) {
            if (var1.isPopupTrigger()) {
               JPopupMenu var2 = new JPopupMenu();
               JCheckBoxMenuItem var3 = new JCheckBoxMenuItem("Show Reserved Space", GCSpacePanel.this.isShowReservedSpace());
               var3.addItemListener(new ItemListener() {
                  public void itemStateChanged(ItemEvent var1) {
                     GCSpacePanel.this.setShowReservedSpace(var1.getStateChange() == 1);
                  }
               });
               var2.add(var3);
               var2.show(GCSpacePanel.this, var1.getX(), var1.getY());
            }

         }
      });
   }

   public Line getLine() {
      return this.line;
   }

   public void setShowReservedSpace(boolean var1) {
      this.reservedMode = var1;
   }

   public boolean isShowReservedSpace() {
      return this.reservedMode;
   }

   public void updateGraph(long var1, long var3, long var5) {
      if (!$assertionsDisabled && !SwingUtilities.isEventDispatchThread()) {
         throw new AssertionError();
      } else {
         this.dataset.add(new Double((double)var5));
         if (this.isShowReservedSpace()) {
            this.dataset.setMaxValue((double)var1);
            this.line.updateGrayLevel(1.0D - (double)var3 / (double)var1);
         } else {
            this.dataset.setMaxValue((double)var3);
            this.line.updateGrayLevel(0.0D);
         }

      }
   }

   public void updateGraph(long var1, long var3, long var5, long var7) {
      if (!$assertionsDisabled && !SwingUtilities.isEventDispatchThread()) {
         throw new AssertionError();
      } else {
         this.dataset.add(new Double((double)var5));
         if (this.isShowReservedSpace()) {
            long var9 = var1 + var7;
            this.dataset.setMaxValue((double)var9);
            this.line.updateGrayLevel(1.0D - (double)var3 / (double)var9);
         } else {
            this.dataset.setMaxValue((double)var3);
            this.line.updateGrayLevel(0.0D);
         }

      }
   }

   public void updateTextComponents(long var1, long var3) {
      TitledBorder var5 = (TitledBorder)this.getBorder();
      Color var6 = this.getForeground();
      if (var5.getTitleColor() != var6) {
         Border var7 = BorderFactory.createEtchedBorder(var6, Color.GRAY);
         var5.setTitleColor(var6);
         var5.setBorder(var7);
      }

      String var8 = this.borderString + Converter.longToKMGString(var1) + "): " + Converter.longToKMGString(var3);
      var5.setTitle(var8);
   }

   public void updateTextComponents(long var1, long var3, long var5, long var7, long var9) {
      TitledBorder var11 = (TitledBorder)this.getBorder();
      Color var12 = this.getForeground();
      if (var11.getTitleColor() != var12) {
         Border var13 = BorderFactory.createEtchedBorder(var12, Color.GRAY);
         var11.setTitleColor(var12);
         var11.setBorder(var13);
      }

      String var14 = this.borderString + Converter.longToKMGString(var1) + "): " + Converter.longToKMGString(var3) + ", " + var5 + " collections, " + Converter.longToTimeString(var7, var9);
      var11.setTitle(var14);
   }

   static {
      $assertionsDisabled = !GCSpacePanel.class.desiredAssertionStatus();
   }
}
