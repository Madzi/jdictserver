/*
 * Created on 23.06.2004
 *
 */
package org.dict.kernel;

/**
 * @author duc
 *
 */
public class GermanMorphAnalyzer implements org.dict.kernel.IMorphAnalyzer {
	public java.lang.String[] getPossibleBases(String word) {
		if (word.endsWith("es") || word.endsWith("en") | word.endsWith("em")) {
			return new String[]{word.substring(0, word.length()-1), word.substring(0, word.length()-2)};
		}
		if (word.endsWith("s") || word.endsWith("e") || word.endsWith("n")) {
			return new String[]{word.substring(0, word.length()-1)};
		}
		return new String[0];
	}
}
