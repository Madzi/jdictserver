package org.dict.kernel;

/**
 * Insert the type's description here.
 * Creation date: (25.02.2002 18:01:20)
 * @author: 
 */
public class WordPosition implements IWordPosition {
	int fPosition;
	String fKey;
	/**
	 * Neighbor constructor comment.
	 */
	public WordPosition(String k, int pos) {
		super();
		fKey = k;
		fPosition = pos;
	}
	/**
	 * getKey method comment.
	 */
	public java.lang.String getKey() {
		return fKey;
	}
	/**
	 * getPosition method comment.
	 */
	public int getPosition() {
		return fPosition;
	}

	public String toString() {
		return fKey;
	}

}
