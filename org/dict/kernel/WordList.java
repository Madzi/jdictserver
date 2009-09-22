/*
 * Created on 04.09.2003
 *
 */
package org.dict.kernel;

/**
 * @author duc
 * 
 */
public class WordList implements IWordList {

	String description;
	IWordPosition[] wordPositions;

	public WordList() {
		this("", new IWordPosition[0]);
	}

	public WordList(String desc, IWordPosition[] arr) {
		description = desc;
		wordPositions = arr;
	}

	public String getDescription() {
		return description;
	}

	public IWordPosition[] getWordPositions() {
		return wordPositions;
	}

	public void setDescription(String string) {
		description = string;
	}

	public void setWordPositions(IWordPosition[] positions) {
		wordPositions = positions;
	}

}
