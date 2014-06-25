package com.xl.display.dataviewer;

/**
 * Copyright Copyright 2007-13 Simon Andrews
 *
 *    This file is part of SeqMonk.
 *
 *    SeqMonk is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    SeqMonk is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with SeqMonk; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

import com.xl.datatypes.DataGroup;
import com.xl.datatypes.DataSet;
import com.xl.datatypes.ReplicateSet;
import com.xl.datatypes.annotation.AnnotationSet;
import com.xl.datatypes.probes.ProbeList;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * The Class DataTreeRenderer sets the look of the DataTree
 */
public class DataTreeRenderer extends DefaultTreeCellRenderer {

    /**
     * The data set icon.
     */
    private static Icon dataSetIcon = new ImageIcon(
            ClassLoader
                    .getSystemResource("resources/dataset_icon.png"));

    /**
     * The data group icon.
     */
    private static Icon dataGroupIcon = new ImageIcon(
            ClassLoader
                    .getSystemResource("resources/datagroup_icon.png"));

    /**
     * The probe list icon.
     */
    private static Icon probeListIcon = new ImageIcon(
            ClassLoader
                    .getSystemResource("resources/probelist_icon.png"));

    /**
     * The annotation set icon.
     */
    private static Icon annotationSetIcon = new ImageIcon(
            ClassLoader
                    .getSystemResource("resources/annotation_set_icon.png"));

    /**
     * The replicate set icon.
     */
    private static Icon replicateSetIcon = new ImageIcon(
            ClassLoader
                    .getSystemResource("resources/replicate_set_icon.png"));

    /*
     * (non-Javadoc)
     *
     * @see
     * javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent
     * (javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int,
     * boolean)
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean selected, boolean expanded, boolean leaf, int row,
                                                  boolean hasFocus) {

        if (value instanceof DataSet) {
            JLabel label = new JLabel(value.toString(), dataSetIcon,
                    JLabel.LEFT);

            if (selected) {
                label.setOpaque(true);
                label.setBackground(Color.LIGHT_GRAY);
            }
            return label;
        } else if (value instanceof DataGroup) {
            JLabel label = new JLabel(value.toString(), dataGroupIcon,
                    JLabel.LEFT);

            if (selected) {
                label.setOpaque(true);
                label.setBackground(Color.LIGHT_GRAY);
            }
            return label;
        } else if (value instanceof ReplicateSet) {
            JLabel label = new JLabel(value.toString(), replicateSetIcon,
                    JLabel.LEFT);

            if (selected) {
                label.setOpaque(true);
                label.setBackground(Color.LIGHT_GRAY);
            }
            return label;
        } else if (value instanceof ProbeList) {
            JLabel label = new JLabel(value.toString(), probeListIcon,
                    JLabel.LEFT);
            if (selected) {
                label.setOpaque(true);
                label.setBackground(Color.LIGHT_GRAY);
            }
            return label;
        } else if (value instanceof AnnotationSet) {
            JLabel label = new JLabel(value.toString(), annotationSetIcon,
                    JLabel.LEFT);
            if (selected) {
                label.setOpaque(true);
                label.setBackground(Color.LIGHT_GRAY);
            }
            return label;
        } else {
            return super.getTreeCellRendererComponent(tree, value, selected,
                    expanded, leaf, row, hasFocus);
        }
    }

}
