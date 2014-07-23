package com.dw.dnarna;

/**
 * import vcf file
 */

import com.dw.publicaffairs.DatabaseManager;
import com.dw.publicaffairs.Utilities;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DnaRnaVcf {
    // establish database connection;
    private DatabaseManager databaseManager;

    private String dnaVcfPath = null;
    private String rnaVcfPath = null;
    private String dnaVcfTable = null;
    private String rnaVcfTable = null;

    FileInputStream inputStream;
    // SQL to be executed
    // basic unit to process
    private String line = null;
    // data of each column
    private String[] col = new String[40];
    private String[] temp = new String[10];
    private StringBuffer s1 = new StringBuffer();
    // count for each function
    private int count_r = 1;
    private int count_d = 1;

    private int ref = 0;
    private int alt = 0;
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // public RedInput()
    // {
    // this.file=new File(dir);
    // }
    // public String directory()
    // {
    // dir="D:/TDDOWNLOAD/HCC448T.subset.vcf";
    // return dir;
    // }
    // establish table structure for following tables

    /**
     * @param databaseManager
     * @param rnaVcfPath
     * @param dnaVcfPath
     * @param rnaVcfTable
     * @param dnaVcfTable
     */
    public DnaRnaVcf(DatabaseManager databaseManager, String rnaVcfPath, String dnaVcfPath, String rnaVcfTable,
                     String dnaVcfTable) {
        this.databaseManager = databaseManager;
        this.rnaVcfPath = rnaVcfPath;
        this.dnaVcfPath = dnaVcfPath;
        this.rnaVcfTable = rnaVcfTable;
        this.dnaVcfTable = dnaVcfTable;
    }

    public void establishRnaTable() {
        databaseManager.deleteTable(rnaVcfTable);
        databaseManager.createTable(rnaVcfTable, "(chrome varchar(15),"
                + Utilities.getInstance().getS2() + ",index(chrome,pos))");
    }

    public void establishDnaTable() {
        databaseManager.deleteTable(dnaVcfTable);
        databaseManager.createTable(dnaVcfTable, "(chrome varchar(15),"
                + Utilities.getInstance().getS2() + ",index(chrome,pos))");
    }

    public int getdepth() {
        try {
            inputStream = new FileInputStream(rnaVcfPath);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        BufferedReader rin = new BufferedReader(new InputStreamReader(
                inputStream));
        int depth = -1;
        try {
            while ((line = rin.readLine()) != null) {
                s1 = new StringBuffer();
                if (line.startsWith("##"))
                    continue;
                if (line.startsWith("#"))
                    continue;
                // value in each line
                for (int k = 0; k < line.split("\\t")[8].split(":").length; k++) {
                    if (line.split("\\t")[8].split(":")[k].equals("DP")) {
                        depth = k;
                    }
                }
                break;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(depth);
        return depth;
    }

    // table for RnaVcf X.length-9=time for circulation
    public void RnaVcf(int num) {
        System.out.println("rnavcf start" + " " + df.format(new Date()));

        try {

            databaseManager.setAutoCommit(false);
            // timer for transaction
            int ts_count = 0;
            int depth = -1;
            int gtype = -1;
            try {
                inputStream = new FileInputStream(rnaVcfPath);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            BufferedReader rin = new BufferedReader(new InputStreamReader(
                    inputStream));
            while ((line = rin.readLine()) != null) {
                if (line.startsWith("##"))
                    continue;
                if (line.startsWith("#")) {
                    continue;
                }
                for (int i = 0; i < line.split("\\t").length; i++) {
                    col[i] = line.split("\\t")[i];
                }
                int length = col[8].split(":").length;
                if (col[num].split(":").length != length) {
                    continue;
                }
                if (count_r > 0) {
                    for (int i = 0; i < col[8].split(":").length; i++) {
                        if (line.split("\\t")[8].split(":")[i].equals("DP")) {
                            depth = i;
                        }
                        if (col[8].split(":")[i].equals("GT")) {
                            gtype = i;
                        }
                    }
                    count_r--;
                }
                // data for import
                // '.' stands for undetected, so we discard it
                // if ((depth>-1)&&col[num].split(":")[depth].equals("."))
                // continue;
                if (gtype > -1
                        && ((col[num].split(":")[gtype].split("/")[0]
                        .equals(".")) || (col[num].split(":")[gtype]
                        .split("/")[1].equals(".")))) {
                    continue;
                }
                s1.append("'" + col[0] + "'");
                for (int i = 1; i < 8; i++)
                    s1.append("," + "'" + col[i] + "'");
                for (int i = 0; i < col[num].split(":").length; i++) {
                    temp[i] = col[num].split(":")[i].replace(",", ";");
                    // System.out.println(temp[i]);
                    s1.append("," + "'" + temp[i] + "'");
                }
                databaseManager.executeSQL("insert into " + rnaVcfTable + "("
                        + Utilities.getInstance().getS3() + ") values(" + s1
                        + ")");
                ts_count++;
                if (ts_count % 20000 == 0)
                    databaseManager.commit();
                // clear insert data
                s1.delete(0, s1.length());
            }
            databaseManager.commit();
            databaseManager.setAutoCommit(true);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("rnavcf end" + " " + df.format(new Date()));

    }

    public void rnaVcf() {
        System.out.println("rnavcf start" + " " + df.format(new Date()));

        try {
            databaseManager.setAutoCommit(false);
            // timer for transaction
            int ts_count = 0;
            int depth = -1;
            int gtype = -1;
            try {
                inputStream = new FileInputStream(rnaVcfPath);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            BufferedReader rin = new BufferedReader(new InputStreamReader(
                    inputStream));
            while ((line = rin.readLine()) != null) {
                if (line.startsWith("##"))
                    continue;
                if (line.startsWith("#")) {
                    continue;
                }
                for (int i = 0; i < line.split("\\t").length; i++) {
                    col[i] = line.split("\\t")[i];
                }
                int length = col[8].split(":").length;
                if (col[9].split(":").length != length) {
                    continue;
                }
                if (count_r > 0) {
                    for (int i = 0; i < col[8].split(":").length; i++) {
                        if (line.split("\\t")[8].split(":")[i].equals("DP")) {
                            depth = i;
                        }
                        if (col[8].split(":")[i].equals("GT")) {
                            gtype = i;
                        }
                    }
                    count_r--;
                }
                // data for import
                // '.' stands for undetected, so we discard it
                // if ((depth>-1)&&col[num].split(":")[depth].equals("."))
                // continue;
                if (gtype > -1
                        && ((col[9].split(":")[gtype].split("/")[0]
                        .equals(".")) || (col[9].split(":")[gtype]
                        .split("/")[1].equals(".")))) {
                    continue;
                }
                s1.append("'" + col[0] + "'");
                for (int i = 1; i < 8; i++)
                    s1.append("," + "'" + col[i] + "'");
                for (int i = 0; i < col[9].split(":").length; i++) {
                    temp[i] = col[9].split(":")[i].replace(",", ";");
                    // System.out.println(temp[i]);
                    s1.append("," + "'" + temp[i] + "'");
                }
                databaseManager.executeSQL("insert into " + rnaVcfTable + "("
                        + Utilities.getInstance().getS3() + ") values(" + s1
                        + ")");
                ts_count++;
                if (ts_count % 20000 == 0)
                    databaseManager.commit();
                // clear insert data
                s1.delete(0, s1.length());
            }
            databaseManager.commit();
            databaseManager.setAutoCommit(true);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("rnavcf end" + " " + df.format(new Date()));

    }

    // table for DnaVcf
    public void dnaVcf(int num) {
        System.out.println("dnavcf start" + " " + df.format(new Date()));

        try {
            databaseManager.setAutoCommit(false);
            // timer for transaction
            int ts_count = 0;
            int depth = -1;
            int gtype = -1;
            try {
                inputStream = new FileInputStream(dnaVcfPath);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            BufferedReader rin = new BufferedReader(new InputStreamReader(
                    inputStream));
            while ((line = rin.readLine()) != null) {

                if (line.startsWith("##") || line.startsWith("#"))
                    continue;

                for (int i = 0; i < line.split("\\t").length; i++) {
                    col[i] = line.split("\\t")[i];
                }
                int length = col[8].split(":").length;
                if (col[num].split(":").length != length) {
                    continue;
                }
                if (count_d > 0) {
                    for (int i = 0; i < col[8].split(":").length; i++) {
                        if (col[8].split(":")[i].equals("DP")) {
                            depth = i;
                        }
                        if (col[8].split(":")[i].equals("GT")) {
                            gtype = i;
                        }
                    }
                    count_d--;
                }
                // data for import
                // we don't need unnecessary base
                // ||(Float.parseFloat(col[5])<20)
                if (!(col[3].equals("A")) || !(col[4].equals("."))
                        || !(col[6].equals("PASS"))) {
                    continue;
                }
                // if((depth>-1)&&(col[num].split(":")[depth].equals(".")))
                // {
                // continue;
                // }
                // '.' stands for undetected, so we discard it
                if (gtype > -1
                        && ((col[num].split(":")[gtype].split("/")[0]
                        .equals(".")) || (col[num].split(":")[gtype]
                        .split("/")[1].equals(".")))) {
                    continue;
                }
                ref = Integer
                        .parseInt(col[num].split(":")[gtype].split("/")[0]);
                alt = Integer
                        .parseInt(col[num].split(":")[gtype].split("/")[1]);
                if ((ref != 0) || (alt != 0)) {
                    continue;
                }

                s1.append("'" + col[0] + "'");
                for (int i = 1; i < 8; i++)
                    s1.append("," + "'" + col[i] + "'");
                for (int i = 0; i < col[num].split(":").length; i++) {
                    temp[i] = col[num].split(":")[i].replace(",", ";");
                    // System.out.println(temp[i]);
                    s1.append("," + "'" + temp[i] + "'");
                }
                databaseManager.executeSQL("insert into " + dnaVcfTable + "("
                        + Utilities.getInstance().getS3() + ") values(" + s1
                        + ")");
                ts_count++;
                if (ts_count % 20000 == 0) {
                    databaseManager.commit();
                }

                // clear insert data
                s1.delete(0, s1.length());
            }
            databaseManager.commit();
            databaseManager.setAutoCommit(true);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("dnavcf end" + " " + df.format(new Date()));

    }

    // table for DnaVcf
    public void dnaVcf() {
        System.out.println("dnavcf start" + " " + df.format(new Date()));

        try {
            databaseManager.setAutoCommit(false);
            // timer for transaction
            int ts_count = 0;
            int depth = -1;
            int gtype = -1;
            try {
                inputStream = new FileInputStream(dnaVcfPath);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            BufferedReader rin = new BufferedReader(new InputStreamReader(
                    inputStream));
            while ((line = rin.readLine()) != null) {
                if (line.startsWith("##"))
                    continue;
                if (line.startsWith("#")) {
                    continue;
                }
                for (int i = 0; i < line.split("\\t").length; i++) {
                    col[i] = line.split("\\t")[i];
                }
                int length = col[8].split(":").length;
                if (col[9].split(":").length != length) {
                    continue;
                }
                if (count_d > 0) {
                    for (int i = 0; i < col[8].split(":").length; i++) {
                        if (col[8].split(":")[i].equals("DP")) {
                            depth = i;
                        }
                        if (col[8].split(":")[i].equals("GT")) {
                            gtype = i;
                        }
                    }
                    count_d--;
                }
                // data for import
                // we don't need unnecessary base
                // ||(Float.parseFloat(col[5])<20)
                if (!(col[3].equals("A")) || !(col[4].equals("."))
                        || !(col[6].equals("PASS"))) {
                    continue;
                }
                // if((depth>-1)&&(col[num].split(":")[depth].equals(".")))
                // {
                // continue;
                // }
                // '.' stands for undetected, so we discard it
                if (gtype > -1
                        && ((col[9].split(":")[gtype].split("/")[0].equals(".")) || (col[9]
                        .split(":")[gtype].split("/")[1].equals(".")))) {
                    continue;
                }
                ref = Integer.parseInt(col[9].split(":")[gtype].split("/")[0]);
                alt = Integer.parseInt(col[9].split(":")[gtype].split("/")[1]);
                if ((ref != 0) || (alt != 0)) {
                    continue;
                }

                s1.append("'" + col[0] + "'");
                for (int i = 1; i < 8; i++)
                    s1.append("," + "'" + col[i] + "'");
                for (int i = 0; i < col[9].split(":").length; i++) {
                    temp[i] = col[9].split(":")[i].replace(",", ";");
                    // System.out.println(temp[i]);
                    s1.append("," + "'" + temp[i] + "'");
                }
                databaseManager.executeSQL("insert into " + dnaVcfTable + "("
                        + Utilities.getInstance().getS3() + ") values(" + s1
                        + ")");
                ts_count++;
                if (ts_count % 20000 == 0)
                    databaseManager.commit();
                // clear insert data
                s1.delete(0, s1.length());
            }
            databaseManager.commit();
            databaseManager.setAutoCommit(true);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("dnavcf end" + " " + df.format(new Date()));
    }

}
