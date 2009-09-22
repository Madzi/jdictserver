package org.dict.zip;

/**
 * Insert the type's description here. Creation date: (28.07.01 22:57:34)
 * 
 * @author: Administrator
 */
public class DictZipDataAccessor implements org.dict.kernel.IDataAccessor {
	String fDatafile;
	DictZipHeader fHeader;

	/**
	 * DictZipDatabase constructor comment.
	 */
	public DictZipDataAccessor(String fileName) {
		super();
		fDatafile = fileName;
	}

	public String getDatafile() {
		return fDatafile;
	}

	/**
	 * Insert the method's description here. Creation date: (28.07.01 23:06:21)
	 * 
	 * @return org.dict.DictZipHeader
	 */
	public DictZipHeader getHeader() {
		if (fHeader == null) {
			initialize();
		}
		return fHeader;
	}

	void initialize() {
		String s = getDatafile();
		RandomAccessInputStream in = null;
		DictZipInputStream din = null;
		try {
			in = new RandomAccessInputStream(s, "r");
			din = new DictZipInputStream(in);
			fHeader = din.readHeader();
			in.close();
			din.close();
		} catch (java.io.IOException e) {
			throw new RuntimeException("Cannot initialize DICTZIP header: " + e);
		} finally {
			if (din != null) {
				try {
					din.close();
					din = null;
				} catch (Throwable t) {
				}
			}
			if (in != null) {
				try {
					in.close();
					in = null;
				} catch (Throwable t) {
				}
			}
		}
	}

	private byte[] readData(int start, int len) {
		String s = getDatafile();
		RandomAccessInputStream in = null;
		DictZipInputStream din = null;
		try {
			in = new RandomAccessInputStream(s, "r");
			din = new DictZipInputStream(in);
			DictZipHeader h = getHeader();
			int idx = start / h.chunkLength;
			int off = start % h.chunkLength;
			int pos = h.offsets[idx];
			in.seek(pos);
			byte[] b = new byte[off + len];
			din.readFully(b);
			byte[] ret = new byte[len];
			System.arraycopy(b, off, ret, 0, len);
			return ret;
		} catch (java.io.IOException e) {
			System.err.println(e);
			return null;
		} finally {
			try {
				din.close();
				in.close();
			} catch (Throwable t) {
			}
			din = null;
			in = null;
		}

	}

	/**
	 * readData method comment.
	 */
	public byte[] readData(long pos, long len) throws java.io.IOException {
		return readData((int) pos, (int) len);
	}

	/**
	 * Insert the method's description here. Creation date: (28.07.01 23:06:21)
	 * 
	 * @param newHeader
	 *            org.dict.DictZipHeader
	 */
	public void setHeader(DictZipHeader newHeader) {
		fHeader = newHeader;
	}
}
