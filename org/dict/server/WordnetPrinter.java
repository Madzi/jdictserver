package org.dict.server;

import java.util.*;
import org.dict.kernel.*;
import java.io.*;
//import duc.util.*;
/**
 * Insert the type's description here.
 * Creation date: (9/10/01 2:10:47 PM)
 * @author: Administrator
 */
public class WordnetPrinter extends org.dict.kernel.HTMLPrinter {
private boolean isBOL(String token) {
	if (token.endsWith(":") && Character.isDigit(token.charAt(0))) return true;
	if (token.equals("n") || token.equals("v")) return true;
	if (token.equals("adj") || token.equals("adv")) return true;
	return false;
}
protected void addLink(StringBuffer sb, String line, String word, String base) {
	if (line == null | line.length() < 2) {
		sb.append(line);
		return;
	}
	StringTokenizer st = new StringTokenizer(line, " ,;!?()[]\"'`´\t\n\r\f", true); // period not always a delimiter: etc., e.g.
	String s;
	while (st.hasMoreTokens()) {
		s = st.nextToken();
		if (isBOL(s)) {
			sb.append("<p>");
			sb.append(s);
		} else if ((s.length() > 1) && !s.equals(word) && !s.endsWith(":")) {
			sb.append("<a href=\"");
			sb.append(base);
			sb.append("&word=");
			sb.append(s);
			sb.append("\">");
			sb.append(s);
			sb.append("</a>");
		}
		else {
			sb.append(s);
		}
	}
}
	protected void printDefinition(IAnswer a, PrintWriter out, String base) throws IOException {
		String def = a.getDefinition();
		if (def != null) {
			def = def.trim();
			String key = a.getKey();
			if (def.startsWith(key)) {
				out.print(key);
				out.println(" &nbsp; &nbsp;<a href=\"javascript:audio1('"+key+"');\"><img src=\"audio.gif\" border=0></a>");
				def = def.substring(key.length()).trim();
			}
			String lk = makeLink(a.getKey(), def, base);
			out.println(lk);
		} else {
			out.println("No definition found for \""+a.getKey()+"\"");
		}
	}
}
