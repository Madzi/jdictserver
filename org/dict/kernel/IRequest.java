package org.dict.kernel;

/**
 * Insert the type's description here. Creation date: (10.03.2002 16:56:58)
 * 
 * @author:
 */
public interface IRequest {
	/**
	 * Creation date: (10.03.2002 16:57:30)
	 * 
	 * @return java.lang.String
	 * @param param
	 *            java.lang.String
	 */
	String getParameter(String param);

	/**
	 * Insert the method's description here. Creation date: (10.03.2002
	 * 16:58:17)
	 * 
	 * @return java.lang.String[]
	 * @param param
	 *            java.lang.String
	 */
	String[] getParameterValues(String param);

	/**
	 * Creation date: (10.03.2002 16:57:30)
	 * 
	 * @return java.lang.String
	 */
	String getRequestURI();
}
