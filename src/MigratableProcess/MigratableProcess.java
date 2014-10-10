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
	 * The flag to show if the process is suspended.
	 */
	protected volatile boolean suspending;
	protected boolean finished;
	
	/**
	 * Any class extends <code>MigratableProcess</code> should take 
	 * an array of String as its arguments.
	 * @param args Array of String
	 */
	public MigratableProcess(String[] args) {
		suspending = false;
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
	
	/**
	 * This method is to stop running of the thread.
	 */
	public void stop() {
		suspending = true;
		finished = true;
	}
		
	/**
	 * If a job has finished yet
	 * @return true if the job has finished, false otherwise.
	 */
	public boolean hasFinished() {
		return finished;
	}
}
