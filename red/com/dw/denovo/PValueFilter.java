package com.dw.denovo;

/**
 *P_value based on alt and ref 
 */

import rcaller.Globals;
import rcaller.RCaller;
import rcaller.RCode;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import com.dw.publicaffairs.DatabaseManager;
import com.xl.datatypes.probes.Probe;

public class PValueFilter {
	private DatabaseManager databaseManager;
	// File file = new File("D:/TDDOWNLOAD/data/hg19.txt");
	private String pIn = null;
	private String pValueTable = null;
	private String refHg19 = null;
	private String dnSnpTable = null;
	FileInputStream inputStream;
	private String line = null;
	private String[] col = new String[40];
	// insert鏃朵娇閿熺煫纰夋嫹閿熸枻鎷烽敓锟�
	private StringBuffer s1 = new StringBuffer();
	private StringBuffer s2 = new StringBuffer();
	// create table鏃朵娇閿熺煫纰夋嫹閿熻鍑ゆ嫹
	// insert 鏃堕敓鏂ゆ嫹浣块敓鐭鎷烽敓鏂ゆ嫹閿熸枻锟�
	private StringBuffer s3 = new StringBuffer();
	private int count = 1;
	private String chr;
	private String ps;
	private double known_alt = 0;
	private double known_ref = 0;
	ArrayList<Double> fd_ref = new ArrayList<Double>();
	ArrayList<Double> fd_alt = new ArrayList<Double>();
	List<String> coordinate = new ArrayList<String>();
	// private StringBuffer ref = new StringBuffer();
	// private StringBuffer alt = new StringBuffer();
	// float[] P_V;
	private double fdr = 0;
	private double ref_n = 0;
	private double alt_n = 0;
	private double pvalue = 0;
	// 閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鑺傞潻鎷峰紡
	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public PValueFilter(DatabaseManager databaseManager, String pIn,
			String pValueTable, String refHg19, String dnSnpTable) {
		this.databaseManager = databaseManager;
		this.pIn = pIn;
		this.pValueTable = pValueTable;
		this.refHg19 = refHg19;
		this.dnSnpTable = dnSnpTable;
	}
	

