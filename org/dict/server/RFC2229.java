package org.dict.server;

//import com.sun.java.util.collections.*;
import java.io.*;
import org.dict.kernel.*;
/**
 * Insert the type's description here.
 * Creation date: (28.07.01 11:45:40)
 * @author: Administrator
 */
public class RFC2229 implements Runnable {
	public final static String CRLF = "\r\n";
	private java.net.Socket fSocket;
	private IDictEngine fEngine;
/**
 * RFC2229 constructor comment.
 */
public RFC2229(IDictEngine engine, java.net.Socket s) {
	super();
	this.fEngine = engine;
	this.fSocket = s;
}
IDictEngine getEngine() {
	return fEngine;
}
/**
 * Insert the method's description here.
 * Creation date: (28.07.01 16:08:45)
 * @return java.io.OutputStream
 */
public java.io.OutputStream getOut() throws IOException {
	return fSocket.getOutputStream();
}
int processBanner() throws IOException {
	StringBuffer sb = new StringBuffer("220 dict.org ");
	sb.append(JDictd.SERVER_NAME).append(" ");
	sb.append(JDictd.SERVER_VERSION).append(" <");
	sb.append(System.currentTimeMillis());
	sb.append("@dict.org>");
	return sendLine(sb.toString());
}
int processClient() throws IOException {
	return sendLine("250 ok");
}
public int processCommand(String cmd) throws IOException {
	// RFC2229 3.1 - Initial Connection
	if (cmd == null) {
		return processBanner();
	}
	// RFC2229 3.2 - DEFINE
	if (cmd.toUpperCase().startsWith("DEFINE")) {
		return processDefine(cmd);
	}
	// RFC2229 3.3 - MATCH
	if (cmd.toUpperCase().startsWith("MATCH")) {
		return processMatch(cmd);
	}
	// RFC2229 3.5 - SHOW command
	if (cmd.toUpperCase().startsWith("SHOW")) {
		return processShow(cmd);
	}
	// RFC2229 3.6
	if (cmd.toUpperCase().startsWith("CLIENT")) {
		return sendLine("250 OK");
	}
	// RFC2229 3.7
	if (cmd.equalsIgnoreCase("STATUS")) {
		return processStatus();
	}
	// RFC2229 3.8
	if (cmd.equalsIgnoreCase("HELP")) {
		return processHelp();
	}
	// RFC2229 3.9
	if (cmd.equalsIgnoreCase("QUIT")) {
		return processQuit();
	}
	// RFC2229 3.10
	if (cmd.equalsIgnoreCase("OPTION")) {
		return sendLine("250 OK");
	}
	return sendLine("500 Syntax error, command not recognized");
}
int processDefine(String cmd) throws IOException {
	Logger.getInstance().log(cmd);
	int defSep = cmd.indexOf(' ');
	int dbSep = cmd.indexOf(' ', defSep+1);
	String def = cmd.substring(0, defSep).trim();
	String db = cmd.substring(defSep+1, dbSep).trim();
	if (db.startsWith("\"") && db.endsWith("\"")) {
		db = db.substring(1, db.length()-1);
	}
	String what = cmd.substring(dbSep+1).trim();
	if (what.startsWith("\"") && what.endsWith("\"")) {
		what = what.substring(1, what.length()-1);
	}
	IAnswer[] m = getEngine().define(db, what);
	if (m == null) {
		sendLine("550 Invalid database, use \"SHOW DB\" for list of databases");
		//sendLine("250 Command complete ");
		return 0;
	}
	if (m.length == 0) {
		sendLine("552 No match");
		return 0;
	}
	sendLine("150 "+m.length+" definitions found: list follows");
	for (int i = 0; i < m.length; i++){
		String id = m[i].getDatabase().getID();
		String name = m[i].getDatabase().getName();
		String res = m[i].getDefinition();
		sendLine("151 \""+what+"\" "+id+" \""+name+"\": text follows");
		sendLine(res);
		sendLine(".");
	}
	sendLine("250 Command complete ");
	return 0;
}
int processHelp() throws IOException {
	sendLine("113 Help text follows");
	sendLine("DEFINE database word            look up word in database");
	sendLine("MATCH database strategy word    match word in database using strategy");
	sendLine("The MATCH command returns up to 20 matches");
	sendLine(".");
	sendLine("250 Command complete");
	return 0;
}
int processMatch(String cmd) throws IOException {
	int matchSep = cmd.indexOf(' ');
	int dbSep = cmd.indexOf(' ', matchSep+1);
	int stratSep = cmd.indexOf(' ', dbSep+1);
	String match = cmd.substring(0, matchSep).trim();
	String db = cmd.substring(matchSep+1, dbSep).trim();
	String strat = cmd.substring(dbSep+1, stratSep).trim();
	String what = cmd.substring(stratSep+1).trim();
	if (what.startsWith("\"") && what.endsWith("\"")) {
		what = what.substring(1, what.length()-1);
	}
	int strategy = IDatabase.STRATEGY_PREFIX;
	if ("exact".equals(strat)) {
		strategy = IDatabase.STRATEGY_EXACT;
	} else if ("suffix".equals(strat)) {
		strategy = IDatabase.STRATEGY_SUFFIX;
	} else if ("substr".equals(strat)) {
		strategy = IDatabase.STRATEGY_SUBSTRING;
	}
	IAnswer[] m = getEngine().match(db, what, strategy);
	if (m == null) {
		sendLine("550 Invalid database, use \"SHOW DB\" for list of databases");
		return 0;
	}		
	StringBuffer sb = new StringBuffer();
	int count = 0;
	for (int i = 0; i < m.length; i++){
		String id = m[i].getDatabase().getID();
		IWordPosition[] res = m[i].getAdjacentWords().getWordPositions();
		count += res.length;
		for (int i2 = 0; i2 < res.length; i2++){
			sb.append(id+" \""+res[i2].getKey()+"\"");
			sb.append(CRLF);
		}
	}
	if (count == 0) {
		sendLine("552 No match");
		//sendLine("250 Command complete");
		return 0;
	}
	sendLine("152 "+count+" matches found: list follows");
	getOut().write(sb.toString().getBytes());
	sendLine(".");
	sendLine("250 Command complete");
	return 0;
}
int processQuit() throws IOException {
	return sendLine("221 Closing connection");
}
int processShow(String cmd) throws IOException {
	java.util.StringTokenizer st = new java.util.StringTokenizer(cmd);
	String show = st.nextToken();
	String what = st.nextToken().toUpperCase();
	if (what.equals("DB") || what.equals("DATABASES")) {
		return processShowDB();
	}
	if (what.equals("INFO")) {
		String db = st.nextToken();
		return processShowInfo(db);
	}
	if (what.equals("STRAT") || what.equals("STRATEGIES")) {
		// sendLine("555 No strategies available");
		sendLine("111 2 strategies present: list follows");
		sendLine("exact \"Match words exactly\"");
		sendLine("prefix \"Match word prefixes\"");
		sendLine(".");
		sendLine("250 Command complete");
		return 0;
	}
	if (what.equals("SERVER")) {
		sendLine("114 server information follows");
		sendLine("Java implementation of the DICT protocol (RFC2229)");
		sendLine("Copyright 2001 Ho Ngoc Duc (duc@informatik.uni-leipzig.de)");
		sendLine(".");
		sendLine("250 Command complete");
	}
	return 0;
}
int processShowDB() throws IOException {
	IDatabase[] all = getEngine().getDatabases();
	sendLine("110 "+all.length+" databases present - list follows");
	for (int i = 0; i < all.length; i++){
		sendLine(all[i].getID()+" \""+all[i].getName()+"\"");
	}
	sendLine(".");
	sendLine("250 Command complete");
	return 0;
}
int processShowInfo(String db) throws IOException {
	IDatabase[] all = getEngine().getDatabases();
	IDatabase it = null;
	for (int i = 0; i < all.length; i++){
		if (all[i].getID().equalsIgnoreCase(db)) {
			it = all[i];
			break;
		}
	}
	sendLine("112 database information follows");
	if (it != null) {
		sendLine(it.getDescription());
	} else {
		sendLine("Invalid database, use \"SHOW DB\" for list of databases");
	}
	sendLine(".");
	sendLine("250 Command complete");
	return 0;
}
int processStatus() throws IOException {
	return sendLine("210 Not supported");
}
public void run() {
	try {
		BufferedReader in = new BufferedReader(new InputStreamReader(fSocket.getInputStream(), "UTF-8"));
		String inputLine;
		processCommand(null);
		while ((inputLine = in.readLine()) != null) {
			//System.out.println(inputLine);
			processCommand(inputLine);
			if ("QUIT".equalsIgnoreCase(inputLine)) {
				break;
			}
		}
	} catch (Exception e) {
		trace(e.toString());
	} finally {
		try {
			fSocket.close();
		} catch (Throwable t) {
			trace(t.toString());
		}
	}
}
public int sendLine(String line) throws IOException {
	if (line == null) return 0;
	sendLine(line, getOut());
	return line.length() + 2;
}
public static void sendLine(String line, OutputStream out) throws IOException {
	out.write(line.getBytes("UTF-8"));
	out.write(CRLF.getBytes("UTF-8"));
}
void trace(String s) {
	System.out.println(s);
}
}
