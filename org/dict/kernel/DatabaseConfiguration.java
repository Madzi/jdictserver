/*
 * Created on 12.03.2004
 *
 */
package org.dict.kernel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.Properties;
import java.util.Vector;

/**
 * @author duc
 *
 */

public class DatabaseConfiguration {
	
	public static DatabaseConfiguration[] readConfiguration(String cfg) {
		File f = new File(cfg);
		if (!f.exists()) {
			return new DatabaseConfiguration[0];
		}
		Properties p = new Properties();
		try {
			InputStream in = new FileInputStream(f);
			p.load(in);
			in.close();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		String[] dbIDs = getDatabaseNames(cfg);
		Vector v = new Vector(dbIDs.length);
		for (int i = 0; i < dbIDs.length; i++) {
			if ("false".equals(p.getProperty(dbIDs[i]+".use"))) continue;
			v.addElement(new DatabaseConfiguration(dbIDs[i], f, p));
		}
		DatabaseConfiguration[] ret = new DatabaseConfiguration[v.size()];
		v.copyInto(ret);
		return ret;
	}
	
	static File getFile(File cfg, String s) {
		File f = new File(s);
		if (f.isAbsolute()) return f;
		return new File(cfg.getParent(), s);
	}

	private static String[] getDatabaseNames(String cfg) {
		Vector v = new Vector(5);
		try {
			BufferedReader r = new BufferedReader(new FileReader(cfg));
			String s;
			while ((s = r.readLine()) != null) {
				if (s.startsWith("#")) continue;
				int idx = s.indexOf('.');
				if (idx > 0 && s.indexOf('=', idx) > 0) {
					s = s.substring(0, idx);
					if (!v.contains(s)) {
						v.addElement(s);
					}
				}
			}
			r.close();
		} catch (Throwable t) {
			//t.printStackTrace();
		}
		String[] dbIDs = new String[v.size()];
		v.copyInto(dbIDs);
		return dbIDs;
	}
	
	String id, name, dbClass, encoding, comparator, htmlPrinter, plainPrinter, morph;
	File data, index, cfgFile;
	boolean memoryIndex;
	
	public DatabaseConfiguration(String id, File f, Properties p) {
		setId(id);
		setCfgFile(f);
		setName(p.getProperty(id + ".name"));
		setData(getFile(f, p.getProperty(id + ".data")));
		setIndex(getFile(f, p.getProperty(id + ".index")));
		setEncoding(p.getProperty(id + ".encoding", "UTF-8"));
		setMemoryIndex(!"false".equals(p.getProperty(id + ".memoryIndex")));
		setDbClass(p.getProperty(id + ".dbClass"));
		setMorph(p.getProperty(id + ".morph"));
		setComparator(p.getProperty(id + ".comparator"));
		setHtmlPrinter(p.getProperty(id + ".html"));
		setPlainPrinter(p.getProperty(id + ".txt"));
	}
	
	public String getComparator() {
		return comparator;
	}

	public File getData() {
		return data;
	}

	public String getDbClass() {
		return dbClass;
	}

	public String getEncoding() {
		return encoding;
	}

	public String getHtmlPrinter() {
		return htmlPrinter;
	}

	public String getId() {
		return id;
	}

	public File getIndex() {
		return index;
	}

	public boolean isMemoryIndex() {
		return memoryIndex;
	}

	public String getMorph() {
		return morph;
	}

	public String getName() {
		return name;
	}

	public String getPlainPrinter() {
		return plainPrinter;
	}

	public void setComparator(String string) {
		comparator = string;
	}

	public void setData(File f) {
		data = f;
	}

	public void setDbClass(String string) {
		dbClass = string;
	}

	public void setEncoding(String string) {
		encoding = string;
	}

	public void setHtmlPrinter(String string) {
		htmlPrinter = string;
	}

	public void setId(String string) {
		id = string;
	}

	public void setIndex(File f) {
		index = f;
	}

	public void setMemoryIndex(boolean b) {
		memoryIndex = b;
	}

	public void setMorph(String string) {
		morph = string;
	}

	public void setName(String string) {
		name = string;
	}

	public void setPlainPrinter(String string) {
		plainPrinter = string;
	}
	
	public boolean equals(Object o) {
		if (o instanceof DatabaseConfiguration) {
			DatabaseConfiguration dc = (DatabaseConfiguration) o;
			return getIndex().equals(dc.getIndex()) && getData().equals(dc.getData());
		} else {
			return false;
		}
	}
	
	public int hashCode() {
		return getIndex().hashCode();
	}
	
	public String getDisplayString() {
		String s = unescape(getName());
		if (s == null) {
			s = getId();
		}
		if (s.length() > 20) {
			s = s.substring(0, 17)+"...";
		}
		return s;
	}
	
	public String toString() {
		return getId()+" | Index: "+getIndex()+" | Data: "+getData();
	}

	public File getCfgFile() {
		return cfgFile;
	}

	public void setCfgFile(File file) {
		cfgFile = file;
	}

	static String unescape(String s) {
		String ret = s;
		int k1 = ret.indexOf("&#");
		int k2 = ret.indexOf(";", k1);
		while (k1 >= 0 && k2 > k1) {
			String esc = ret.substring(k1+"&#".length(), k2);
			String c = "";
			try {
				if (esc.toLowerCase().startsWith("x")) {
					c = ""+(char)Integer.parseInt(esc.substring(1), 16);
				} else {
					c = ""+(char)Integer.parseInt(esc);
				}
			} catch (Throwable e) {
			}
			ret = ret.substring(0, k1)+c+ret.substring(k2+1);
			k1 = ret.indexOf("&#");
			k2 = ret.indexOf(";", k1);
		}
		return ret; 
	}
	

}
