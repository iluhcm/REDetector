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
package com.xl.filter;

import com.dw.dbutils.DatabaseManager;
import com.dw.dbutils.Query;
import com.dw.denovo.SpliceJunctionFilter;
import com.xl.datatypes.DataCollection;
import com.xl.datatypes.DataStore;
import com.xl.datatypes.sites.Site;
import com.xl.datatypes.sites.SiteList;
import com.xl.exception.REDException;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

/**
 * The ValuesFilter filters sites based on their associated values
 * from quantiation.  Each site is filtered independently of all
 * other sites.
 */
public class SpliceJunctionFilterMenu extends AbstractSiteFilter {

    private int sequenceEdge = 2;
    private JTextField edgeField = null;
    private SpliceJunctionFilterOptionPanel optionsPanel = new SpliceJunctionFilterOptionPanel();

    /**
     * Instantiates a new values filter with default values
     *
     * @param collection The dataCollection to filter
     * @throws com.xl.exception.REDException if the dataCollection isn't quantitated.
     */
    public SpliceJunctionFilterMenu(DataCollection collection) throws REDException {
        super(collection);
    }

    @Override
    public String description() {
        return "Filter editing bases by splice-junction.";
    }

    @Override
    protected void generateSiteList() {
        progressUpdated("Filtering RNA-editing sites by splice-junction, please wait...", 0, 0);
        SpliceJunctionFilter cf = new SpliceJunctionFilter(databaseManager);
        cf.establishSpliceJunctionResultTable(DatabaseManager.SPLICE_JUNCTION_FILTER_RESULT_TABLE_NAME);
        cf.executeSpliceJunctionFilter(DatabaseManager.SPLICE_JUNCTION_FILTER_TABLE_NAME, DatabaseManager.SPLICE_JUNCTION_FILTER_RESULT_TABLE_NAME,
                parentTable, sequenceEdge);
        DatabaseManager.getInstance().distinctTable(DatabaseManager.SPLICE_JUNCTION_FILTER_RESULT_TABLE_NAME);
        Vector<Site> sites = Query.queryAllEditingSites(DatabaseManager.SPLICE_JUNCTION_FILTER_RESULT_TABLE_NAME);
        SiteList newList = new SiteList(parentList, DatabaseManager.SPLICE_JUNCTION_FILTER_RESULT_TABLE_NAME, description(),
                DatabaseManager.SPLICE_JUNCTION_FILTER_RESULT_TABLE_NAME);
        int index = 0;
        int sitesLength = sites.size();
        for (Site site : sites) {
            progressUpdated(index++, sitesLength);
            if (cancel) {
                cancel = false;
                progressCancelled();
                return;
            }
            newList.addSite(site);
        }
        filterFinished(newList);
    }

    @Override
    public JPanel getOptionsPanel() {
        return optionsPanel;
    }


    @Override
    public boolean hasOptionsPanel() {
        return true;
    }

    @Override
    public boolean isReady() {
        return stores.length != 0 && edgeField.getText().length() != 0;
    }

    @Override
    public String name() {
        return "Splice-junction Filter";
    }

    @Override
    protected String listName() {
        return "Splice-juncion length:" + sequenceEdge;
    }

    /**
     * The ValuesFilterOptionPanel.
     */
    private class SpliceJunctionFilterOptionPanel extends AbstractOptionPanel implements KeyListener {


        /**
         * Instantiates a new values filter option panel.
         */
        public SpliceJunctionFilterOptionPanel() {
            super(collection);
        }

        public void valueChanged(ListSelectionEvent lse) {
            System.out.println(SpliceJunctionFilterMenu.class.getName() + ":valueChanged()");
            Object[] objects = dataList.getSelectedValues();
            stores = new DataStore[objects.length];
            for (int i = 0; i < stores.length; i++) {
                stores[i] = (DataStore) objects[i];
            }
            optionsChanged();
        }

        @Override
        public void keyTyped(KeyEvent e) {
            int keyChar = e.getKeyChar();
            if (!(keyChar >= KeyEvent.VK_0 && keyChar <= KeyEvent.VK_9)) {
                e.consume();
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {

        }

        @Override
        public void keyReleased(KeyEvent e) {
            JTextField f = (JTextField) e.getSource();
            if (f.getText().length() == 0) {
                edgeField.setText("");
            } else if (f == edgeField) {
                sequenceEdge = Integer.parseInt(edgeField.getText());
            }
            optionsChanged();
        }

        @Override
        protected boolean hasChoicePanel() {
            return true;
        }

        @Override
        protected JPanel getChoicePanel() {
            JPanel choicePanel = new JPanel();
            choicePanel.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();

            c.gridy = 0;
            c.gridx = 0;
            c.weightx = 0.5;
            c.weighty = 0.5;
            c.fill = GridBagConstraints.HORIZONTAL;
            choicePanel.add(new JLabel("Splice-junction length: "), c);
            c.gridx = 1;
            c.weightx = 0.1;
            edgeField = new JTextField(3);
            edgeField.setText(sequenceEdge + "");
            edgeField.addKeyListener(this);
            choicePanel.add(edgeField, c);
            return choicePanel;
        }

        @Override
        protected String getPanelDescription() {
            return "Variants that are within +/-k bp (e.g., k = 2) of the splice junction, which are supposed to be unreliable , are excluded;";
        }
    }
}
