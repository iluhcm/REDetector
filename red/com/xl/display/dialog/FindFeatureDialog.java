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

package com.xl.display.dialog;

import com.xl.datatypes.DataCollection;
import com.xl.datatypes.annotation.AnnotationCollection;
import com.xl.datatypes.annotation.AnnotationSet;
import com.xl.datatypes.feature.Feature;
import com.xl.display.featureviewer.FeatureListViewer;
import com.xl.exception.RedException;
import com.xl.interfaces.Cancellable;
import com.xl.main.RedApplication;
import com.xl.utils.ui.OptionDialogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * The Class FindFeatureDialog shows a dialog which the user can use to search for any kind of annotation.
 */
public class FindFeatureDialog extends JDialog implements ActionListener, Runnable, Cancellable {
    private final Logger logger = LoggerFactory.getLogger(FindFeatureDialog.class);
    /**
     * The data collection containing the annotation we want to search.
     */
    private DataCollection dataCollection;
    /**
     * The annotation collection
     */
    private AnnotationCollection collection;
    /**
     * The search.
     */
    private JTextField search;
    /**
     * The search in.
     */
    private JComboBox searchIn;
    /**
     * The button to save the current hits
     */
    private JButton saveAllHitsAsTrackButton;
    /**
     * The button to save the current hits
     */
    private JButton saveSelectedHitsAsTrackButton;
    /**
     * The last list of hits found
     */
    private Feature[] lastHits = new Feature[0];
    /**
     * The viewer.
     */
    private FeatureListViewer viewer = null;
    /**
     * The scroll.
     */
    private JScrollPane scroll = null;
    /**
     * The progress dialog.
     */
    private ProgressDialog spd = null;
    /**
     * Cancel the search.
     */
    private boolean cancelSearch = false;

