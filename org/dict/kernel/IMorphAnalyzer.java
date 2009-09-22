package org.dict.kernel;

/**
 * Insert the type's description here. Creation date: (28.08.01 23:37:36)
 * 
 * @author: Administrator
 */
public interface IMorphAnalyzer {
	/**
	 * Insert the method's description here. Creation date: (28.08.01 23:38:53)
	 * 
	 * @return java.lang.String
	 * @param word
	 *            java.lang.String
	 */
	String[] getPossibleBases(String word);
}
