package org.dict.server;

import java.io.*;

public class Logger {
	private static int BUFFER = Integer.getInteger("BUFFER", 10000).intValue();
	public static boolean DEBUG = Boolean.getBoolean("debug"); 
	private static Logger fInstance = new Logger();
	public static Logger getInstance() {
		return fInstance;
	}
	java.text.DateFormat df = new java.text.SimpleDateFormat("yyyyMMdd");
	StringBuffer sb = new StringBuffer(BUFFER);
	/**
	 * Logger constructor comment.
	 */
	public Logger() {
		super();
	}
	public void flush() {
		String file = df.format(new java.util.Date()) + ".txt";
		File f = new File(file);
		if (f.length() < 512000) {
			log(sb.toString(), file);
		}
		sb = new StringBuffer(BUFFER);
		sb.append(new java.util.Date()).append(": ");
	}
	public void log(String s) {
		sb.append(s).append('\n');
		if (DEBUG || sb.length() >= BUFFER) {
			flush();
		}
	}
	public void log(String s, String file) {
		if (DEBUG) {
			System.out.println(s);
			return;
		}
		try {
			OutputStream os = new BufferedOutputStream(new FileOutputStream(file, true));
			os.write(s.getBytes());
			os.write('\n');
			os.flush();
			os.close();
		} catch (Throwable t) {
		
		}
	}
}
