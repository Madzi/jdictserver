package org.dict.kernel;

/**
 * Insert the type's description here.
 * Creation date: (7/31/01 3:35:05 PM)
 * @author: Administrator
 */
public interface IDictEngine {
/**
 * Insert the method's description here.
 * Creation date: (7/31/01 3:35:55 PM)
 * @return IDefinition[]
 * @param db java.lang.String
 * @param word java.lang.String
 */
IAnswer[] define(String db, String word);

IDatabase[] getDatabases();

void addDatabase(IDatabase db);

void removeDatabase(IDatabase db);


IAnswer[] defineMatch(String db, String word, String pos, boolean define, int strategy);

/**
 * Insert the method's description here.
 * Creation date: (7/31/01 3:36:20 PM)
 * @return IAnswer[]
 * @param db java.lang.String
 * @param word java.lang.String
 */
IAnswer[] match(String db, String word, int strategy);

public IAnswer[] lookup(IRequest req);
}
