package org.dict.kernel;

import java.util.Vector;
//import java.util.StringTokenizer;
//import com.sun.java.util.collections.*;
import java.io.*;
/**
 * Insert the type's description here.
 * Creation date: (29.07.01 12:47:32)
 * @author: Administrator
 */
public class DictEngine implements IDictEngine {
	IDatabase[] fDatabases = new IDatabase[0];
/**
 * DatabaseFactory constructor comment.
 */
public DictEngine() {
	super();
}
public IAnswer[] define(String db, String word) {
	IAnswer[] ans = defineMatch(db, word, null, true, IDatabase.STRATEGY_NONE);
	Vector v = new Vector(ans.length);
	for (int i = 0; i < ans.length; i++){
		if (ans[i].getDefinition() != null) v.addElement(ans[i]);
	}
	IAnswer[] ret = new IAnswer[v.size()];
	v.copyInto(ret);
	return ret;
}
public static byte[] getData(String fileName) throws IOException {
	BufferedInputStream fis = null;
	try {
	fis = new BufferedInputStream(new FileInputStream(fileName));
	ByteArrayOutputStream bout = new ByteArrayOutputStream();
	byte[] b = new byte[1024];
	int len;
	while ((len = fis.read(b)) > 0) {
		bout.write(b, 0, len);
	}
	return bout.toByteArray();
	} finally {
		try {
			fis.close();
			fis = null;
		} catch (Throwable t) {}
	}
}
/**
 * Insert the method's description here.
 * Creation date: (29.07.01 23:31:38)
 * @return org.dict.IDatabase[]
 */
public IDatabase[] getDatabases() {
	return fDatabases;
}
/**
public void setDatabases(IDatabase[] newDatabases) {
	fDatabases = newDatabases;
}
 */

/**
 * defineMatch method comment.
 */
public IAnswer[] defineMatch(String db, String word, String pos, boolean define, int strategy) {
	if (pos != null) {
		int idx = Integer.parseInt(pos);
		IDatabase d = findDatabase(db);
		if (d == null) {
			throw new RuntimeException("Database does not exist: "+db);
		}
		return new IAnswer[]{d.defineMatch(idx, define, IDatabase.STRATEGY_NONE)};
	}
	if (word == null) return null;
	String s = word.trim().toLowerCase();
	if (db.equals("*")) return defineMatchAll(s, define, strategy);
	if (db.equals("!")) return defineMatchAny(s, define, strategy);
	IDatabase d = findDatabase(db);
	if (d == null) {
		throw new RuntimeException("Database does not exist: "+db);
	}
	IAnswer a = d.defineMatch(s, define, strategy);
	return new IAnswer[]{a};
}

IAnswer[] defineMatchAll(String word, boolean define, int strategy) {
	java.util.Vector v = new java.util.Vector();
	IDatabase[] all = getDatabases();
	for (int i = 0; i < all.length; i++){
		IAnswer a = all[i].defineMatch(word, define, strategy);
		v.addElement(a);
	}
	IAnswer[] ret = new IAnswer[v.size()];
	v.copyInto(ret);
	return ret;
}

IAnswer[] defineMatchAny(String word, boolean define, int strategy) {
	//System.out.println("Lookup "+word);
	IDatabase[] all = getDatabases();
	for (int i = 0; i < all.length; i++){
		IAnswer a = all[i].defineMatch(word, define, strategy);
		if (a.getDefinition() != null) {
			return new IAnswer[]{a};
		}
	}
	return new IAnswer[0];
}

protected IDatabase findDatabase(String id) {
	IDatabase[] all = getDatabases();
	for (int i = 0; i < all.length; i++){
		if (all[i].getID().equalsIgnoreCase(id)) {
			return all[i];
		}
	}
	return null;
}

public IAnswer[] match(String db, String word, int strategy) {
	return defineMatch(db, word, null, false, strategy);
}

public IAnswer[] lookup(IRequest req) {
    String word = req.getParameter("word");
    String pos = req.getParameter("pos");
    String[] db = req.getParameterValues("db");
    int strat = IDatabase.STRATEGY_NONE;
    if (word != null && word.length() > 2) {
		word = word.trim();
    	if (word.startsWith("*") && word.endsWith("*")) {
    		word = word.substring(1, word.length()-1);
			strat = IDatabase.STRATEGY_SUBSTRING;
    	} else if (word.startsWith("*")) {
			word = word.substring(1);
			strat = IDatabase.STRATEGY_SUFFIX;
		} else if (word.endsWith("*")) {
			word = word.substring(0, word.length()-1);
			strat = IDatabase.STRATEGY_PREFIX;
		}
	}
    if (db == null || db.length == 0) {
        return this.defineMatch("*", word, pos, true, strat);
    }
    IAnswer[] answers = null;
    java.util.Vector v = new java.util.Vector();
    for (int i = 0; i < db.length; i++) {
	    try {
	        answers = this.defineMatch(db[i], word, pos, true, strat);
	    } catch (Throwable t) {
	    	StringWriter sw = new StringWriter();
	    	t.printStackTrace(new PrintWriter(sw));
		    StringBuffer sb = new StringBuffer();
		    sb.append("<pre>\n").append(sw.toString());
		    sb.append("\nAvailable dictionaries: ");
		    for (int k = 0; k < getDatabases().length; k++){
		    	sb.append(" ").append(getDatabases()[k].getID());
		    }
			sb.append("</pre>");
		    IAnswer a = new Answer(this.getDatabases()[0], word, -1, sb.toString());
		    answers = new IAnswer[]{a};
	    }
        for (int k = 0; k < answers.length; k++) {
            v.addElement(answers[k]);
        }
    }
    answers = new IAnswer[v.size()];
    v.copyInto(answers);
    return answers;
}
public synchronized void addDatabase(IDatabase db) {
	for (int i = 0; i < fDatabases.length; i++) {
		if (fDatabases[i].equals(db)) {
			return;
		}
	}
	IDatabase[] a = new IDatabase[fDatabases.length+1];
	System.arraycopy(fDatabases, 0, a, 0, fDatabases.length);
	a[a.length-1] = db;
	fDatabases = a;
}
public synchronized void removeDatabase(IDatabase db) {
	Vector v = new Vector();
	for (int i = 0; i < fDatabases.length; i++) {
		if (!fDatabases[i].equals(db)) {
			v.addElement(fDatabases[i]);
		}
	}
	if (v.size() == fDatabases.length) {
		return;
	}
	fDatabases = new IDatabase[v.size()];
	v.copyInto(fDatabases);
	v = null;
}
}
