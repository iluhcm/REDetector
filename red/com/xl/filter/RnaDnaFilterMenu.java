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

import com.dw.dnarna.DnaRnaFilter;
import com.dw.publicaffairs.DatabaseManager;
import com.dw.publicaffairs.Query;
import com.xl.datatypes.DataCollection;
import com.xl.datatypes.DataStore;
import com.xl.datatypes.probes.Probe;
import com.xl.datatypes.probes.ProbeList;
import com.xl.dialog.TypeColourRenderer;
import com.xl.exception.REDException;
import com.xl.utils.ListDefaultSelector;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.Vector;

/**
 * The ValuesFilter filters probes based on their associated values
 * from quantiation.  Each probe is filtered independently of all
 * other probes.
 */
public class RnaDnaFilterMenu extends ProbeFilter {

    private DataStore[] stores = new DataStore[0];
    private RnaDnaFilterOptionPanel optionsPanel = new RnaDnaFilterOptionPanel();

    /**
     * Instantiates a new values filter with default values
     *
     * @param collection The dataCollection to filter
     * @throws com.xl.exception.REDException if the dataCollection isn't quantitated.
     */
    public RnaDnaFilterMenu(DataCollection collection) throws REDException {
        super(collection);
    }

    @Override
    public String description() {
        return "Filter editing bases by comparing RNA and DNA.";
    }

    @Override
    protected void generateProbeList() {
        DnaRnaFilter dnaRnaFilter = new DnaRnaFilter(databaseManager);
        dnaRnaFilter.establishDnaRnaTable(DatabaseManager.DNA_RNA_FILTER_RESULT_TABLE_NAME);
        dnaRnaFilter.executeDnaRnaFilter(DatabaseManager.DNA_RNA_FILTER_RESULT_TABLE_NAME, DatabaseManager.DNA_VCF_RESULT_TABLE_NAME,
                parentTable);
        DatabaseManager.getInstance().distinctTable(DatabaseManager.DNA_RNA_FILTER_RESULT_TABLE_NAME);

        Vector<Probe> probes = Query.queryAllEditingSites(DatabaseManager.REPEAT_FILTER_RESULT_TABLE_NAME);
        ProbeList newList = new ProbeList(parentList, DatabaseManager.REPEAT_FILTER_RESULT_TABLE_NAME, "",
                DatabaseManager.REPEAT_FILTER_RESULT_TABLE_NAME);
        int index = 0;
        int probesLength = probes.size();
        for (Probe probe : probes) {
            progressUpdated(index++, probesLength);
            if (cancel) {
                cancel = false;
                progressCancelled();
                return;
            }
            newList.addProbe(probe);
        }
        filterFinished(newList);
    }

    /* (non-Javadoc)
     * @see uk.ac.babraham.SeqMonk.Filters.ProbeFilter#getOptionsPanel()
     */
    @Override
    public JPanel getOptionsPanel() {
        return optionsPanel;
    }

    /* (non-Javadoc)
     * @see uk.ac.babraham.SeqMonk.Filters.ProbeFilter#hasOptionsPanel()
     */
    @Override
    public boolean hasOptionsPanel() {
        return true;
    }

    /* (non-Javadoc)
     * @see uk.ac.babraham.SeqMonk.Filters.ProbeFilter#isReady()
     */
    @Override
    public boolean isReady() {
        return stores.length != 0;
    }

    /* (non-Javadoc)
     * @see uk.ac.babraham.SeqMonk.Filters.ProbeFilter#name()
     */
    @Override
    public String name() {
        return "RNA&DNA Filter";
    }

    /* (non-Javadoc)
     * @see uk.ac.babraham.SeqMonk.Filters.ProbeFilter#listDescription()
     */
    @Override
    protected String listDescription() {
        StringBuilder b = new StringBuilder();

        b.append("Filter on probes in ");
        b.append(collection.probeSet().getActiveList().name() + " ");

        for (int s = 0; s < stores.length; s++) {
            b.append(stores[s].name());
            if (s < stores.length - 1) {
                b.append(" , ");
            }
        }
        return b.toString();
    }

    /* (non-Javadoc)
     * @see uk.ac.babraham.SeqMonk.Filters.ProbeFilter#listName()
     */
    @Override
    protected String listName() {
        return "RNA&DNA filter";
    }


    /**
     * The ValuesFilterOptionPanel.
     */
    private class RnaDnaFilterOptionPanel extends JPanel implements ListSelectionListener {

        private JList<DataStore> dataList;
        private JTextArea description = null;

        /**
         * Instantiates a new values filter option panel.
         */
        public RnaDnaFilterOptionPanel() {
            setLayout(new BorderLayout());
            JPanel dataPanel = new JPanel();
            dataPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            dataPanel.setLayout(new BorderLayout());
            dataPanel.add(new JLabel("Data Sets/Groups", JLabel.CENTER), BorderLayout.NORTH);

            DefaultListModel<DataStore> dataModel = new DefaultListModel<DataStore>();

            DataStore[] stores = collection.getAllDataStores();

            for (DataStore store : stores) {
                dataModel.addElement(store);
            }

            dataList = new JList<DataStore>(dataModel);
            ListDefaultSelector.selectDefaultStores(dataList);
            dataList.setCellRenderer(new TypeColourRenderer());
            dataList.addListSelectionListener(this);
            dataPanel.add(new JScrollPane(dataList), BorderLayout.CENTER);

            add(dataPanel, BorderLayout.WEST);

            JPanel choicePanel = new JPanel();
            choicePanel.setLayout(new GridBagLayout());
            choicePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            description = new JTextArea("RNA-editing means editing in RNA while DNA is not snp.\n" +
                    "So only otherwise, all the difference between DNA and RNA\n" +
                    "will be selected.");
//            description.setLineWrap(true);
            description.setEditable(false);
            choicePanel.add(description);

            valueChanged(null);
            add(new JScrollPane(choicePanel), BorderLayout.CENTER);
        }

        /* (non-Javadoc)
         * @see javax.swing.JComponent#getPreferredSize()
         */
        public Dimension getPreferredSize() {
            return new Dimension(600, 250);
        }

        /* (non-Javadoc)
         * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
         */
        public void valueChanged(ListSelectionEvent lse) {
            System.out.println(RnaDnaFilterMenu.class.getName() + ":valueChanged()");
            java.util.List<DataStore> lists = dataList.getSelectedValuesList();
            stores = new DataStore[lists.size()];
            for (int i = 0; i < stores.length; i++) {
                stores[i] = lists.get(i);
            }
            optionsChanged();
        }

    }
}