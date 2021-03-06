/*
 * RED: RNA Editing Detector
 *     Copyright (C) <2014>  <Xing Li>
 *
 *     RED is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     RED is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.xl.display.panel;

import com.xl.main.Global;
import com.xl.utils.ColourScheme;
import com.xl.utils.FontManager;
import com.xl.utils.ui.IconLoader;

import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * The Class RedTitlePanel provides a small panel which gives details of the RED version and copyright. Used in both the welcome panel and the about dialog.
 */
public class RedTitlePanel extends JPanel {

    public RedTitlePanel() {
        setLayout(new BorderLayout(5, 1));

        add(new JLabel("", IconLoader.LOGO_1, JLabel.CENTER), BorderLayout.WEST);
        add(new JLabel("", IconLoader.LOGO_2, JLabel.CENTER), BorderLayout.EAST);
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new GridBagLayout());

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 1;
        gridBagConstraints.insets = new Insets(6, 6, 0, 0);
        gridBagConstraints.fill = GridBagConstraints.NONE;

        JLabel program = new SmoothJLabel("RED: A Java-based RNA Editing Sites Identification and Visualization Program", JLabel.CENTER);
        program.setFont(new Font("Dialog", Font.BOLD, 18));
        program.setForeground(ColourScheme.PROGRAM_NAME);
        jPanel.add(program, gridBagConstraints);

        gridBagConstraints.gridy++;
        JLabel version = new SmoothJLabel("Version: " + Global.VERSION, JLabel.CENTER);
        version.setFont(new Font("Dialog", Font.BOLD, 15));
        version.setForeground(ColourScheme.PROGRAM_VERSION);
        jPanel.add(version, gridBagConstraints);

        gridBagConstraints.gridy++;

        JTextPane webPane = new JTextPane();
        webPane.setText(Global.HOME_PAGE);
        webPane.setEditable(false);
        StyledDocument doc = (StyledDocument) webPane.getDocument();
        Style s = new StyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setUnderline(s, true);
        doc.setCharacterAttributes(0, webPane.getText().length(), s, false);
        webPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        webPane.setBackground(getBackground());
        webPane.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                URI uri = null;
                try {
                    uri = new URI(Global.HOME_PAGE);
                } catch (URISyntaxException e1) {
                    e1.printStackTrace();
                }
                try {
                    Desktop.getDesktop().browse(uri);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
        jPanel.add(webPane, gridBagConstraints);
        gridBagConstraints.gridy++;

        JLabel buptCopyright = new JLabel("\u00a9Xing Li, Di Wu, Yongmei Sun, Beijing University of Posts and Telecommunications",
                JLabel.CENTER);
        buptCopyright.setFont(FontManager.COPYRIGHT_FONT);
        jPanel.add(buptCopyright, gridBagConstraints);
        gridBagConstraints.gridy++;

        JLabel cqmuCopyright = new JLabel(
                "\u00a9Qi Pan, Keyue Ding, Key Laboratory of Molecular Biology for Infectious Diseases, Ministry of Education of China", JLabel.CENTER);
        cqmuCopyright.setFont(FontManager.COPYRIGHT_FONT);
        jPanel.add(cqmuCopyright, gridBagConstraints);
        gridBagConstraints.gridy++;

        JLabel cqmuCopyright2 = new JLabel("The Second Affiliated Hospital of Chongqing Medical University, Chongqing, P. R. China ", JLabel.CENTER);
        cqmuCopyright2.setFont(FontManager.COPYRIGHT_FONT);
        jPanel.add(cqmuCopyright2, gridBagConstraints);
        gridBagConstraints.gridy++;

        JLabel copyright = new JLabel("Distributed under the GNU General Public License, Version 3", JLabel.CENTER);
        copyright.setFont(FontManager.COPYRIGHT_FONT);
        copyright.setForeground(ColourScheme.PROGRAM_VERSION);
        jPanel.add(copyright, gridBagConstraints);


        gridBagConstraints.gridy++;
        add(jPanel, BorderLayout.CENTER);
    }

    /**
     * A JLabel with anti-aliasing enabled. Takes the same constructor arguments as JLabel
     */
    private class SmoothJLabel extends JLabel {

        /**
         * Creates a new label
         *
         * @param text     The text
         * @param position The JLabel constant position for alignment
         */
        public SmoothJLabel(String text, int position) {
            super(text, position);
        }

        /*
         * (non-Javadoc)
         *
         * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
         */
        public void paintComponent(Graphics g) {
            if (g instanceof Graphics2D) {
                ((Graphics2D) g).setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
            }
            super.paintComponent(g);
        }

    }

}
