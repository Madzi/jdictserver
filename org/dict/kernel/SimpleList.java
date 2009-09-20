/*
 * Created on 03.09.2003
 *
 */
package org.dict.kernel;

/**
 * @author Administrator
 *
 */
public class SimpleList implements IList {
	
	private Object[] data;

	public Object get(int i) {
		return getData()[i];
	}

	public int size() {
		return getData().length;
	}

	public Object[] getData() {
		return data;
	}

	public void setData(Object[] objects) {
		data = objects;
	}

}
