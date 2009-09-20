package org.dict.kernel;

/**
 * Insert the type's description here.
 * Creation date: (28.08.01 23:39:32)
 * @author: Administrator
 */
public class EnglishMorphAnalyzer implements IMorphAnalyzer {
/**
 * EnglishMorphAnalyzer constructor comment.
 */
public EnglishMorphAnalyzer() {
	super();
}
/**
 * getPossibleBases method comment.
 */
public String[] getPossibleBases(String word) {
	if (word.endsWith("es")) {
		if (word.endsWith("ies")) {
			String s = word.substring(0, word.length()-3);
			return new String[]{s+"y", s+"ie", s+"i"};
		}
		return new String[]{word.substring(0, word.length()-1), word.substring(0, word.length()-2)};
	}
	if (word.endsWith("s")) {
		return new String[]{word.substring(0, word.length()-1)};
	}
	if (word.endsWith("ing") || word.endsWith("est")) {
		String s = word.substring(0, word.length()-3);
		return new String[]{s+"e", s};
	}
	if (word.endsWith("ed") || word.endsWith("er")) {
		String s = word.substring(0, word.length()-2);
		return new String[]{s+"e", s};
	}
	if (word.endsWith("men")) {
		return new String[]{word.substring(0, word.length()-3)+"man"};
	}
	return new String[0];
}
}
