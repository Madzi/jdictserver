/*
 * Created on 03.09.2003
 *
 */
package org.dict.kernel;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author duc
 * 
 */
public interface IAnswerPrinter {
	void printAnswer(IRequest req, IAnswer a, boolean matches, PrintWriter out) throws IOException;
}
