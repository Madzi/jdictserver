package org.dict.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
/**
 * Insert the type's description here.
 * Creation date: (24.02.2002 16:33:09)
 * @author: 
 */
public class JDict {
	String host = "localhost";
	int port = 2628;
	String db = "*";
	boolean match = false;
/**
 * JDict constructor comment.
 */
public JDict() {
	super();
}
/**
 * Insert the method's description here.
 * Creation date: (24.02.2002 16:47:00)
 * @return java.lang.String
 */
public java.lang.String getDb() {
	return db;
}
/**
 * Insert the method's description here.
 * Creation date: (24.02.2002 16:42:15)
 * @return java.lang.String
 */
public java.lang.String getHost() {
	return host;
}
/**
 * Insert the method's description here.
 * Creation date: (24.02.2002 16:42:15)
 * @return int
 */
public int getPort() {
	return port;
}
/**
 * Insert the method's description here.
 * Creation date: (24.02.2002 16:42:15)
 * @return boolean
 */
public boolean isMatch() {
	return match;
}
public void lookup(String[] args) {
	Socket s = null;
	String word = args[args.length-1];
	PrintWriter out = null;
	BufferedReader in = null;
	try {
		s = new Socket(getHost(), getPort());
		out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), "UTF-8"), true);
		in = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF-8"));
		String fromServer = in.readLine(); // Server banner
		//out.println("client \"Java DICT client\"");
		out.println("define "+getDb()+" "+word);
		fromServer = in.readLine();
		if (fromServer.startsWith("552") && !fromServer.startsWith("550")) {
			out.println("match "+getDb()+" prefix "+word);
		}
		out.println("quit");
		while ((fromServer = in.readLine()) != null) {
			System.out.println(fromServer);
		}
		out.close();
		in.close();
		s.close();
	} catch (Exception e) {
		System.err.println(e);
	}
}
public static void main(String args[]) {
	if (args.length == 0) {
		System.out.println("Usage: java ...JDict [-h host] [-p port] [-d database] [-m] word");
		System.exit(0);
	}
	JDict c = new JDict();
	for (int i = 0; i < args.length-1; i++){
		if (args[i].equals("-h")) {
			c.setHost(args[++i]);
		} else if (args[i].equals("-p")) {
			try {
				c.setPort(Integer.parseInt(args[++i]));
			} catch (Throwable t) {}
		} else if (args[i].equals("-d")) {
			c.setDb(args[++i]);
		} else if (args[i].equals("-m")) {
			c.setMatch(true);
		}
	}
	c.lookup(args);
}
/**
 * Insert the method's description here.
 * Creation date: (24.02.2002 16:47:00)
 * @param newDb java.lang.String
 */
public void setDb(java.lang.String newDb) {
	db = newDb;
}
/**
 * Insert the method's description here.
 * Creation date: (24.02.2002 16:42:15)
 * @param newHost java.lang.String
 */
public void setHost(java.lang.String newHost) {
	host = newHost;
}
/**
 * Insert the method's description here.
 * Creation date: (24.02.2002 16:42:15)
 * @param newMatch boolean
 */
public void setMatch(boolean newMatch) {
	match = newMatch;
}
/**
 * Insert the method's description here.
 * Creation date: (24.02.2002 16:42:15)
 * @param newPort int
 */
public void setPort(int newPort) {
	port = newPort;
}
}
