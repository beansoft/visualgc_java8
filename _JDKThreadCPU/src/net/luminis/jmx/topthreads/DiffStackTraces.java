/*
 * Copyright 2007-2016 Peter Doornbosch
 *
 * This file is part of TopThreads, a JConsole plugin to analyse CPU-usage per thread.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * TopThreads is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package net.luminis.jmx.topthreads;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DiffStackTraces
{
    private List<String> st1;
    private List<String> st2;

    public DiffStackTraces(List<String> st1, List<String> st2) {
        this.st1 = st1;
        this.st2 = st2;
    }

    public DiffStackTraces(String[] st1, String[] st2) {
        this.st1 = Arrays.asList(st1);
        this.st2 = Arrays.asList(st2);
    }

    public DiffStackTraces(StackTraceElement[] st1, StackTraceElement[] st2) {
        this.st1 = new ArrayList<String>();
        for (StackTraceElement el: st1) {
            this.st1.add(el.toString());
        }
        this.st2 = new ArrayList<String>();
        for (StackTraceElement el: st2) {
            this.st2.add(el.toString());
        }
    }

    private List matches = new ArrayList();

    public DiffStackTraces diff() {

        boolean tryDown = false;

        for (int index = st2.size() - 1; index >= 0; index--) {
            String line = st2.get(index);

            Integer firstMatch = null;
            Integer lastMatch = null;
            for (int search = st1.size() -1; search >= 0; search--) {
                if (firstMatch == null) {
                    if (st1.get(search).equals(line)) {
                        firstMatch = search;
                        lastMatch = search;
                    }
                }
                else {
                    // st1[firstMatch] equals st2[index]
                    int inspect = index + (search - firstMatch);
                    if (inspect >= 0 && st1.get(search).equals(st2.get(inspect))) {
                        lastMatch = search;
                    }
                    else {
                        break;
                    }
                }
            }

            int growDown = 0;
            // A second match starts where that last one failed, otherwise we get many overlapping matches, e.g.
            // 'baaab' and 'baab' matches 'aab', 'aa' (twice) and 'a' (six times). Of course, we could remove such
            // overlapping matches, but...
            // This solution is easier and cheaper:
            // - if the next match is adjacent to the previous
            // - try whether this match can be extended ("grow down", because we work bottom up)
            if (tryDown && firstMatch != null) {
                int start = firstMatch;
                while (start + 1 + growDown < st1.size() && index + 1 + growDown < st2.size()) {
                    if (st1.get(start + 1 + growDown).equals(st2.get(index + 1 + growDown))) {
                        firstMatch = start + 1 + growDown;
                        growDown++;
                    }
                    else
                        break;
                }
                if (growDown > 0) {
                    //System.out.println("match grown down with " + (growDown-1));
                }
            }

            if (firstMatch != null) {
                int size = firstMatch - lastMatch + 1;
                if (size > 1) {
                    matches.add(new Object[] { new int[] { lastMatch, firstMatch }, new int[] { index + growDown - size + 1, index + growDown } });
                    //System.out.println("got a match from " + lastMatch + ":" + firstMatch + "->" + (index + growDown - size + 1) + ":" + (index + growDown) + ", size=" + size);
                }
                else {
                    //System.out.println("neglecting single line match from " + lastMatch + ":" + firstMatch + "->" + (index - size + 1) + ":" + index + ", size=" + size);
                }

                // Continue search from first non-matching line
                index = index - size + 1;    // Add one, because the for loop decrements before entering the next loop

                // Because the previous line makes us skip overlapping matches, we need to try to "grow down" the next match
                tryDown = true;
            }
        }

        return this;
    }

    public int getNrOfMatches() {
        return matches.size();
    }

    public Object[] getMatch(int index) {
        return (Object[]) matches.get(index);
    }

    public int[] getMatchFrom(int index) {
        return (int[]) ((Object[]) matches.get(index))[0];
    }

    public int[] getMatchTo(int index) {
        return (int[]) ((Object[]) matches.get(index))[1];
    }

    public Object[] largestMatch() {

        int max = 0;
        Object[] largest = null;

        for (Object m: matches) {
            int[] range = (int[]) ((Object[]) m)[0];
            int size = range[1] - range[0];
            if (size > max) {
                max = size;
                largest = (Object[]) m;
            }
        }
        return (Object[]) largest;
    }

    public void visualize() throws BadLocationException {
        JFrame frame = new JFrame();
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
        JTextPane left = new JTextPane();
        JTextPane right = new JTextPane();
        contentPane.add(left);
        contentPane.add(right);
        Object[] match = largestMatch();
        int[] rangeLeft = (int[]) match[0];
        int[] range = (int[]) match[1];

        StyledDocument document = new DefaultStyledDocument();
        SimpleAttributeSet colorAttrs = new SimpleAttributeSet();
        colorAttrs.addAttribute(StyleConstants.Background, Color.lightGray);

        SimpleAttributeSet italics = new SimpleAttributeSet();
        italics.addAttribute(StyleConstants.Italic, true);

        SimpleAttributeSet fontColor = new SimpleAttributeSet();
        fontColor.addAttribute(StyleConstants.Foreground, Color.red);

        AttributeSet defaultAttrs = new SimpleAttributeSet();

        int index = 0;
        for (String s: st1) {
            if (index >= rangeLeft[0] && index <= rangeLeft[1])
                document.insertString(document.getLength(), s + "\n", fontColor);
            else {
                document.insertString(document.getLength(), s + "\n", defaultAttrs);
            }
            index++;
        }
        left.setDocument(document);

        document = new DefaultStyledDocument();
        index = 0;
        for (String s: st2) {
            if (index >= range[0] && index <= range[1])
                document.insertString(document.getLength(), s + "\n", colorAttrs);
            else {
                document.insertString(document.getLength(), s + "\n", defaultAttrs);
            }
            index++;
        }
        right.setDocument(document);

        JScrollPane scroller = new JScrollPane(contentPane);

        frame.getContentPane().add(scroller);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public static void main(String[] args) throws Exception {
        BufferedReader in = new BufferedReader(new FileReader(args[0]));
        String line;
        List<String> st1 = new ArrayList();
        while ((line = in.readLine()) != null) {
            st1.add(line);
        }
        in.close();

        in = new BufferedReader(new FileReader(args[1]));
        List<String> st2 = new ArrayList();
        while ((line = in.readLine()) != null) {
            st2.add(line);
        }
        in.close();

        new DiffStackTraces(st1, st2).diff().visualize();
    }
}