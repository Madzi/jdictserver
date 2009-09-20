package org.dict.kernel;

public class BASE64Converter {
	private static String codeString = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	private static int[] codeTable;
/**
 * BASE64NumberConverter constructor comment.
 */
public BASE64Converter() {
	super();
}
private static String getCodeString() {
	return codeString;
}
private static int[] getCodeTable() {
	if (codeTable == null) {
		codeTable = new int[128];
		for (int i = 0; i < codeTable.length; i++) {
			codeTable[i] = -1;
		}
		for (int i = 0; i < codeString.length(); i++) {
			char c = codeString.charAt(i);
			codeTable[(int) c] = i;
		}
	}
	return codeTable;
}
public static long parse(String s) throws NumberFormatException {
	try {
		long ret = 0;
		byte[] b = s.getBytes();
		for (int i = b.length-1; i >= 0; i--){
			int k = getCodeTable()[b[i]];
			if (k == -1) {
				throw new NumberFormatException(s);
			}
			int pow = b.length-1-i;
			ret += k << 6*pow;
		}
   		return ret;
	} catch (Throwable t) {
		throw new NumberFormatException(t.toString()+": "+s);
	}
}
public static String toString(long i) {
	int shift = 6;
	char[] buf = new char[64];
	int charPos = 64;
	int radix = 1 << shift;
	long mask = radix - 1;
	do {
	    buf[--charPos] = getCodeString().charAt((int)(i & mask));
	    i >>>= shift;
	} while (i != 0);
	return new String(buf, charPos, (64 - charPos));
}
}
