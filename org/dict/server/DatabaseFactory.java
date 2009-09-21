package org.dict.server;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.dict.kernel.Database;
import org.dict.kernel.DatabaseConfiguration;
import org.dict.kernel.DictEngine;
import org.dict.kernel.IComparator;
import org.dict.kernel.IDictEngine;
import org.dict.kernel.KeyComparator;
import org.dict.util.Logger;
/**
 * <p>To sort the index file: java org.dict.server.DatabaseFactory indexFile
 *
 * <p>Format of the ini file:
 * <pre>
	# Configuration for ev
	ev.data=anhviet.txt
	ev.index=anhviet.idx
	ev.memoryIndex=true
	ev.comparator=org.dict.vietdict.KeyComparator
	ev.name=English-Vietnamese
	# Configuration for ve
	ve.data=vietanh.txt
	ve.index=vietanh.idx
	ve.memoryIndex=true
	ve.comparator=org.dict.vietdict.KeyComparator
	ev.name=Vietnamese-English
 * </pre>
 */
public class DatabaseFactory {
	static java.util.Hashtable fInstances = new java.util.Hashtable();
/**
 * DatabaseFactory constructor comment.
 */
public DatabaseFactory() {
	super();
}
public static void addDatabases(IDictEngine eng, String cfg) {
	File f = new File(cfg);
	if (!f.exists()) {
		return;
	}
	log(new java.util.Date().toString());
	log("\nCreate dictionary engine using configuration "+cfg);
	DatabaseConfiguration[] configs = DatabaseConfiguration.readConfiguration(cfg);
	for (int i = 0; i<configs.length; i++){
		try {
			eng.addDatabase(Database.createDatabase(configs[i]));
		} catch (Throwable e) {
			log("Cannot create database "+configs[i]);
			log(e.toString());
		}
	}
	log("\nDatabases created!");
}
public static void dbSort(String in, IComparator c) {
	int last = in.lastIndexOf('.');
	String base = in;
	if (last > 0) {
		base = in.substring(0, last);
	}
	String tmp = base+".tmp";
	String bak = base+".bak";
	System.out.println("Sort file "+in+" and copy original file to "+bak);
	try {
		org.dict.kernel.list.KeyList kl = new org.dict.kernel.list.MemoryKeyList(in);
		org.dict.kernel.IKey[] arr = new org.dict.kernel.IKey[kl.size()];
		for (int i = 0; i < arr.length; i++){
			arr[i] = (org.dict.kernel.IKey) kl.get(i);
		}
		org.dict.kernel.list.ListUtil.sort(arr, c);
		BufferedOutputStream w = new BufferedOutputStream(new FileOutputStream(tmp));
		for (int i = 0; i < arr.length; i++){
			w.write((arr[i].getKey()+"\t"+arr[i].getOffset()+"\t"+arr[i].getLength()).getBytes(kl.getEncoding()));
			if (i<arr.length-1) w.write('\n');
		}
		w.flush();
		w.close();
		System.out.println("File sorted. Rename original file to "+bak);
		File old = new File(bak);
		if (old.exists()) {
			old.delete();
		}
		new File(in).renameTo(old);
		System.out.println("Rename temp file to "+in);
		File f = new File(tmp);
		try {
			boolean ret = f.renameTo(new File(in));
			if (!ret) {
				System.out.println("Cannot rename. Please copy file "+tmp+" to "+in);
			}
		} catch (Exception e) {
			System.out.println("Please copy file "+tmp+" to "+in);
			return;
		}
		if (f.exists()) {
			System.out.println("Delete temp file");
			try {
				f.delete();
			} catch (Exception e) {
				System.out.println("Could not remove temp file "+tmp);
			}
		}
		System.out.println("Done!");
	} catch (Throwable t) {
		t.printStackTrace();
	}
}
public static IDictEngine getEngine(String cfg) {
	String s = new File(cfg).getAbsolutePath().toLowerCase();
	IDictEngine ret = (IDictEngine) fInstances.get(s);
	if (ret == null) {
		ret = new DictEngine();
		addDatabases(ret, cfg);
		fInstances.put(s, ret);
	}
	return ret;
}

static void log(String msg) {
	Logger.getInstance().log(msg);
}
/**
 * 
 * @param args java.lang.String[]
 */
public static void main(String[] args) {
	if (args.length != 2) {
		System.out.println("Usage: java "+DatabaseFactory.class.getName()+" iniFile dbID");
		System.exit(0);
	}
	File f = new File(args[0]);
	java.util.Properties p = new java.util.Properties();
	try {
		InputStream in = new FileInputStream(f);
		p.load(in);
		in.close();
	} catch (Throwable t) {
		t.printStackTrace();
	}
	String id = args[1];
	String indexFile = (String) p.get(id+".index");
	String enc = p.getProperty(id + ".encoding", "utf-8");
	IComparator c = new KeyComparator();
	String comp = p.getProperty(id + ".comparator");
	if (comp != null) {
		try {
			c = (IComparator) Class.forName(comp).newInstance();
		} catch (Throwable e) {
		}
	}
	dbSort(indexFile, c);
}
}
