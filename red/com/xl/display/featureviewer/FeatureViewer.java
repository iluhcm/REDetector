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
package com.xl.display.featureviewer;

import com.xl.datatypes.feature.Feature;
import com.xl.main.RedApplication;
import com.xl.preferences.DisplayPreferences;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * The Class FeatureViewer shows the key/value annotations for a selected feature.
 */
public class FeatureViewer extends JDialog implements MouseListener, KeyListener {
    /**
     * The table.
     */
    private JTable table = null;
    /**
     * The model for a feature.
     */
    private FeatureAttributeTable model = null;
    /**
     * The feature to be shown.
     */
    private Feature feature = null;

    /**
     * Instantiates a new feature viewer.
     *
     * @param feature the feature
     */
    public FeatureViewer(Feature feature) {
        super(RedApplication.getInstance(), "Feature: " + feature.getAliasName());

        this.feature = feature;
        model = new FeatureAttributeTable(feature);
        table = new JTable(model);
        table.setColumnSelectionAllowed(true);
        table.setRowSelectionAllowed(true);
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(350);
        table.addMouseListener(this);
        table.addKeyListener(this);

        setContentPane(new JScrollPane(table));

        setSize(550, 300);
        setLocationRelativeTo(RedApplication.getInstance());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setVisible(true);

    }

    @Override
    public void mouseClicked(MouseEvent me) {
        if (me.getClickCount() == 2) {
            Object value = model.getValueAt(table.getSelectedRow(), 0);
            if ("Transcription".equals(value) || "Coding Region".equals(value) || "Exons".equals(value)) {
                DisplayPreferences.getInstance().setLocation(feature.getChr(), feature.getTxLocation().getStart(), feature.getTxLocation().getEnd());
                dispose();
            }
        }
    }

    public void mouseEntered(MouseEvent arg0) {
    }

    public void mouseExited(MouseEvent arg0) {
    }

    public void mousePressed(MouseEvent arg0) {
    }

    public void mouseReleased(MouseEvent arg0) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            dispose();
        }

    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

}
