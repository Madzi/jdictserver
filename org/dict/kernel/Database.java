package org.dict.kernel;

import java.util.Vector;
import java.io.*;

/**
 * Insert the type's description here.
 * Creation date: (28.07.01 21:54:33)
 * @author: Administrator
 */
public class Database implements IDatabase {
	public static int MAX_MATCHES = Integer.getInteger("matches", 15).intValue();

	private IComparator fComparator;
	private IDataAccessor fDataAccessor;
	private String fEncoding = "utf-8";
	private IAnswerPrinter fHTMLPrinter;
	private String fID;
	private KeyList fIndex;
	private IMorphAnalyzer fMorphAnalyzer;
	private String fName;
	private IAnswerPrinter fPlainPrinter;
	private File indexFile, dataFile;
	/**
	 * Database constructor comment.
	 */
	public Database() {
		super();
	}
	int compare(IKey k1, IKey k2) {
		return getComparator().compare(k1, k2);
	}
	/**
	 * define method comment.
	 */
	public String define(String word) {
		return defineMatch(word, true, STRATEGY_NONE).getDefinition();
	}
	/**
	 * define method comment.
	 */
	public IAnswer defineMatch(int pos, boolean define, int strat) {
		String def = null;
		KeyList kl = getIndex();
		if (pos < 0 || pos >= kl.size()) {
			pos = Math.abs(pos) % kl.size();
		}
		kl.startUp();
		String word = ((IKey)kl.get(pos)).getKey();
		IWordList neighbors = findAdjacentWords(word, pos, MAX_MATCHES);
		if (define && pos >= 0) {
			byte[] b = readAll(pos, pos);
			try {
				def = new String(b, getEncoding());
			} catch (Throwable e) {
				def = new String(b);
			}
			//System.out.println(def);
		}
		kl.shutDown();
		IAnswer ret = new Answer(this, word, pos, def);
		ret.setAdjacentWords(neighbors);
		return ret;
	}
	/**
	 * define method comment.
	 */
	public IAnswer defineMatch(String word, boolean define, int strat) {
		KeyList kl = getIndex();
		IKey k = new Key(word, null, null);
		kl.startUp();
		int pos = search(k, define);
		IAnswer ret = new Answer(this, word, pos, null);
		IWordList neighbors = findAdjacentWords(word, pos, MAX_MATCHES);
		ret.setAdjacentWords(neighbors);
		if (define && pos >= 0) {
			byte[] b = readDefinitions(pos);
			try {
				ret.setDefinition(new String(b, getEncoding()));
			} catch (Exception e) {
				ret.setDefinition(new String(b));
			}
		}
		int max2 = MAX_MATCHES;
		if (strat == STRATEGY_EXACT) {
			ret.setMatches(findExactMatches(word, pos));
		} else if (strat == STRATEGY_SUFFIX) {
			ret.setMatches(findSuffixMatches(word, max2));
		} else if (strat == STRATEGY_SUBSTRING) {
			ret.setMatches(findSubstringMatches(word, max2));
		} else if (strat == STRATEGY_PREFIX) {
			ret.setMatches(findPrefixMatches(word, pos, max2));
		}
		kl.shutDown();
		return ret;
	}
	
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Database)) {
			return false;
		}
		Database db = (Database) o;
		return dataFile.equals(db.dataFile) && indexFile.equals(db.indexFile); 	}

	IWordList findAdjacentWords(String word, int pos, int max) {
		KeyList kl = getIndex();
		if (pos < 0) {
			pos = -pos - 1;
		}
		int beg = Math.max(0, pos - (max/2));
		int end = Math.min(kl.size()-1, beg+max);
		IWordPosition[] ret = new IWordPosition[end-beg+1];
		for (int i = beg; i <= end; i++){
			IKey k = (IKey) kl.get(i);
			ret[i-beg] = new WordPosition(k.getKey(), i);
		}
		return new WordList("Adjacent words", ret);
	}

	IWordList findExactMatches(String word, int pos) {
		if (pos < 0) {
			return new WordList();
		} else {
			IWordPosition[] arr = new IWordPosition[]{new WordPosition(word, pos)};
			return new WordList("Exact matches", arr);
		}
	}
	IWordList findPrefixMatches(String word, int pos, int max) {
		KeyList kl = getIndex();
		if (pos < 0) {
			pos = -pos - 1;
		}
		Vector ls = new Vector(max);
		String lword = word.toLowerCase();
		boolean match = false;
		do {
			if (pos >= kl.size() || ls.size() >= max) {
				break;
			}
			Key k = (Key) kl.get(pos);
			String w = k.getKey();
			if (w.toLowerCase().startsWith(lword)) {
				ls.addElement(new WordPosition(w, pos));
				match = true;
			} else {
				match = false;
			}
			pos++;
		} while (match);
		IWordPosition[] ret = new IWordPosition[ls.size()];
		ls.copyInto(ret);
		String desc = "Words that begin with \""+word+"\"";
		if (ret.length == max) {
			desc = "First "+max+" words that begin with \""+word+"\"";
		}
		return new WordList(desc, ret);
	}

	IWordList findSubstringMatches(String word, int max) {
		KeyList kl = getIndex();
		Vector ls = new Vector(max);
		String lword = word.toLowerCase();
		int count = 0;
		for (int i = 0; count < max && i < kl.size(); i++) {
			IKey k = (IKey) kl.get(i);
			if (k.getKey().toLowerCase().indexOf(lword) >= 0) {
				ls.addElement(new WordPosition(k.getKey(), i));
				count++;
			}
		}
		IWordPosition[] ret = new IWordPosition[ls.size()];
		ls.copyInto(ret);
		String desc = "Words that contain string \""+word+"\"";
		if (ret.length == max) {
			desc = "First "+max+" words that contain string \""+word+"\"";
		}
		return new WordList(desc, ret);
	}

	IWordList findSuffixMatches(String word, int max) {
		KeyList kl = getIndex();
		Vector ls = new Vector(max);
		String lword = word.toLowerCase();
		int count = 0;
		for (int i = 0; count < max && i < kl.size(); i++) {
			IKey k = (IKey) kl.get(i);
			if (k.getKey().toLowerCase().endsWith(lword)) {
				ls.addElement(new WordPosition(k.getKey(), i));
				count++;
			}
		}
		IWordPosition[] ret = new IWordPosition[ls.size()];
		ls.copyInto(ret);
		String desc = "Words that end with \""+word+"\"";
		if (ret.length == max) {
			desc = "First "+max+" words that end with \""+word+"\"";
		}
		return new WordList(desc, ret);
	}

	/**
	 * Insert the method's description here.
	 * Creation date: (11.08.01 10:09:50)
	 * @return org.dict.kernel.IComparator
	 */
	public IComparator getComparator() {
		return fComparator;
	}

	public IDataAccessor getDataAccessor() {
		return fDataAccessor;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (28.07.01 22:01:41)
	 * @return java.lang.String
	 */
	public java.lang.String getDescription() {
		return define("00-database-info");
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (03.09.01 21:55:54)
	 * @return java.lang.String
	 */
	public java.lang.String getEncoding() {
		return fEncoding;
	}

	public IAnswerPrinter getHTMLPrinter() {
		return fHTMLPrinter;
	}

	public java.lang.String getID() {
		return fID;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (28.07.01 22:01:41)
	 * @return org.dict.IKeyList
	 */
	public KeyList getIndex() {
		return fIndex;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (02.09.01 12:46:21)
	 * @return org.dict.kernel.IMorphAnalyzer
	 */
	public IMorphAnalyzer getMorphAnalyzer() {
		return fMorphAnalyzer;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (29.07.01 22:00:15)
	 * @return java.lang.String
	 */
	public java.lang.String getName() {
		return fName;
	}

	public IAnswerPrinter getPlainPrinter() {
		return fPlainPrinter;
	}


	public int getPosition(String key) {
		return search(new Key(key, null, null), true);
	}
	public int getSize() {
		return getIndex().size();
	}
	
	public int hashCode() {
		return dataFile.hashCode();
	}

	public String initialize(DatabaseConfiguration dc) throws Exception {
		StringBuffer sb = new StringBuffer();
		String id = dc.getId();
		setID(id);
		dataFile = dc.getData();
		indexFile = dc.getIndex();
		if (!dataFile.exists()) {
			throw new Exception("File does not exist: "+dataFile);
		}
		if (!indexFile.exists()) {
			throw new Exception("File does not exist: "+indexFile);
		}
		sb.append("\nCreating database with data file " + dataFile);
		IDataAccessor acc = null;
		if (dataFile.getName().toLowerCase().endsWith(".dz")) {
			acc = new org.dict.zip.DictZipDataAccessor(dataFile.getAbsolutePath());
		} else {
			acc = new FlatDataAccessor(dataFile.getAbsolutePath());
		}
		setDataAccessor(acc);
		String enc = dc.getEncoding();
		setEncoding(enc);
		// Init key list
		KeyList kl = null;
		boolean inMemory = dc.isMemoryIndex();
		if (inMemory) {
			kl = new MemoryKeyList(indexFile.getAbsolutePath());
		} else {
			kl = new FileKeyList(indexFile.getAbsolutePath());
		}
		kl.setEncoding(enc);
		setIndex(kl);
		// Init comparator
		IComparator c = new KeyComparator();
		String comp = dc.getComparator();
		try {
			if (comp != null) {
				c = (IComparator) Class.forName(comp).newInstance();
			}
		} catch (Throwable t) {
			sb.append("\nCannot init comparator: " + t);
		}
		setComparator(c);
		sb.append("\nComparator used for keys: " + c);
		// Init morphological functions
		IMorphAnalyzer ma = null;
		String morphName = dc.getMorph();
		try {
			if (morphName != null) {
				ma = (IMorphAnalyzer) Class.forName(morphName).newInstance();
				sb.append("\nMorphological functions used: " + ma);
			}
		} catch (Throwable t) {
			sb.append("\nNo morphological function available: " + t);
		}
		setMorphAnalyzer(ma);
		// Search database for its name if necessary
		String name = dc.getName();
		if (name == null) {
			String dbName = "00-database-short";
			name = define(dbName);
			if (name == null || !name.startsWith(dbName)) {
				name = id;
			} else {
				name = name.substring(dbName.length()).trim();
			}
		}
		setName(name);
		// Init printer for formatting HTML output
		fHTMLPrinter = new HTMLPrinter();
		String formatter;
		formatter = dc.getHtmlPrinter();
		try {
			if (formatter != null) {
				fHTMLPrinter = (IAnswerPrinter) Class.forName(formatter).newInstance();
			}
		} catch (Throwable t) {
			sb.append("\nCannot init HTMLPrinter: " + t);
		}
		sb.append("\nHTML Printer used for formatting output: " + fHTMLPrinter);
		fPlainPrinter = new PlainPrinter();
		formatter = dc.getPlainPrinter();
		try {
			if (formatter != null) {
				fPlainPrinter = (PlainPrinter) Class.forName(formatter).newInstance();
			}
		} catch (Throwable t) {
			sb.append("\nCannot init PlainPrinter: " + t);
		}
		sb.append("\nPlain Printer used for formatting output: " + fPlainPrinter);
		sb.append("\nDatabase created: " + this);
		return sb.toString();
	}
	/**
	 * match method comment.
	 */
	public IWordList match(String word, int strategy) {
		return defineMatch(word, false, strategy).getMatches();
	}
	byte[] readAll(int start, int end) {
		IKey k = (IKey) getIndex().get(start);
		byte[] ret = new byte[0];
		for (int i = start; i <= end; i++) {
			k = (IKey) getIndex().get(i);
			long off = BASE64Converter.parse(k.getOffset());
			long len = BASE64Converter.parse(k.getLength());
			//System.out.println("Read "+len+" from "+off);
			byte[] b;
			try {
				b = getDataAccessor().readData(off, len);
				//System.out.println("Length: "+new String(b, "UTF-8").length());
			} catch (IOException e) {
				System.out.println(e);
				ByteArrayOutputStream w = new ByteArrayOutputStream();
				e.printStackTrace(new PrintWriter(w));
				b = w.toByteArray();
			}
			byte[] tmp = new byte[ret.length + b.length];
			System.arraycopy(ret, 0, tmp, 0, ret.length);
			System.arraycopy(b, 0, tmp, ret.length, b.length);
			ret = tmp;
		}
		return ret;
	}

	byte[] readDefinitions(int pos) {
		KeyList kl = getIndex();
		IKey k = (IKey) kl.get(pos);
		int start = pos, end = pos;
		//System.out.println(k.getKey());
		while (start > 0) {
			IKey k2 = (IKey) kl.get(start - 1);
			//System.out.println("Go back. Compare "+k.getKey()+" with "+k2.getKey());
			if (compare(k2, k) == 0) {
				start--;
			} else {
				break;
			}
		}
		while (end < kl.size() - 1) {
			IKey k2 = (IKey) kl.get(end + 1);
			//System.out.println("Go forward. Compare "+k.getKey()+" with "+k2.getKey());
			if (compare(k2, k) == 0) {
				end++;
			} else {
				break;
			}
		}
		return readAll(start, end);
	}
	public int search(IKey k, boolean useMorph) {
		if (useMorph) {
			return search(k, getMorphAnalyzer());
		} else {
			return search(k, null);
		}
	}
	public int search(IKey k, IMorphAnalyzer ma) {
		int pos = ListUtil.search(getIndex(), k, getComparator());
		if (pos >= 0 || ma == null) return pos;
		int range = 500;
		int beg = Math.max(0, -pos-range+50);
		int end = Math.min(beg+range, getIndex().size()-1);
		String[] bases = ma.getPossibleBases(k.getKey());
		for (int i = 0; i < bases.length; i++){
			IKey k2 = new Key(bases[i], null, null);
			int p2 = ListUtil.search(getIndex(), k2, getComparator(), beg, end);
			if ( p2 >= 0) return p2;
		}
		return pos;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (11.08.01 10:09:50)
	 * @param newComparator org.dict.kernel.IComparator
	 */
	public void setComparator(IComparator newComparator) {
		fComparator = newComparator;
	}

	public void setDataAccessor(IDataAccessor acc) {
		fDataAccessor = acc;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (03.09.01 21:55:54)
	 * @param newEncoding java.lang.String
	 */
	public void setEncoding(java.lang.String newEncoding) {
		fEncoding = newEncoding;
	}
	public void setHTMLPrinter(IAnswerPrinter printer) {
		fHTMLPrinter = printer;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (7/30/01 6:39:21 PM)
	 * @param newID java.lang.String
	 */
	public void setID(java.lang.String newID) {
		fID = newID;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (28.07.01 22:01:41)
	 * @param newIndex org.dict.KeyList
	 */
	public void setIndex(KeyList newIndex) {
		fIndex = newIndex;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (02.09.01 12:46:21)
	 * @param newMorphAnalyzer org.dict.kernel.IMorphAnalyzer
	 */
	public void setMorphAnalyzer(IMorphAnalyzer newMorphAnalyzer) {
		fMorphAnalyzer = newMorphAnalyzer;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (29.07.01 22:00:15)
	 * @param newName java.lang.String
	 */
	public void setName(java.lang.String newName) {
		fName = newName;
	}

	public void setPlainPrinter(IAnswerPrinter printer) {
		fPlainPrinter = printer;
	}

	public static IDatabase createDatabase(DatabaseConfiguration dc) throws Exception {
		Database ret = new Database();
		String dbClass = dc.getDbClass();
		if (dbClass != null) {
			try {
				ret = (Database) Class.forName(dbClass).newInstance();
			} catch (Throwable t) {
				//DatabaseFactory.log("Instantiation error: " + t);
			}
		}
		try {
			String msg = ret.initialize(dc);
			//DatabaseFactory.log(msg);
		} catch (Throwable t) {
			//t.printStackTrace();
			//DatabaseFactory.log(t.toString());
			throw new Exception(t);
		}
	    return ret;
	}
	public IWordPosition getKey(int pos) {
		int p = pos % getSize();
		String key = ((IKey) getIndex().get(p)).getKey();
		return new WordPosition(key, p);
	}

}
