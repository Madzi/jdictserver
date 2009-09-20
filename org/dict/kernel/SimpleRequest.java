package org.dict.kernel;

import java.io.*;
import java.util.*;
/**
 * Insert the type's description here.
 * Creation date: (10.03.2002 16:59:36)
 * @author: 
 */
public class SimpleRequest implements IRequest {
	private String[] fParameters;
	private String fRequestURI, requestString;

	public SimpleRequest(String uri, String req) {
		super();
		fRequestURI = uri;
		requestString = req;
		fParameters = parseQuery(req);
	}
	public static String decode(String s) 
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream(s.length());
		
		for (int i = 0; i < s.length(); i++) 
		{
			char c = s.charAt(i);
			if (c == '+') 
			{
				out.write(' ');
			}
			else if (c == '%') 
			{
				int c1 = Character.digit(s.charAt(++i), 16);
				int c2 = Character.digit(s.charAt(++i), 16);
				out.write((char) (c1 * 16 + c2));
			}
			else 
			{
				out.write(c);
			}
		} // end for
		try {
			return out.toString("UTF-8");
		} catch (Exception e) {
			return out.toString();
		}
	}
	public String getParameter(String param) {
		String[] in = getParameters();
		for (int i = 0; i < in.length-1; i++){
			if (in[i].equals(param)) {
				return in[i+1];
			}
		}
		return null;
	}
	public String[] getParameters() {
		return fParameters;
	}
	public String toString() {
		return requestString;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (10.03.2002 16:59:53)
	 * @return java.lang.String[]
	 * @param param java.lang.String
	 */
	public java.lang.String[] getParameterValues(String param) {
		String[] in = getParameters();
		Vector v = new Vector();
		for (int i = 0; i < in.length-1; i++){
			if (in[i].equals(param)) {
				v.addElement(in[i+1]);
			}
		}
		String[] ret = new String[v.size()];
		v.copyInto(ret);
		return ret;
	}
	/**
	 * Creation date: (10.03.2002 17:10:42)
	 * @return java.lang.String
	 */
	public java.lang.String getRequestURI() {
		return fRequestURI;
	}
	public static String[] parseQuery(String s)
	{
		return parseQuery(s, "&", "=");
	}
	public static String[] parseQuery(String s, String delim, String rel)
	{
		String str = s;
		if (s == null) str = "";
		java.util.Properties result = new java.util.Properties();
		java.util.Vector v = new java.util.Vector();
		StringTokenizer st = new StringTokenizer(str, delim);
		String current, key, value;
		int sep = 0;
		while (st.hasMoreTokens()) 
		{
			current = st.nextToken();
			sep = current.indexOf(rel);
			if (sep == -1) sep = 0;
			key = decode(current.substring(0, sep));
			value = decode(current.substring(sep+1));
			v.addElement(key);
			v.addElement(value);
		}
		String[] ret = new String[v.size()];
		v.copyInto(ret);
		return ret;
	}
}
