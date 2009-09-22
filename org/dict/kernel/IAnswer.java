package org.dict.kernel;

/**
 * Insert the type's description here. Creation date: (10.08.01 20:55:06)
 * 
 * @author: Administrator
 */
public interface IAnswer {
	IWordList getAdjacentWords();

	IDatabase getDatabase();

	String getDefinition();

	String getKey();

	IWordList getMatches();

	int getPosition();

	void setAdjacentWords(IWordList ls);

	void setDatabase(IDatabase db);

	void setDefinition(String def);

	void setKey(String k);

	void setMatches(IWordList ls);
}
