package org.dict.zip;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class DictZipInputStream extends InflaterInputStream {

	/**
	 * CRC-32 for uncompressed data.
	 */
	protected CRC32 crc = new CRC32();

	/**
	 * Indicates end of input stream.
	 */
	protected boolean eos;

	/**
	 * Creates a new input stream with a default buffer size.
	 * 
	 * @param in
	 *            the input stream
	 * @exception IOException
	 *                if an I/O error has occurred
	 */
	public DictZipInputStream(InputStream in) throws IOException {
		this(in, 512);
	}

	/**
	 * Creates a new input stream with the specified buffer size.
	 * 
	 * @param in
	 *            the input stream
	 * @param size
	 *            the input buffer size
	 * @exception IOException
	 *                if an I/O error has occurred
	 */
	public DictZipInputStream(InputStream in, int size) throws IOException {
		super(in, new Inflater(true), size);
		// readHeader();
		// crc.reset();
	}

	/**
	 * Closes the input stream.
	 * 
	 * @exception IOException
	 *                if an I/O error has occurred
	 */
	@Override
	public void close() throws IOException {
		inf.end();
		in.close();
		eos = true;
	}

	/**
	 * Reads uncompressed data into an array of bytes. Blocks until enough input
	 * is available for decompression.
	 * 
	 * @param buf
	 *            the buffer into which the data is read
	 * @param off
	 *            the start offset of the data
	 * @param len
	 *            the maximum number of bytes read
	 * @return the actual number of bytes read, or -1 if the end of the
	 *         compressed input stream is reached
	 * @exception IOException
	 *                if an I/O error has occurred or the compressed input data
	 *                is corrupt
	 */
	@Override
	public int read(byte[] buf, int off, int len) throws IOException {
		if (eos) {
			return -1;
		}
		len = super.read(buf, off, len);
		if (len == -1) {
			// readTrailer();
			eos = true;
		} else {
			crc.update(buf, off, len);
		}
		return len;
	}

	public final void readFully(byte b[]) throws IOException {
		readFully(b, 0, b.length);
	}

	public final void readFully(byte b[], int off, int len) throws IOException {
		int n = 0;
		while (n < len) {
			int count = read(b, off + n, len - n);
			if (count < 0)
				throw new EOFException();
			n += count;
		}
	}

	public DictZipHeader readHeader() throws IOException {
		DictZipHeader h = new DictZipHeader();
		DictZipHeader.readHeader(h, in, crc);
		crc.reset();
		return h;
	}
}
