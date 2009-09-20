package org.dict.kernel;

import java.io.*;
/**
 * Creation date: (11.03.2002 16:28:49)
 * @author: duc
 */
public class PlainPrinter implements IAnswerPrinter {
/**
 * PlainPrinter constructor comment.
 */
public PlainPrinter() {
	super();
}
public void printAnswer(IRequest req, IAnswer a, PrintWriter out) throws IOException {
	out.println(a.getDefinition());
}
public void printAnswer(IRequest req, IAnswer a, boolean matches, PrintWriter out) throws IOException {
	if (a.getDefinition() != null) {
		out.println(a.getDefinition());
	} else {
		out.println("No definition found");
	}
	out.println();
	if (matches) {
		printAdjacentWords(a, out);
	}
}
public static void printAnswers(IDictEngine e, IRequest req, IAnswer[] a, boolean matches, PrintWriter out) throws IOException {
	if (a == null || a.length == 0) {
		out.println("Nothing found");
	}
	//out.println("Search results for "+req.getParameter("word"));
	//out.println();
	for (int i = 0; i < a.length; i++){
		//out.println("Reply from "+a[i].getDatabase().getName());
		//out.println();
		IAnswerPrinter p = a[i].getDatabase().getPlainPrinter();
		p.printAnswer(req, a[i], matches, out);
	}
}
private static void printAdjacentWords(IAnswer a, PrintWriter out) throws IOException {
	IWordList neighbors = a.getAdjacentWords();
    out.println(neighbors.getDescription()+":");
    for (int k = 0; k < neighbors.getWordPositions().length; k++) {
        String sep = k == 10 ? "\n" : " | ";
        out.print(neighbors.getWordPositions()[k].getKey() + sep);
    }
    out.println('\n');
}
}
