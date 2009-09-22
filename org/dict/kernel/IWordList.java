/*
 * Created on 04.09.2003
 *
 */
package org.dict.kernel;

/**
 * @author duc
 * 
 */
public interface IWordList {
	String getDescription();

	IWordPosition[] getWordPositions();
}
