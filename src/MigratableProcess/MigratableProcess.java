package MigratableProcess;

import java.io.Serializable;

/**
 * This abstract class gives an uniform definition of <code>MigratableProcess</code>.
 * 
 * @author Xiaoxiang Wu(xiaoxiaw)
 * @author Ye Zhou
 */
public abstract class MigratableProcess implements Runnable, Serializable {
	
	
	/**
	 * Any class extends <code>MigratableProcess</code> should take 
	 * an array of String as its arguments.
	 * @param args Array of String
	 */
	public MigratableProcess(String[] args) {
		
	}
	
	/**
	 * Any class extends <code>MigratableProcess</code> must overwrite this method 
	 * to run as a thread.
	 */
	public abstract void run();
	
	/**
	 * Any class extends <code>MigratableProcess</code> must overwrite this method 
	 * to put the process into a safe state, so that it can be migrated.
	 */
	public abstract void suspend();
	
	/**
	 * Any class extends <code>MigratableProcess</code> must overwrite this method 
	 * to return a string representation of the process object.
	 */
	public abstract String toString();
	
	/**
	 * Any class extends <code>MigratableProcess</code> must overwrite this method 
	 * to return restore the state of the process object.
	 */
	public abstract void resume();
		
}
