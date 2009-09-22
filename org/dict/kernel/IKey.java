package org.dict.kernel;

/**
 * Insert the type's description here. Creation date: (29.07.01 13:06:23)
 * 
 * @author: Administrator
 */
public interface IKey {
	char TAB = '\t';

	/**
	 * Return the dictionary key.
	 */
	String getKey();

	String getLength();

	String getOffset();
}
