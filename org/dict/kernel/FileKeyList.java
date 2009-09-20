package org.dict.kernel;

import java.io.*;

public class FileKeyList extends KeyList {
	private String fFilename;
	private RandomAccessFile fFile;
/**
 * FileKeyList constructor comment.
 */
protected FileKeyList() {
	super();
}
/**
 * FileKeyList constructor comment.
 */
public FileKeyList(String fileName) throws IOException {
	super();
	initialize(fileName);
}
/**
 * Insert the method's description here.
 * Creation date: (26.06.2001 14:34:05)
 * @return java.io.RandomAccessFile
 */
public java.io.RandomAccessFile getFile() {
	return fFile;
}
/**
 * Insert the method's description here.
 * Creation date: (26.06.2001 14:32:56)
 * @return java.lang.String
 */
public java.lang.String getFilename() {
	return fFilename;
}
void initialize(String fileName) throws IOException {
	byte[] b = getData(fileName);
	int[] arr = getLineMarkers(b);
	setIndexes(arr);
	setFilename(fileName);
}
/**
 * Insert the method's description here.
 * Creation date: (26.06.2001 14:34:05)
 * @param newFile java.io.RandomAccessFile
 */
public void setFile(java.io.RandomAccessFile newFile) {
	fFile = newFile;
}
/**
 * Insert the method's description here.
 * Creation date: (26.06.2001 14:32:56)
 * @param newFilename java.lang.String
 */
public void setFilename(java.lang.String newFilename) {
	fFilename = newFilename;
}
public void shutDown() {
	try {
		getFile().close();
	} catch (IOException e) {
		e.printStackTrace();
	}
}
public void startUp() {
	try {
		setFile(new RandomAccessFile(getFilename(), "r"));
	} catch (IOException e) {
		e.printStackTrace();
	}
}

/**
 * get method comment.
 */
public Object get(int index) {
	try {
		int beg = getIndexes()[index];
		int end = (int) getFile().length();
		if (index < getIndexes().length - 1) {
			end = getIndexes()[index + 1];
		}
		byte[] b = new byte[end - beg];
		getFile().seek(beg);
		getFile().read(b);
		return createKey(b, 0, b.length);
	} catch (IOException e) {
		throw new RuntimeException(e.toString());
	}
}
}
