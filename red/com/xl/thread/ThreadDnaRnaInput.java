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

package com.xl.thread;

import com.xl.database.DatabaseManager;
import com.xl.database.DatabaseSelector;
import com.xl.database.TableCreator;
import com.xl.exception.REDException;
import com.xl.filter.denovo.FisherExactTestFilter;
import com.xl.filter.denovo.KnownSNPFilter;
import com.xl.filter.denovo.RepeatRegionsFilter;
import com.xl.filter.denovo.SpliceJunctionFilter;
import com.xl.main.REDApplication;
import com.xl.parsers.dataparsers.DNAVCFParser;
import com.xl.parsers.dataparsers.RNAVCFParser;
import com.xl.preferences.LocationPreferences;

/**
 * Created by Xing Li on 2014/7/22.
 * <p/>
 * The Class ThreadDnaRnaInput generates a new thread to input all data with DNA-RNA mode.
 */
public class ThreadDnaRnaInput implements Runnable {

    @Override
    public void run() {
        DatabaseManager manager = DatabaseManager.getInstance();
        LocationPreferences locationPreferences = LocationPreferences.getInstance();
        manager.setAutoCommit(true);

        manager.createDatabase(DatabaseManager.DNA_RNA_DATABASE_NAME);
        manager.useDatabase(DatabaseManager.DNA_RNA_DATABASE_NAME);

        RNAVCFParser rnaVCFParser = new RNAVCFParser();
        rnaVCFParser.parseVCFFile(locationPreferences.getRnaVcfFile());
        String[] rnaVcfSamples = rnaVCFParser.getSampleNames();

        DNAVCFParser dnaVCFParser = new DNAVCFParser();
        dnaVCFParser.parseVCFFile(locationPreferences.getDnaVcfFile());
        String[] dnaVCFSamples = dnaVCFParser.getSampleNames();
        boolean match = false;
        for (String rnaSample : rnaVcfSamples) {
            match = false;
            for (String dnaSample : dnaVCFSamples) {
                if (rnaSample.equals(dnaSample)) {
                    match = true;
                }
            }
        }
        if(!match){
            try {
                throw new REDException("Samples in DNA VCF file does not match the RNA VCF, please have a check the sample name.");
            } catch (REDException e) {
                e.printStackTrace();
            }
        }

        RepeatRegionsFilter rf = new RepeatRegionsFilter(manager);
        TableCreator.createRepeatRegionsTable(DatabaseManager.REPEAT_MASKER_TABLE_NAME);
        rf.loadRepeatTable(DatabaseManager.REPEAT_MASKER_TABLE_NAME, locationPreferences.getRepeatFile());

        SpliceJunctionFilter cf = new SpliceJunctionFilter(manager);
        TableCreator.createSpliceJunctionTable(DatabaseManager.SPLICE_JUNCTION_TABLE_NAME);
        cf.loadSpliceJunctionTable(DatabaseManager.SPLICE_JUNCTION_TABLE_NAME, locationPreferences.getRefSeqFile());

        KnownSNPFilter sf = new KnownSNPFilter(manager);
        TableCreator.createDBSNPTable(DatabaseManager.DBSNP_DATABASE_TABLE_NAME);
        sf.loadDbSNPTable(DatabaseManager.DBSNP_DATABASE_TABLE_NAME, locationPreferences.getDbSNPFile());

        FisherExactTestFilter pv = new FisherExactTestFilter(manager);
        TableCreator.createDARNEDTable(DatabaseManager.DARNED_DATABASE_TABLE_NAME);
        pv.loadDarnedTable(DatabaseManager.DARNED_DATABASE_TABLE_NAME, locationPreferences.getDarnedFile());

        new DatabaseSelector(REDApplication.getInstance());
    }
}