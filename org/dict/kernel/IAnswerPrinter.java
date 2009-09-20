/*
 * Created on 03.09.2003
 *
 */
package org.dict.kernel;

import java.io.PrintWriter;
import java.io.IOException;

/**
 * @author duc
 *
 */
public interface IAnswerPrinter {
	void printAnswer(IRequest req, IAnswer a, boolean matches, PrintWriter out) throws IOException;
}
