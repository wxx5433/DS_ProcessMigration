package ProcessManager;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class manages all processes running on a slave node.
 * 
 * @author Xiaoxiang Wu(xiaoxiaw)
 * @author Ye Zhou(yezhou)
 */
public class SlaveProcessesManager {
	private ConcurrentHashMap<Long, String> slaveProcessesManagement = new ConcurrentHashMap<Long, String>();

	/**
	 * add new process to this slave node.
	 * 
	 * @param threadID
	 * @param processName
	 */
	public void newProcessLaunched(long threadID, String processName) {
		slaveProcessesManagement.put(threadID, processName);
	}

	/**
	 * Get the running process number on this slave node
	 * 
	 * @return process number
	 */
	public int getProcessNum() {
		return slaveProcessesManagement.size();
	}

	/**
	 * Check whether there is this threadID in the slave node
	 */
	public boolean getProcessStatus(long threadID) {
		return slaveProcessesManagement.containsKey(threadID);
	}

	/**
	 * Get the process name from threadID
	 */
	public String getProcessName(long threadID) {
		return slaveProcessesManagement.get(threadID);
	}

	/**
	 * Remove process by threadID
	 */
	public boolean removeProcess(long threadID) {
		if (slaveProcessesManagement.remove(threadID) != null)
			return true;
		else
			return false;
	}

	/**
	 * print process information
	 */
	public void printProcesses() {
		if (!slaveProcessesManagement.isEmpty()) {
			System.out.println("Process ThreadID\tProcess Name");
			for (Entry<Long, String> entry : slaveProcessesManagement
					.entrySet()) {
				System.out.println(entry.getKey() + "\t" + entry.getValue());
			}
		}
	}

}
