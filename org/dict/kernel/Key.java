package org.dict.kernel;

public class Key implements IKey {
	private String fKey, fOffset, fLength;
	private String fComparableKey;
/**
 * Contruct a dummy key, used for comparisons only
 */
public Key(String k, String off, String len) {
	super();
	fKey = k;
	fOffset = off;
	fLength = len;
}
public String getComparableKey(IComparator c) {
	if (fComparableKey == null) {
		fComparableKey = c.getComparableKey(fKey);
	}
	return fComparableKey;
}
/**
 * Insert the method's description here.
 * Creation date: (03.09.01 22:18:17)
 * @return java.lang.String
 */
public java.lang.String getKey() {
	return fKey;
}
/**
 * Insert the method's description here.
 * Creation date: (03.09.01 22:18:17)
 * @return java.lang.String
 */
public java.lang.String getLength() {
	return fLength;
}
/**
 * Insert the method's description here.
 * Creation date: (03.09.01 22:18:17)
 * @return java.lang.String
 */
public java.lang.String getOffset() {
	return fOffset;
}
/**
 * Insert the method's description here.
 * Creation date: (03.09.01 22:18:17)
 * @param newKey java.lang.String
 */
public void setKey(java.lang.String newKey) {
	fKey = newKey;
}
/**
 * Insert the method's description here.
 * Creation date: (03.09.01 22:18:17)
 * @param newLength java.lang.String
 */
public void setLength(java.lang.String newLength) {
	fLength = newLength;
}
/**
 * Insert the method's description here.
 * Creation date: (03.09.01 22:18:17)
 * @param newOffset java.lang.String
 */
public void setOffset(java.lang.String newOffset) {
	fOffset = newOffset;
}
public String toString() {
	return fKey;
}
}