	public void loadRefHg19() {
		try {
			System.out.println("loadhg19 start" + " " + df.format(new Date()));// new
			
			int count_ts = 0;
			inputStream = new FileInputStream(pIn);
			BufferedReader rin = new BufferedReader(new InputStreamReader(
					inputStream));
			while ((line = rin.readLine()) != null) {
				StringBuffer s1 = new StringBuffer();
				if (count > 0) {
					s2.append(line.split("\\t")[0] + " " + "varchar(9)");
					s2.append("," + line.split("\\t")[1] + " " + "bigint");
					s2.append("," + line.split("\\t")[2] + " " + "varchar(5)");
					s2.append("," + line.split("\\t")[3] + " " + "varchar(5)");
					s2.append("," + line.split("\\t")[4] + " " + "varchar(5)");
					count--;
					s3.append(line.split("\\t")[0]);
					for (int i = 1; i < 5; i++)
						s3.append("," + line.split("\\t")[i]);
					databaseManager.deleteTable(refHg19);
					databaseManager.createTable(refHg19, "(" + s2
							+ ",index(chrom,coordinate))");
					continue;
				}
				databaseManager.setAutoCommit(false);
				for (int i = 0; i < 5; i++) {
					col[i] = line.split("\\t")[i];
					if (i == 0 && col[i].length() < 3)
						col[i] = "chr" + col[i];
				}
				// A-I or G is what we focus on
				if (col[3].toCharArray()[0] == 'A'
						&& (col[4].toCharArray()[0] == 'G' || col[4]
								.toCharArray()[0] == 'I')) {
					s1.append("'" + col[0] + "'");
					for (int i = 1; i < 5; i++)
						s1.append("," + "'" + col[i] + "'");
					// 閿熸枻鎷疯彉閿熸枻鎷烽敓鎹疯鎷烽敓璇紝姣忛敓鍙鎷烽敓鏂ゆ嫹
					databaseManager.executeSQL("insert into " + refHg19 + "("
							+ s3 + ") values(" + s1 + ")");
					count_ts++;
					if (count_ts % 20000 == 0)
						databaseManager.commit();
				}
			}
			databaseManager.commit();
			databaseManager.setAutoCommit(true);

			// clear insert data
			s1.delete(0, s1.length());
			s2.delete(0, s2.length());
			s3.delete(0, s3.length());

			System.out.println("loadhg19 end" + " " + df.format(new Date()));// new
																				// Date()涓洪敓鏂ゆ嫹鍙栭敓鏂ゆ嫹鍓嶇郴缁熸椂閿熸枻锟�
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void level(String chr, String ps) {
		try {
			ref_n = alt_n = 0;
			ResultSet rs = databaseManager.query(dnSnpTable, "AD", "chrome='"
					+ chr + "' and pos=" + ps + "");

			List<String> coordinate_level = new ArrayList<String>();

			while (rs.next()) {
				coordinate_level.add(rs.getString(1));
			}
			for (int i = 0, len = coordinate_level.size(); i < len; i++) {
				String[] section = coordinate_level.get(i).split(";");
				ref_n = Double.parseDouble(section[0]);
				alt_n = Double.parseDouble(section[1]);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void Exp_num() {
		try {
			ResultSet rs = databaseManager.query(dnSnpTable, "chrome,pos", "1");

			while (rs.next()) {
				coordinate.add(rs.getString(1));
				coordinate.add(rs.getString(2));
			}
			for (int i = 0, len = coordinate.size(); i < len; i++) {
				if (i % 2 == 0) {
					chr = coordinate.get(i);
				} else {
					ps = coordinate.get(i);
					level(chr, ps);
					rs = databaseManager.query(refHg19, "strand", "chrom='"
							+ chr + "' and coordinate='" + ps + "'");
					fd_alt.add(alt_n);
					fd_ref.add(ref_n);
					if (rs.next()) {
						known_alt += alt_n;
						known_ref += ref_n;
					} else {
						known_alt += 0;
						known_ref += (alt_n + ref_n);
					}
				}

			}

			known_alt /= (coordinate.size() / 2);
			known_ref /= (coordinate.size() / 2);
			known_alt = Math.round(known_alt);
			known_ref = Math.round(known_ref);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public double calculate(double found_ref, double found_alt,
			double known_ref, double known_alt, String commandD) {
		try {
			RCaller caller = new RCaller();
			RCode code = new RCode();
			Globals.detect_current_rscript();
			caller.setRscriptExecutable(commandD);

			double[][] data = new double[][] { { found_ref, found_alt },
					{ known_ref, known_alt } };
			// double[][] data=new double[][]{{51.2,49.3},{100.,0}};
			code.addDoubleMatrix("mydata", data);
			code.addRCode("result <- fisher.test(mydata)");
			code.addRCode("mylist <- list(pval = result$p.value)");

			caller.setRCode(code);
			caller.runAndReturnResult("mylist");
			pvalue = caller.getParser().getAsDoubleArray("pval")[0];
			return pvalue;
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		return 0;
	}

	public void estblishPvTable() {
		databaseManager.deleteTable(pValueTable);
		databaseManager
				.createTable(
						pValueTable,
						"(chrome varchar(15),pos int,ref smallint,alt smallint,level varchar(10),p_value double,fdr double)");
	}

	public void P_V(String commandD) {
		System.out.println("P_V start" + " " + df.format(new Date()));// new
																		// Date()涓洪敓鏂ゆ嫹鍙栭敓鏂ゆ嫹鍓嶇郴缁熸椂閿熸枻锟�
		estblishPvTable();
		Exp_num();
		DecimalFormat dF = new DecimalFormat("0.000 ");

		for (int i = 0, len = fd_ref.size(); i < len; i++) {
//			System.out.println(fd_ref.get(i) + " " + coordinate.get(i) + " "
//					+ i);
			ref_n = fd_ref.get(i);
			alt_n = fd_alt.get(i);
			chr = coordinate.get(2 * i);

			ps = coordinate.get(2 * i + 1);
			double lev = alt_n / (alt_n + ref_n);
			// double lev=found_alt[i]/(found_alt[i]+found_ref[i]);
			// if (((int) ref_n + (int) alt_n) < 6)
			// {
			// continue;
			// }
			calculate(ref_n, alt_n, known_ref, known_alt, commandD);
			if(pvalue<0.05){
			databaseManager.executeSQL("insert into " + pValueTable
					+ "(chrome,pos,ref,alt,level,p_value) values('" + chr
					+ "'," + ps + "," + (int) ref_n + "," + (int) alt_n + ","
					+ dF.format(lev) + "," + pvalue + ")");
			// System.out.println(chr+" "+ps+" "+(int)found_ref[i]+" "+(int)found_alt[i]+" "+dF.format(lev)+" "+pvalue);
			s1.append(pvalue + "\t");
			}
		}

		System.out.println("P_V end" + " " + df.format(new Date()));// new
																	// Date()涓洪敓鏂ゆ嫹鍙栭敓鏂ゆ嫹鍓嶇郴缁熸椂閿熸枻锟�
	}

	public void fdr(String commandD) {
		P_V(commandD);
		try {
			RCaller caller = new RCaller();
			RCode code = new RCode();
			Globals.detect_current_rscript();
			caller.setRscriptExecutable(commandD);
			ArrayList<Float> P_V = new ArrayList<Float>();
			for (int i = 0; i < s1.toString().split("\t").length; i++) {
				// System.out.println(s1.toString().split("\t")[i]);
				// P_V[i]=Double.parseDouble(s1.toString().split("\t")[i]);
				P_V.add(Float.valueOf(s1.toString().split("\t")[i]));
			}
			Object[] objs = P_V.toArray();
			float[] floats = new float[objs.length];
			for (int i = 0; i < objs.length; i++) {
				floats[i] = (Float) objs[i];
			}
			code.addFloatArray("parray", floats);
			code.addRCode("result<-p.adjust(parray,method='fdr',length(parray))");
			// code.addRCode("mylist <- list(qval = result$q.value)");
			caller.setRCode(code);
			caller.runAndReturnResult("result");

			for (int i = 0; i < caller.getParser().getAsDoubleArray("result").length; i++) {
				// for (int i = 0; i < coordinate.size(); i++) {
				fdr = caller.getParser().getAsDoubleArray("result")[i];
				chr = coordinate.get(i);
				i++;
				ps = coordinate.get(i);
				databaseManager.executeSQL("update " + pValueTable
						+ " set fdr=" + fdr + " where chrome='" + chr
						+ "' and pos=" + ps + " ");
			}
			// clear insert data
			s1.delete(0, s1.length());
			s2.delete(0, s2.length());
			s3.delete(0, s3.length());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Vector<Probe> queryAllEditingSites(){
		Vector<Probe> probeVector= new Vector<>();
		ResultSet rs=databaseManager.query(pValueTable, " chrome, pos,alt "," 1 ");
		try {
			while(rs.next()){
				Probe p=new Probe(rs.getString(1),rs.getInt(2),rs.getString(3).toCharArray()[0]);
				probeVector.add(p);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return probeVector;
	}
 
 public Probe queryEditingSite(String chrome,int pos){
		ResultSet rs=databaseManager.query(pValueTable, " chrome, pos ,alt "," chrome="+chrome+" and pos='"+pos+"' ");
		try {
			while(rs.next()){
				return new Probe(rs.getString(1),rs.getInt(2),rs.getString(3).toCharArray()[0]);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return null;
	}
 
 public Vector<Probe> queryEditingSitesForChr(String chrome){
		Vector<Probe> probeVector= new Vector<>();
		ResultSet rs=databaseManager.query(pValueTable, " chrome, pos ,alt "," chrome="+chrome+" ");
		try {
			while(rs.next()){
				Probe p=new Probe(rs.getString(1),rs.getInt(2),rs.getString(3).toCharArray()[0]);
				probeVector.add(p);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return probeVector;
	}
	
	

}