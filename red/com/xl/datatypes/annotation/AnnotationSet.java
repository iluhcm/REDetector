package com.xl.datatypes.annotation;

/**
 * Copyright 2010-13 Simon Andrews
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

import com.xl.datatypes.genome.Genome;
import com.xl.dialog.CrashReporter;
import com.xl.display.featureviewer.Feature;
import com.xl.main.REDApplication;
import com.xl.preferences.REDPreferences;

import java.io.*;
import java.util.*;

/**
 * AnnotationSet represents a set of genome annotations deriving from a single
 * source (file URL etc). They are combined in an AnnotationCollection to
 * provide the full set of genome annotations used by the program.
 */
public class AnnotationSet {

    protected Genome genome;
    private String name;
    private AnnotationCollection collection = null;

    private boolean finalised = false;
    /*
     * We store features by chromosome. Within each chromosome we store by
	 * feature type for quick access and then within that we store a vector of
	 * features.
	 */

    protected FeatureSet featureSet = null;

    /**
     * Instantiates a new annotation set.
     *
     * @param genome The base genome which this annotation applies to
     * @param name   The name for the set (file name, URL etc)
     */
    public AnnotationSet(Genome genome, String name) {
        this.genome = genome;
        this.name = name;
        this.featureSet = new FeatureSet();
    }

    /**
     * This method should only be called by an AnnotationCollection to which
     * this annotation set is being added. An annotation set can only be added
     * to one collection. In addition to creating a link between the set and
     * collection this also triggers the caching of data in this set and
     * consequently no more annotation can be added to this set once this has
     * been called.
     *
     * @param collection The AnnotationCollection to which this set has been added
     */
    protected void setCollection(AnnotationCollection collection) {
        if (this.collection != null) {
            throw new IllegalArgumentException(
                    "This annotation set is already part of a collection");
        }
        this.collection = collection;

        finalise();
    }

    /**
     * Provides an enumeration to iterate through all chromosomes which have
     * data in this set. This is because we don't want to have to return all
     * features in this set at the same time due to memory constraints.
     * Therefore using this iterator is the recommended way to get hold of all
     * features in the set.
     *
     * @return An enumeration of all chromosome names which have data in this
     * set.
     */
    public Enumeration<String> getChromosomeNames() {
        return featureSet.getChromosomeNames();
    }

    public Feature[] getFeaturesForChr(String chr) {
        return featureSet.getFeatureTypeCollection(chr).getFeatures();
    }

    /**
     * Gets all features for a given type on a given chromosome.
     * <p/>
     * Features returned by this method are not guaranteed to be sorted.
     *
     * @param chr  The chromosome name
     * @param name The name of feature
     * @return A list of features of this type
     */
    public Feature getFeaturesForName(String chr, String name) {
        return featureSet.getFeaturesForName(chr, name);
    }

    /**
     * Gets all features for a given type.
     * <p/>
     * Features returned by this method are not guaranteed to be sorted.
     *
     * @param name The name of feature
     * @return A list of features of this type
     */
    public Feature getFeaturesForName(String name) {
        return featureSet.getFeaturesForName(name);
    }

    public Feature getFeatrueForLocation(int location) {
        return featureSet.getFeaturesForLocation(location);
    }

    public Feature deleteFeatureForName(String chr, String name) {
        return featureSet.deleteFeatureForName(chr, name);
    }

    public boolean deleteFeature(String chr, Feature feature) {
        return featureSet.deleteFeature(chr, feature);
    }

    public Feature[] getAllFeatures() {

        // TODO: Find a way to not load all features into memory just to do
        // this.

        Vector<Feature> allFeatures = new Vector<Feature>();

        Enumeration<String> chrs = featureSet.getChromosomeNames();

        while (chrs.hasMoreElements()) {
            String c = chrs.nextElement();
            Feature[] features = featureSet
                    .getFeatureTypeCollection(c).getFeatures();
            for (Feature f : features) {
                allFeatures.add(f);
            }
        }
        return allFeatures.toArray(new Feature[0]);
    }

    public synchronized void finalise() {

        // This is called when we're added to a collection and lets us optimise
        // storage and cache off unused data. It should only be called once
        // and we prevent the adding of more features once it's been called.

        if (finalised)
            return;
        finalised = true;
        featureSet.finalise();
    }

    /**
     * This is used to clean up the set when it is being removed from its
     * containing collection. It will sever the links between this set and the
     * annotatoin collection as well as blanking its internal data structures.
     * This will also trigger the notification of any listeners that this set
     * has been removed.
     */
    public void delete() {
        if (collection != null) {
            collection.removeAnnotationSet(this);
            collection = null;
        }
        if (featureSet != null) {
            featureSet = null;
        }
    }

    /**
     * The name of this annotation set
     *
     * @return The annotation set name
     */
    public String name() {
        return name;
    }

