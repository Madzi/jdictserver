package org.dict.server;

import java.net.*;
import java.io.*;
import java.util.*;
import org.dict.kernel.*;
/**
 * Start with: java ...JDictd iniFile<p>
 * For the format of the ini file see DatabaseFactory.
 */
public class JDictd implements Runnable {
	public static final String SERVER_NAME = "Ho Ngoc Duc's DICT server";
	public static final String SERVER_VERSION = "2.2";
	public static final String DICT_PORT = "dict_port";
	public static final String HTTP_PORT = "http_port";
	//private static Properties fProperties = new Properties();
	private IDictEngine fEngine;
	private ServerSocket fServerSocket;
	private ServerSocket fHTTPSocket;
	public JDictd(ServerSocket dictdSocket, ServerSocket httpSocket, IDictEngine engine) {
		fServerSocket = dictdSocket;
		fHTTPSocket = httpSocket;
		fEngine = engine;
	}
	public static void createForm(File f, IDatabase[] dbs) {
		String form = "", head = "", tail = "";
		try {
			FileInputStream fis = new FileInputStream(f);
			byte[] b = new byte[(int)f.length()];
			fis.read(b);
			fis.close();
			form = new String(b);
		} catch (Throwable t) {    	
		}
		int start = form.indexOf("<select multiple name=\"db\"");
		int end = form.indexOf("</select>", start+1);
		if (start >= 0 && end > start) {
			head = form.substring(0, start).trim();
			tail = form.substring(end+"</select>".length()).trim();
		}
		try {
			PrintWriter out = new PrintWriter(new FileWriter(f));
			out.println(head);
			out.println("<select multiple name=\"db\" size="+dbs.length+">");
			for (int i = 0; i < dbs.length; i++) {
				String db = dbs[i].getID();
				if (i == 0) {
					out.println("  <option value=" + db + " selected>" + dbs[i].getName());
				} else {
					out.println("  <option value=" + db + ">" + dbs[i].getName());
				}
			}
			out.println("</select>");
			out.println(tail);
			out.flush();
			out.close();
		} catch (Throwable t) {
			//t.printStackTrace();
		}
	}
	public static void listen(String[] args) {
		Properties prop = System.getProperties();
		if (args == null) {
			throw new RuntimeException("No configuration file specified");
		}
		DictEngine engine = new DictEngine();
		for (int i = 0; i < args.length; i++){
			DatabaseFactory.addDatabases(engine, args[i]);
		}
		File f = new File(System.getProperty("ROOT", "."));
		f = new File(f, "form.html");
		createForm(f, engine.getDatabases());
		try {
			int port = Integer.parseInt(prop.getProperty(DICT_PORT, "2628"));
			ServerSocket socket = new ServerSocket(port);
			Runnable r = new JDictd(socket, null, engine);
			new Thread(r, SERVER_NAME).start();
			trace(new Date().toString() + ": JDictd started at " + port);
		} catch (Exception e) {
			System.out.println("Cannot init: " + e);
		}
		try {
			int httpPort = Integer.parseInt(prop.getProperty(HTTP_PORT, "2626"));
			if (httpPort > 0) {
				ServerSocket httpSocket = new ServerSocket(httpPort);
				Runnable httpRunner = new JDictd(null, httpSocket, engine);
				new Thread(httpRunner, SERVER_NAME).start();
				trace(new Date().toString() + ": HTTP server started at " + httpPort);
			} else {
				trace("HTTP server not started");
			}
		} catch (Exception e) {
			System.out.println("Cannot init: " + e);
			return;
		}
	}
	public static void log(String msg, String file) {
		Logger.getInstance().log(msg, file);
	}
	public static void main(String args[]) {
		if (args.length == 0) {
			System.out.println("Usage: java ...JDictd configFile [configFile ...]");
			System.exit(0);
		}
		listen(args);
	}
	public void run() {
		if (fServerSocket != null) {
			runRFC2229();
		}
		if (fHTTPSocket != null) {
			runHTTP();
		}
	}
	protected void runHTTP() {
		try {
			do {
				java.net.Socket s = fHTTPSocket.accept();
				DictHTTPConnection c = new DictHTTPConnection(fEngine, s, fHTTPSocket);
				new Thread(c).start();
			} while (true);
			//fHTTPSocket.close();
		} catch (IOException e) {
			trace("Error while running: " + e.getMessage());
		}
	}
	protected void runRFC2229() {
		try {
			do {
				java.net.Socket s = fServerSocket.accept();
				s.setSoTimeout(5000);
				RFC2229 r = new RFC2229(fEngine, s);
				new Thread(r).start();
			} while (true);
		} catch (IOException e) {
			trace("Error while running: " + e.getMessage());
		}
	}
	static void trace(String s) {
		log(s, "jdictd.log");
	}
}
