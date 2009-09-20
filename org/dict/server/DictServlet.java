package org.dict.server;

import java.io.*;

import org.dict.kernel.*;
import org.dict.kernel.IDictEngine;
import javax.servlet.http.*;

public class DictServlet extends HttpServlet {
	org.dict.kernel.IDictEngine fEngine;
/**
 * WordNetServlet constructor comment.
 */
public DictServlet() {
	super();
}
protected void doGet(HttpServletRequest req, HttpServletResponse res)
	throws javax.servlet.ServletException, java.io.IOException {
	handleRequest(req, res);
}
protected void doPost(HttpServletRequest req, HttpServletResponse res)
	throws javax.servlet.ServletException, java.io.IOException {
	handleRequest(req, res);
}
/**
 * handleRequest method comment.
 */
protected void handleRequest(HttpServletRequest request, HttpServletResponse res) throws javax.servlet.ServletException, java.io.IOException {
	String enc = getOutputEncoding(request);
	res.setContentType("text/html; charset="+enc);
	java.io.PrintWriter out = res.getWriter();
	IRequest req = new SimpleRequest(request.getRequestURI(), request.getQueryString());
	IAnswer[] arr = fEngine.lookup(req);
	HTMLPrinter.printAnswers(fEngine, req, arr, true, out);
}
public void init() {
	String base = getServletContext().getRealPath("/");
	String cfg = getInitParameter("config");
	if (!new java.io.File(cfg).isAbsolute()) {
		cfg = new java.io.File(base, cfg).getAbsolutePath();
	}
	fEngine = getEngine(cfg);
	File f = new File(base, "form.html");
	JDictd.createForm(f, fEngine.getDatabases());
}

protected IDictEngine getEngine(String cfg) {
	DictEngine engine = new DictEngine();
	try {
		BufferedReader r = new BufferedReader(new FileReader(cfg));
		String line;
		while ((line = r.readLine()) != null) {
			if (new File(line).exists()) {
				DatabaseFactory.addDatabases(engine, line);
			}
		}
		r.close();
	} catch (Exception e) {
		//e.printStackTrace();
	}
	//return org.dict.server.DatabaseFactory.getEngine(cfg);
	return engine;
}

protected String getOutputEncoding(HttpServletRequest req) {
	return "UTF-8";
}
}
