package org.dict.kernel;

/**
 * Insert the type's description here. Creation date: (11.08.01 10:11:15)
 * 
 * @author: Administrator
 */
public class KeyComparator implements IComparator {
	/**
	 * KeyComparator constructor comment.
	 */
	public KeyComparator() {
		super();
	}

	/**
	 * compare method comment.
	 */
	public int compare(Object o1, Object o2) {
		Key k1 = (Key) o1;
		Key k2 = (Key) o2;
		return k1.getComparableKey(this).compareTo(k2.getComparableKey(this));
	}

	public String getComparableKey(String key) {
		char[] arr = new char[key.length()];
		int len = 0;
		for (int i = 0; i < arr.length; i++) {
			char c = key.charAt(i);
			if (c == ' ' || Character.isLetterOrDigit(c)) {
				arr[len++] = c;
			}
		}
		return new String(arr, 0, len).toLowerCase().trim();
	}
}
