package org.dict.kernel;

/**
 * Insert the type's description here.
 * Creation date: (28.07.01 21:39:40)
 * @author: Administrator
 */
public interface IDatabase {
	public static final int STRATEGY_NONE = -1; // Dont look for matches
	public static final int STRATEGY_EXACT = 0;
	public static final int STRATEGY_PREFIX = 1;
	public static final int STRATEGY_SUBSTRING = 2;
	public static final int STRATEGY_SUFFIX = 3;
	/**
	 * This command will look up the specified word in the database
	 */
	String define(String word);
	/**
	 * This command will look up the word with specified index in the database
	 */
	IAnswer defineMatch(int pos, boolean define, int strategy);
	/**
	 * This command will look up the specified word in the database
	 */
	IAnswer defineMatch(String word, boolean define, int strategy);

	IAnswerPrinter getHTMLPrinter();
	IAnswerPrinter getPlainPrinter();
	/**
	 * Show the database info
	 */
	String getDescription();
	/**
	 * Show the database info
	 */
	String getID();
	/**
	 * Show the database info
	 */
	String getName();
	/**
	 * Return the position of a key in the database
	 */
	int getPosition(String key);

	/**
	 * This command searches an index for the dictionary, and reports words
	 * which were found using a particular strategy, together with their positions.
	 */
	IWordList match(String word, int strategy);
	
	/**
	 * Get the key together with its position
	 */
	IWordPosition getKey(int pos);
	
	/**
	 * Get the number of entries in the database
	 * @author Administrator
	 * 10.03.2004
	 */
	int getSize();
}