    /**
     * Sets the annotation set name.
     *
     * @param name The new name for this set.
     */
    public void setName(String name) {
        this.name = name;
        // Inform the collection so we can tell any listeners
        if (collection != null) {
            collection.annotationSetRenamed(this);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return name();
    }

    /**
     * Genome
     *
     * @return The genome which underlies this annotation set
     */
    public Genome genome() {
        return genome;
    }

    /**
     * Adds a feature. Note that this operation can fail if the data in this set
     * has been cached to disk. This happens when the set is first queried or
     * when it is added to an annotation collection. In these cases an
     * IllegalArgumentException will be thrown.
     *
     * @param f The feature to add.
     */
    public void addFeature(Feature f) {
        featureSet.addFeature(f);
    }

    /**
     * FeatureSet represents the different sets of feature types and
     * chromosomes.
     */
    protected class FeatureSet {

        private Hashtable<String, FeatureTypeCollection> chrFeatures = new Hashtable<String, FeatureTypeCollection>();

        public FeatureSet() {
            String[] chromosomeNames = genome.getAllChromosomeNames();
            if (chromosomeNames != null) {
                for (String chrosmomeName : chromosomeNames) {
                    FeatureTypeCollection t = new FeatureTypeCollection(chrosmomeName);
                    chrFeatures.put(chrosmomeName, t);
                }
            }
        }

        /**
         * Adds a feature.
         *
         * @param f The feature to add
         */
        public void addFeature(Feature f) {
            if (chrFeatures.containsKey(f.getChr())) {
                chrFeatures.get(f.getChr()).addFeature(f);
            } else {
                FeatureTypeCollection t = new FeatureTypeCollection(f.getChr());
                t.addFeature(f);
                chrFeatures.put(f.getChr(), t);
            }
        }

        /**
         * Chromosome names.
         *
         * @return An enumeration of all chromosome names for which we hold data
         */
        public Enumeration<String> getChromosomeNames() {
            return chrFeatures.keys();
        }

        public Feature deleteFeatureForName(String chr, String name) {
            if (chrFeatures.containsKey(chr)) {
                return chrFeatures.get(chr).deleteFeatureForName(name);
            } else {
                return null;
            }
        }

        public boolean deleteFeature(String chr, Feature feature) {
            if (chrFeatures.contains(chr)) {
                return chrFeatures.get(chr).deleteFeature(feature);
            } else {
                return false;
            }
        }

        /**
         * Gets a list of features.
         *
         * @param chr  The chromosome name
         * @param name The feature type
         * @return A list of features
         */
        public Feature getFeaturesForName(String chr, String name) {
            if (chrFeatures.containsKey(chr)) {
                return chrFeatures.get(chr).getFeatureForName(name);
            } else {
                return null;
            }
        }

        /**
         * Gets a list of features.
         *
         * @param name The feature type
         * @return A list of features
         */
        public Feature getFeaturesForName(String name) {
            Enumeration<String> e = chrFeatures.keys();
            Feature feature = null;
            while (e.hasMoreElements()) {
                feature = chrFeatures.get(e.nextElement()).getFeatureForName(
                        name);
                if (feature != null) {
                    return feature;
                }
            }
            return feature;
        }

        public Feature getFeaturesForLocation(int location) {
            Enumeration<String> e = chrFeatures.keys();
            Feature feature = null;
            while (e.hasMoreElements()) {
                feature = chrFeatures.get(e.nextElement())
                        .getFeatureForLocation(location);
                if (feature != null) {
                    return feature;
                }
            }
            return feature;
        }

        protected FeatureTypeCollection getFeatureTypeCollection(String chr) {
            if (!chrFeatures.containsKey(chr)) {
                chrFeatures.put(chr, new FeatureTypeCollection(chr));
            }
            return chrFeatures.get(chr);
        }

        protected void addPreCacheFeatureTypeCollection(String chr, File file) {
            if (!chrFeatures.containsKey(chr)) {
                chrFeatures.put(chr, new FeatureTypeCollection(chr, file));
            }
        }

        /**
         * Optimises and caches all held data.
         */
        public void finalise() {
            // We're not optimising anything at this level, but we do need to
            // tell the FeatureTypeFeatureSets about this.

            Iterator<FeatureTypeCollection> i = chrFeatures.values()
                    .iterator();
            while (i.hasNext()) {
                i.next().finalise();
            }
        }

    }

    /**
     * FeatureTypeCollection represents a list of features on
     * the same chromosome. It also provides the caching mechanism for feature
     * data.
     */
    protected class FeatureTypeCollection implements Runnable {

        private LinkedList<Feature> buildFeatures = new LinkedList<Feature>();
        private Feature[] featureList = null;
        private File cacheFile = null;
        private String chr = null;

        public FeatureTypeCollection(String chr) {
            this.chr = chr;
        }

        /**
         * This constructor is a shortcut where there is a pre-cached file which
         * we can reuse. Since this is somewhat fragile it should be used with
         * caution.
         *
         * @param cacheFile
         */
        public FeatureTypeCollection(String chr, File cacheFile) {
            this.chr = chr;
            this.cacheFile = cacheFile;
            buildFeatures = null;
        }

        /**
         * Adds a feature.
         *
         * @param f The feature to add
         */
        public void addFeature(Feature f) {
            if (buildFeatures == null) {
                throw new IllegalArgumentException(
                        "Can't add data to a finalsed type collection");
            }
            buildFeatures.add(f);
        }

        public boolean deleteFeature(Feature feature) {
            if (buildFeatures != null && buildFeatures.size() != 0 && buildFeatures.contains(feature)) {
                return buildFeatures.remove(feature);
            } else {
                return false;
            }
        }

        public Feature deleteFeatureForName(String name) {
            if (buildFeatures != null && buildFeatures.size() != 0) {
                int length = buildFeatures.size();
                for (int i = 0; i < length; i++) {
                    if (buildFeatures.get(i).getAliasName().equals(name) || buildFeatures.get(i).getName().equals(name)) {
                        return buildFeatures.remove(i);
                    }
                }
            }
            return null;
        }

        public Feature getFeatureForName(String name) {
            if (buildFeatures != null && buildFeatures.size() != 0) {
                int length = buildFeatures.size();
                for (int i = 0; i < length; i++) {
                    if (buildFeatures.get(i).getAliasName().equals(name) || buildFeatures.get(i).getName().equals(name)) {
                        return buildFeatures.get(i);
                    }
                }
            }
            return null;
        }

        public Feature getFeatureForLocation(int location) {
            if (buildFeatures != null && buildFeatures.size() != 0) {
                int length = buildFeatures.size();
                for (int i = 0; i < length; i++) {
                    if (buildFeatures.get(i).getTxLocation().getStart() >= location
                            && buildFeatures.get(i).getTxLocation().getEnd() <= location) {
                        return buildFeatures.get(i);
                    }
                }
            }
            return null;
        }

        /**
         * Optimises and caches feature data.
         */
        public void finalise() {
            if (buildFeatures == null || cacheFile != null) {
                System.err.println("This already appears to be finalised");
                return;
            }
            featureList = buildFeatures.toArray(new Feature[0]);
            buildFeatures.clear();
            buildFeatures = null;

            // If this is a core annotation set then we want to maintain a set
            // of cache files for future use so we write them where we can get
            // back to them in future. We also don't optimise for size since we
            // need to keep them all.
            if (AnnotationSet.this instanceof CoreAnnotationSet) {
                try {
                    File cacheBase = new File(REDPreferences.getInstance()
                            .getGenomeBase()
                            + "/"
                            + genome.getDisplayName() + "/cache");
                    if (!cacheBase.exists()) {
                        if (!cacheBase.mkdir()) {
                            throw new IOException(
                                    "Can't create cache file for core annotation set");
                        }
                    }

                    cacheFile = new File(cacheBase.getAbsoluteFile() + "/"
                            + chr + ".cache");
                    ObjectOutputStream oos = new ObjectOutputStream(
                            new BufferedOutputStream(new FileOutputStream(
                                    cacheFile)));
                    oos.writeObject(featureList);
                    oos.close();
                    featureList = null;

                } catch (IOException ioe) {
                    new CrashReporter(ioe);
                }
            }

            // If this isn't core annotation then we cache this to the normal
            // cache directory and set a shutdown hook to delete it at the end
            // of the session.
            else if (featureList.length > 500) {
                try {
                    cacheFile = File.createTempFile("red_anotation", ".temp",
                            REDPreferences.getInstance().tempDirectory());
                    ObjectOutputStream oos = new ObjectOutputStream(
                            new BufferedOutputStream(new FileOutputStream(
                                    cacheFile)));
                    oos.writeObject(featureList);
                    oos.close();
                    featureList = null;
                    Runtime.getRuntime().addShutdownHook(new Thread(this));
                } catch (IOException ioe) {
                    new CrashReporter(ioe);
                }
            }
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Runnable#run()
         */
        public void run() {
            // Clean up any temp files.
            if (cacheFile != null) {
                cacheFile.delete();
            }
        }

        /**
         * Gets a list of features.
         *
         * @return The list of stored features.
         */
        public Feature[] getFeatures() {
            if (buildFeatures != null) {
                finalise();
            }
            if (cacheFile != null) {
                REDApplication.getInstance().cacheUsed();
                try {
                    ObjectInputStream ois = new ObjectInputStream(
                            new BufferedInputStream(new FileInputStream(
                                    cacheFile)));
                    Feature[] returnedFeatures = (Feature[]) ois
                            .readObject();
                    ois.close();
                    return returnedFeatures;
                } catch (Exception e) {
                    new CrashReporter(e);
                }
            }
            return featureList;
        }
    }
}