    /**
     * Instantiates a new find feature dialog.
     *
     * @param dataCollection the data collection
     */
    public FindFeatureDialog(DataCollection dataCollection) {
        super(RedApplication.getInstance(), "Find Feature...");
        setSize(700, 350);
        setLocationRelativeTo(RedApplication.getInstance());

        this.dataCollection = dataCollection;
        this.collection = dataCollection.genome().getAnnotationCollection();

        getContentPane().setLayout(new BorderLayout());

        JPanel choicePanel = new JPanel();
        choicePanel.add(new JLabel("Search for "));
        search = new JTextField(15);
        choicePanel.add(search);
        choicePanel.add(new JLabel(" in "));
        searchIn = new JComboBox(new String[]{"name", "location"});
        choicePanel.add(searchIn);
        getContentPane().add(choicePanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        JButton cancelButton = new JButton("Close");
        cancelButton.setActionCommand("close");
        cancelButton.addActionListener(this);
        buttonPanel.add(cancelButton);

        saveAllHitsAsTrackButton = new JButton("Save All As Annotation Track");
        saveAllHitsAsTrackButton.setActionCommand("save_annotation_all");
        saveAllHitsAsTrackButton.addActionListener(this);
        saveAllHitsAsTrackButton.setEnabled(false);
        buttonPanel.add(saveAllHitsAsTrackButton);

        saveSelectedHitsAsTrackButton = new JButton("Save Selected As Annotation Track");
        saveSelectedHitsAsTrackButton.setActionCommand("save_annotation_selected");
        saveSelectedHitsAsTrackButton.addActionListener(this);
        saveSelectedHitsAsTrackButton.setEnabled(false);
        buttonPanel.add(saveSelectedHitsAsTrackButton);

        JButton searchButton = new JButton("Search");
        searchButton.setActionCommand("search");
        searchButton.addActionListener(this);
        getRootPane().setDefaultButton(searchButton);
        buttonPanel.add(searchButton);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    public void run() {
        spd = new ProgressDialog("Searching...", this);
        spd.progressUpdated("Starting Search...", 0, 1);
        makeFeatureList();
    }

    public void cancel() {
        cancelSearch = true;
    }

    /**
     * Make feature list.
     */
    private void makeFeatureList() {

        if (scroll != null) {
            remove(scroll);
            validate();
        }
        Vector<Feature> hits = new Vector<Feature>();
        String query = search.getText().toLowerCase().trim();
        Feature[] feature = null;
        if (searchIn.getSelectedItem().equals("name")) {
            feature = collection.getFeaturesForName(query);
        } else if (searchIn.getSelectedItem().equals("location")) {
            if (query.matches("^[0-9]+$")) {
                feature = collection.getFeatureForLocation(Integer.parseInt(query));
            }
        } else {
            throw new IllegalArgumentException("Wrong item '" + searchIn.getSelectedItem() + "'");
        }
        if (feature == null) {
            spd.progressExceptionReceived(new NullPointerException("No features found!"));
            return;
        }
        for (int k = 0; k < feature.length; k++) {
            if (cancelSearch) {
                spd.progressCancelled();
                cancelSearch = false;
                return;
            }
            spd.progressUpdated("Searching...", k, feature.length);
            hits.add(feature[k]);
        }
        lastHits = hits.toArray(new Feature[0]);
        setTitle("Find features [" + lastHits.length + " hits]");
        saveAllHitsAsTrackButton.setEnabled(lastHits.length > 0);
        saveSelectedHitsAsTrackButton.setEnabled(lastHits.length > 0);

        spd.progressComplete("search_features", lastHits);

        if (hits.size() > 0) {
            viewer = new FeatureListViewer(lastHits);
            scroll = new JScrollPane(viewer);
            add(scroll, BorderLayout.CENTER);
            validate();
        } else {
            repaint(); // So we aren't left with a corrupted table showing from a previous search
            OptionDialogUtils.showMessageDialog(this, "No hits found", "Search results");
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("close")) {
            setVisible(false);
            dispose();
        } else if (ae.getActionCommand().equals("search")) {
            Thread t = new Thread(this);
            t.start();
        } else if (ae.getActionCommand().equals("save_annotation_all")) {
            // Find a name for the type of feature they want to create
            String name = (String) JOptionPane.showInputDialog(this, "Input an annotation name", "Make Annotation Track", JOptionPane.QUESTION_MESSAGE, null,
                    null, search.getText() + " " + searchIn.getSelectedItem());

            if (name == null)
                return; // They cancelled

            // Now we can go ahead and make the new annotation set
            AnnotationSet searchAnnotations = new AnnotationSet(dataCollection.genome(), search.getText() + " " + searchIn.getSelectedItem());
            for (Feature f : lastHits) {
                searchAnnotations.addFeature(f);
            }
            try {
                dataCollection.genome().getAnnotationCollection().addAnnotationSet(searchAnnotations);
            } catch (RedException e) {
                logger.warn("Unable to add annotation set.", e);
            }

        } else if (ae.getActionCommand().equals("save_annotation_selected")) {
            Feature[] selectedHits = viewer.getSelectedFeatures();
            if (selectedHits.length == 0) {
                OptionDialogUtils.showMessageDialog(this, "There are no selected features from which to make a track", "Can't make track");
                return;
            }

            // Find a name for the type of feature they want to create
            String name = (String) JOptionPane.showInputDialog(this, "Input an annotation name", "Make Annotation Track", JOptionPane.QUESTION_MESSAGE, null,
                    null, "selected " + search.getText());

            if (name == null)
                return; // They cancelled

            // Now we can go ahead and make the new annotation set
            AnnotationSet searchAnnotations = new AnnotationSet(dataCollection.genome(), search.getText() + " search results");
            for (Feature f : selectedHits) {
                searchAnnotations.addFeature(f);
            }
            try {
                dataCollection.genome().getAnnotationCollection().addAnnotationSet(searchAnnotations);
            } catch (RedException e) {
                logger.warn("Unable to add annotation set.", e);
            }
        }
    }
}
