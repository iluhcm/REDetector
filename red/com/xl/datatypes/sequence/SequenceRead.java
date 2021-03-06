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

package com.xl.datatypes.sequence;

import com.xl.utils.Strand;
import net.sf.samtools.AlignmentBlock;
import net.sf.samtools.SAMRecord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The Class SequenceRead represents a read parsed from a BAM file.
 */
public class SequenceRead extends Location {
    /**
     * The chromosome name.
     */
    String chr;
    /**
     * The strand.
     */
    Strand strand;
    /**
     * The bases of this read.
     */
    private byte[] readBases = null;
    /**
     * The qualities of the read. If there is no data for qualities, then we fill them with the highest quality(i.e., 127).
     */
    private byte[] qualities = null;

    private List<AlignmentBlock> alignmentBlocks = null;

    public SequenceRead(SAMRecord record) {
        super(record.getAlignmentStart(), record.getAlignmentEnd());
        this.chr = record.getReferenceName();
        if (record.getReadNegativeStrandFlag()) {
            strand = Strand.NEGATIVE;
        } else {
            strand = Strand.POSITIVE;
        }
        this.readBases = record.getReadBases();
        this.qualities = record.getBaseQualities();
        this.alignmentBlocks = record.getAlignmentBlocks();
    }

    /**
     * Instantiates a new sequence read with chromosome.
     *
     * @param chr       The chromosome
     * @param start     Read start
     * @param end       Read end
     * @param strand    Read strand
     * @param readBases Read bases
     * @param qualities Read qualities
     */
    public SequenceRead(String chr, int start, int end, Strand strand, byte[] readBases, byte[] qualities) {
        super(start, end);
        this.chr = chr;
        this.strand = strand;
        this.readBases = readBases;
        if (readBases != null) {
            if (qualities == null || qualities.length < readBases.length) {
                this.qualities = new byte[readBases.length];
                Arrays.fill(this.qualities, (byte) 255);
            } else {
                this.qualities = qualities;
            }
        }
    }

    /**
     * An adapter to parse alignment block from AlignmentBlock class.
     *
     * @return A list of alignment reads.
     */
    public List<SmallPieceSequence> getAlignmentBlocks() {
        List<SmallPieceSequence> list = new ArrayList<SmallPieceSequence>(alignmentBlocks.size());
        for (AlignmentBlock block : alignmentBlocks) {
            list.add(new SmallPieceSequence(block.getReadStart(), block.getReferenceStart(), block.getLength(), readBases, qualities));
        }
        return list;
    }

    public byte[] getReadBases() {
        return readBases;
    }

    public byte getReadBaseAt(int index) {
        return readBases[index];
    }

    public byte[] getQualities() {
        return qualities;
    }

    public byte getQuality(int index) {
        return qualities[index];
    }

    public String getChr() {
        return chr;
    }

    public Strand getStrand() {
        return strand;
    }

    public boolean contains(int position) {
        if (position > getStart() && position < getEnd()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        if (getChr() == null) {
            return getStart() + "-" + getEnd();
        } else {
            return getChr() + ":" + getStart() + "-" + getEnd();
        }
    }

    public class SmallPieceSequence {
        private int referenceStart;
        private int length;
        private byte[] bases;
        private byte[] qualities;

        public SmallPieceSequence(int readStart, int referenceStart, int length, byte[] rawBases, byte[] rawQualities) {
            this.referenceStart = referenceStart;
            this.length = length;
            bases = Arrays.copyOfRange(rawBases, readStart - 1, readStart + length - 1);
            qualities = Arrays.copyOfRange(rawQualities, readStart - 1, readStart + length - 1);
        }

        public int getEnd() {
            return referenceStart + length;
        }

        public int getReferenceStart() {
            return referenceStart;
        }

        public byte[] getQualities() {
            return qualities;
        }

        public byte[] getBases() {
            return bases;
        }

    }

}
