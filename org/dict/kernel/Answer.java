package org.dict.kernel;

/**
 * Insert the type's description here.
 * Creation date: (10.08.01 21:02:15)
 * @author: Administrator
 */
public class Answer implements IAnswer {
	IDatabase fDatabase;
	int fPosition;
	String fKey;
	String fDefinition;
	IWordList fMatches;
	IWordList adjacentWords;
	
	public Answer(IDatabase db, String key, int pos, String def) {
		fDatabase = db;
		fKey = key;
		fPosition = pos;
		fDefinition = def;
		fMatches = new WordList();
		adjacentWords = new WordList();
	}
	/**
	 * getDatabase method comment.
	 */
	public IDatabase getDatabase() {
		return fDatabase;
	}
	/**
	 * getDefinition method comment.
	 */
	public String getDefinition() {
		return fDefinition;
	}
	/**
	 * getKey method comment.
	 */
	public java.lang.String getKey() {
		return fKey;
	}
	public IWordList getMatches() {
		return fMatches;
	}
	/**
	 * getPosition method comment.
	 */
	public int getPosition() {
		return fPosition;
	}
	public void setMatches(IWordList arr) {
		fMatches = arr;
	}
	public IWordList getAdjacentWords() {
		return adjacentWords;
	}
	public void setAdjacentWords(IWordList ls) {
		adjacentWords = ls;
	}
	public void setDatabase(IDatabase db) {
		fDatabase = db;
	}
	public void setDefinition(String def) {
		fDefinition = def;
	}
	public void setKey(String k) {
		fKey = k;
	}
}
