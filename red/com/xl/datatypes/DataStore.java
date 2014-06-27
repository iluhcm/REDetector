package com.xl.datatypes;

import com.xl.datatypes.probes.Probe;
import com.xl.datatypes.probes.ProbeSet;
import com.xl.datatypes.sequence.SequenceRead;
import com.xl.exception.REDException;
import com.xl.utils.Strand;

/**
 * The Class DataStore is a generic representation of a set
 * of data and its associated quantitation values.  The two
 * common representations are DataSet for real data and
 * DataGroup for virtual datasets.
 */
public abstract class DataStore implements Comparable<DataStore> {

    /**
     * The name.
     */
    private String name;

    /**
     * The probe data.
     */
    private float[] probeData = null;

    /**
     * The probe data size.
     */
    private int probeDataSize = 0;

    /**
     * The collection.
     */
    private DataCollection collection = null;

    /**
     * Instantiates a new data store.
     *
     * @param name the name
     */
    public DataStore(String name) {
        this.name = name;
    }

    /**
     * Sets the collection.
     *
     * @param collection the new collection
     */
    public void setCollection(DataCollection collection) {
        this.collection = collection;
    }

    /**
     * Collection.
     *
     * @return the data collection
     */
    public DataCollection collection() {
        return collection;
    }

    /**
     * Gets the reads for probe.
     *
     * @param p the p
     * @return the reads for probe
     */
    public abstract SequenceRead[] getReadsForProbe(Probe p);

    /**
     * Gets the reads for chromosome.
     *
     * @param chr the chromosome
     * @return the reads for chromsome
     */
    public abstract SequenceRead[] getReadsForChromosome(String chr);

    /**
     * Gets the read count for chromosome.
     *
     * @param chr the c
     * @return the read count for chromosome
     */
    public abstract int getReadCountForChromosome(String chr);

    /**
     * Gets the total read count.
     *
     * @return the total read count
     */
    public abstract int getTotalReadCount();

    /**
     * Gets the total read count for a particular strand.
     *
     * @param strand the strand requested (FORWARD,REVERSE or UNKNOWN)
     * @return the total read count for the specified strand
     */
    public abstract int getReadCountForStrand(Strand strand);

    /**
     * Gets the total read length.
     *
     * @return the total read length
     */
    public abstract long getTotalReadLength();

    /**
     * Gets the length of the longest read.
     *
     * @return the longest read length
     */
    public abstract int getMaxReadLength();

    /**
     * Gets the length of the shortest read.
     *
     * @return the shortest read length
     */
    public abstract int getMinReadLength();

    /**
     * Name.
     *
     * @return the string
     */
    public String name() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Checks if is quantitated.
     *
     * @return true, if is quantitated
     */
    public boolean isQuantitated() {
        return probeData != null;
    }

    /**
     * Reset all probe values.
     */
    public void resetAllProbeValues() {
        probeData = null;
    }

    /**
     * Sets the value for probe.
     *
     * @param p the p
     * @param f the f
     */
    public void setValueForProbe(Probe p, float f) {
        if (probeData == null) {
            probeData = new float[probeDataSize];
        }
        probeData[p.getIndex()] = f;
    }

    /**
     * Checks for value for probe.
     *
     * @param p the p
     * @return true, if successful
     */
    public boolean hasValueForProbe(Probe p) {
        if (probeData != null && probeData.length > p.getIndex()) {
            return true;
        }
        return false;
    }

    /**
     * Gets the value for probe.
     *
     * @param p the p
     * @return the value for probe
     * @throws REDException the red exception
     */
    public float getValueForProbe(Probe p) throws REDException {
        if (probeData == null) {
            throw new REDException("No quantitation for probe " + p + " in " + name);
        }
        if (p.getIndex() >= probeData.length) {
            throw new REDException("Probe data index out of range");
        } else {
            return probeData[p.getIndex()];
        }
    }

    /**
     * Probe set replaced.
     *
     * @param probes the probes
     */
    public void probeSetReplaced(ProbeSet probes) {
        probeData = null;
        if (probes != null) {
            probeDataSize = probes.size();
        } else {
            probeDataSize = 0;
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return name();
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(DataStore s) {
        // Sort alphabetically
        return name().toLowerCase().compareTo(s.name().toLowerCase());
    }
}
