package org.dict.kernel;

import java.io.*;

/**
 * Insert the type's description here.
 * Creation date: (24.02.2002 14:44:28)
 * @author: 
 */
public class HTMLPrinter implements IAnswerPrinter {
	private static String HEAD, TAIL;
	
	private static void initialize() {
		String RESULT = "<!-- Lookup result -->";
		StringBuffer sb = new StringBuffer();
		sb.append("<html>");
		sb.append("<head>");
		sb.append("<title>Ho Ngoc Duc's Dictionary</title>");
		sb.append("<STYLE type=text/css>");
		sb.append("a:link {color: #0066cc; TEXT-DECORATION: none}");
		sb.append("a:visited {color: #0066cc; TEXT-DECORATION: none}");
		sb.append("</STYLE>");
		sb.append("</head>");
		sb.append("<body>");
		sb.append(RESULT);
		sb.append("</body></html>");
		String template = sb.toString();
		String root = System.getProperty("ROOT", ".");
		File f = new File(root, "result.html");
		if (f.exists()) {
			try {
				FileInputStream fis = new FileInputStream(f);
				byte[] b = new byte[fis.available()];
				fis.read(b);
				fis.close();
				try {
					template = new String(b, "UTF-8");
				} catch (Exception e) {
					template = new String(b);
				}
			} catch (Throwable e) {
			}
		}
		int idx = template.indexOf(RESULT);
		if (idx > 0) {
			HEAD = template.substring(0, idx);
			TAIL = template.substring(idx + RESULT.length());
		} else {
			HEAD = template;
			TAIL = "";
		}
	}
	
	public void appendLink(String s, String base, int pos, StringBuffer sb) {
		sb.append("<a href=\"");
		sb.append(base);
		sb.append("&pos=");
		sb.append(pos);
		sb.append("\">");
		sb.append(s);
		sb.append("</a>");
	}
	public void appendLink(String s, String base, StringBuffer sb) {
		String str = s;
		try {
			str = new String(s.getBytes("UTF-8"));
		} catch (Throwable t) {}
		sb.append("<a href=\"");
		sb.append(base);
		sb.append("&word=");
		sb.append(java.net.URLEncoder.encode(str));
		sb.append("\">");
		sb.append(s);
		sb.append("</a>");
	}
	protected static String getOption(String db, String[] ids) {
		for (int i = 0; i < ids.length; i++){
			if (db.equals(ids[i])) return "\""+db+"\" selected";
		}
		return "\""+db+"\"";
	}
	public static void printAnswers(IDictEngine e, IRequest req, IAnswer[] a, boolean matches, PrintWriter out) throws IOException {
		if (HEAD == null) {
			initialize();
		}
		out.println(HEAD);
		if (a == null || a.length == 0) {
			out.println("<p>Cannot process query "+req);
			out.println("<p>The specified database is not valid, or word not found in the database.");
			out.println("<p><a href=\"index.html\">Please go to main page</a>");
			out.println("<form name=trans><input name=word type=hidden value=\"\"></form>");
			out.println(TAIL);
			return;
		}
		for (int i = 0; i < a.length; i++){
			IAnswerPrinter p = a[i].getDatabase().getHTMLPrinter();
			p.printAnswer(req, a[i], matches, out);
			if (i < a.length - 1) {
				out.println("<hr width=50%>");
			}
		}
		out.println("<form name=trans><input name=word type=hidden value=\""+a[0].getKey()+"\"></form>");
		out.println(TAIL);
	}
	public static void printDefaultForm(IDictEngine e, IRequest req, PrintWriter out) throws IOException {
		String[] ids = req.getParameterValues("db");
		if (ids == null || ids.length == 0) {
			ids = new String[]{"*"};
		}
		String uri = req.getRequestURI();
		IDatabase[] dbs = e.getDatabases();
		out.println("Please enter the word or phrase to look for!");
		out.println("<FORM METHOD=GET action=\""+uri+"\">");
		out.println("<INPUT SIZE=20 NAME=\"word\">");
		out.println("<select name=\"db\">");
		out.println("  <option value="+getOption("*", ids)+">Any");
		out.println("  <option value="+getOption("!", ids)+">First match");
		for (int i = 0; i < dbs.length; i++){
			String db = dbs[i].getName();
			out.println("  <option value="+getOption(dbs[i].getID(), ids)+">"+db);
		}
		out.println("<input type=\"SUBMIT\" value=\"Lookup\">");
		out.println("<input type=\"reset\" value=\"Clear\">");
		out.println("</select>");
		out.println("</FORM>");
	}
	public static String removeSpaces(String s) {
		java.util.StringTokenizer st = new java.util.StringTokenizer(s, " \t\n\r\f", false);
		StringBuffer sb = new StringBuffer(s.length());
		while (st.hasMoreTokens()) {
			sb.append(st.nextToken()).append(' ');
		}
		return sb.toString().trim();
	}
	/**
	 * HTMLFormat constructor comment.
	 */
	public HTMLPrinter() {
		super();
	}
	protected void addLink(StringBuffer sb, String line, String word, String base) {
		sb.append(line);
	}
	
	public String makeLink(String key, String def, String base) {
		if (def == null) return "";
		String delim = "{}";
		java.util.StringTokenizer st = new java.util.StringTokenizer(def, delim, true);
		StringBuffer sb = new StringBuffer(def.length());
		String s;
		while (st.hasMoreTokens()) {
			s = st.nextToken();
			if (s.equals("}")) {
				continue;
			}
			if (s.equals("{")) {
				String tok = st.nextToken();
				s = removeSpaces(tok);
				if (tok.indexOf('\n') >= 0) {
					sb.append("\n\t");
				}
				appendLink(s, base, sb);
			}
			else {
				addLink(sb, s, key, base);
			}
		}
		return sb.toString();
	}
	public void printAnswer(IRequest req, IAnswer a, boolean matches, PrintWriter out) throws IOException {
		out.print("<h3 class=dict align=center><u>");
		out.print(a.getDatabase().getName());
		out.println("</u></h3>\n");
		String uri = req.getRequestURI();
		String base = uri + "?db=" + a.getDatabase().getID();
		printDefinition(a, out, base);
		if (matches) {
			StringBuffer sb = new StringBuffer();
			printWordList(a.getMatches(), a.getPosition(), sb, base);
			printWordList(a.getAdjacentWords(), a.getPosition(), sb, base);
			out.println(sb);
		}
	}

	protected void printDefinition(IAnswer a, PrintWriter out, String base) throws IOException {
		String def = a.getDefinition();
		if (def != null) {
			out.println("<pre>");
			String lk = makeLink(a.getKey(), def, base);
			out.println(lk);
			out.println("</pre>");
		} else {
			out.println("No definition found");
		}
	}
	
	protected boolean areEqual(String s1, String s2) {
		return s1.equalsIgnoreCase(s2);
	}
	
	protected void printWordList(IWordList ls, int pos, StringBuffer sb, String posBase) {
		IWordPosition[] arr = ls.getWordPositions();
		if (arr.length == 0) {
			return;
		}
		sb.append("<p><u>");
		sb.append(ls.getDescription());
		sb.append("</u>:\n");
		for (int k = 0; k < arr.length; k++) {
			if (arr[k].getPosition() == pos) {
				sb.append(arr[k].getKey());
			} else {
				appendLink(arr[k].getKey(), posBase, arr[k].getPosition(), sb);
			}
			if (k < arr.length - 1) {
				sb.append(" | \n");
			}
		}
	}
}
