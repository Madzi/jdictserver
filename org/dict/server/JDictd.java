package org.dict.server;


/**
 * Start with: java ...JDictd iniFile
 * <p>
 * For the format of the ini file see DatabaseFactory.
 */
public class JDictd {

	
	public static void main(String args[]) {
		if (args.length == 0) {
			System.out.println("Usage: java ...JDictd configFile [configFile ...]");
			System.exit(0);
		}
		JDictd dictd = new JDictd();
		dictd.startUp( args );
	}

	private void startUp(String[] args) {
		// TODO Auto-generated method stub
		
	}
}
