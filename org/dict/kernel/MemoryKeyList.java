package org.dict.kernel;

import java.io.IOException;
public class MemoryKeyList extends KeyList {
	private byte[] fData;
/**
 * MemoryKeyList constructor comment.
 */
protected MemoryKeyList() {
	super();
}
/**
 * MemoryKeyList constructor comment.
 */
public MemoryKeyList(String fileName) throws IOException {
	super();
	initialize(getData(fileName));
}
/**
 * Insert the method's description here.
 * Creation date: (22.06.2001 10:34:16)
 * @return byte[]
 */
public byte[] getData() {
	return fData;
}
/**
 * Insert the method's description here.
 * Creation date: (22.06.2001 10:34:16)
 * @param newData byte[]
 */
public void setData(byte[] newData) {
	fData = newData;
}

	/**
	 * Returns the element at the specified position in this list.
	 *
	 * @param index index of element to return.
	 * 
	 * @return the element at the specified position in this list.
	 * @throws IndexOutOfBoundsException if the given index is out of range
	 * 		  (<tt>index &lt; 0 || index &gt;= size()</tt>).
	 */
public Object get(int index) {
	int beg = getIndexes()[index];
	int end = getData().length;
	if (index < getIndexes().length-1) {
		end = getIndexes()[index+1];
	}
	return createKey(getData(), beg, end-beg);
}

void initialize(byte[] b ) {
	int[] arr = getLineMarkers(b);
	setIndexes(arr);
	setData(b);
}

public void sort(IComparator c) throws java.io.IOException {
	Object[] all = new Object[size()];
	for (int i = 0; i < all.length; i++){
		all[i] = get(i);
	}
	ListUtil.sort(all, c);
	java.io.ByteArrayOutputStream bout = new java.io.ByteArrayOutputStream(getData().length);
	java.io.OutputStreamWriter w = new java.io.OutputStreamWriter(bout, getEncoding());
	for (int i = 0; i < all.length; i++){
		IKey k = (IKey) all[i];
		w.write(k.getKey());
		w.write(IKey.TAB);
		w.write(k.getOffset());
		w.write(IKey.TAB);
		w.write(k.getLength());
		w.write('\n');
	}
	w.flush();
	initialize(bout.toByteArray());
}
}
