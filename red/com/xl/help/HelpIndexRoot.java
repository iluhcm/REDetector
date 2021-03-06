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

package com.xl.help;

import com.xl.exception.RedException;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Vector;

/**
 * The Class HelpIndexRoot is the root of the whole help system, ie the folder in which all of the help documents can be found.
 */
public class HelpIndexRoot extends DefaultMutableTreeNode {

    /**
     * Instantiates a new help index root.
     *
     * @param startingLocation the starting location
     */
    public HelpIndexRoot(File startingLocation) throws RedException {
        super("Help Contents");

        if (!startingLocation.exists() || !startingLocation.isDirectory()) {
            throw new RedException("Couldn't find help file directory at '" + startingLocation.getAbsolutePath() + "'");
        }

        addSubFiles(startingLocation, this);
    }

    /**
     * Adds the sub-files.
     *
     * @param directory the directory
     * @param node      the node
     */
    private void addSubFiles(File directory, DefaultMutableTreeNode node) {
        File[] files = directory.listFiles();
        if (files == null || files.length == 0) {
            return;
        }
        Arrays.sort(files, new FileSorter());

        for (File file : files) {
            if (file.isDirectory() && !file.getName().equals("img")) {
                HelpPage h = new HelpPage(file);
                node.add(h);
                addSubFiles(file, h);
            } else if (file.getName().toLowerCase().endsWith(".html")) {
                HelpPage h = new HelpPage(file);
                node.add(h);
            }
        }
    }

    /**
     * Find pages for term.
     *
     * @param searchTerm the search term
     * @return the help page[]
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public HelpPage[] findPagesForTerm(String searchTerm) throws IOException {
        Vector<HelpPage> hits = new Vector<HelpPage>();

        Enumeration kids = children();
        while (kids.hasMoreElements()) {
            Object node = kids.nextElement();
            if (node instanceof HelpPage) {
                ((HelpPage) node).containsString(searchTerm, hits);
            }
        }
        return hits.toArray(new HelpPage[0]);
    }

    /**
     * The Class FileSorter.
     */
    private class FileSorter implements Comparator<File> {

        @Override
        public int compare(File f1, File f2) {

            // The file names should be proceeded by a series of integers separated by dots (eg 1.2.1).  We therefore split these out to compare the
            // individual sections

            int[] numbers1;
            int[] numbers2;

            try {
                numbers1 = getNumberArray(f1);
                numbers2 = getNumberArray(f2);
            } catch (NumberFormatException nfe) {
                return f1.getName().compareTo(f2.getName());
            }

            int shortest = numbers1.length > numbers2.length ? numbers2.length : numbers1.length;

            for (int i = 0; i < shortest; i++) {
                if (numbers1[i] != numbers2[i]) {
                    return numbers1[i] - numbers2[i];
                }
            }

            // If we get here then the shortest number string wins
            return numbers1.length - numbers2.length;
        }

        /**
         * Gets the number array.
         *
         * @param f the f
         * @return the number array
         * @throws NumberFormatException the number format exception
         */
        private int[] getNumberArray(File f) throws NumberFormatException {
            String[] numberStrings = f.getName().split(" ")[0].split("\\.");
            int[] integers = new int[numberStrings.length];
            for (int i = 0; i < numberStrings.length; i++) {
                integers[i] = Integer.parseInt(numberStrings[i]);
            }

            return integers;
        }

    }


}
