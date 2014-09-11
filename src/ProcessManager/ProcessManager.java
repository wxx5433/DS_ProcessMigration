package ProcessManager;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import SlaveNode.NodeID;

/**
 * <code>ProcessManager</code> takes responsibility for all the connected
 * <code>SlaveNode</code> and its running processes through
 * <code>SlaveProcessesManager</code>
 * 
 * @author Xiaoxiang Wu(xiaoxiaw)
 * @author Ye Zhou(yezhou)
 */
public class ProcessManager {
	private ConcurrentHashMap<NodeID, SlaveProcessesManager> processesManagement = new ConcurrentHashMap<NodeID, SlaveProcessesManager>();

	/**
	 * Register the newly connected slave node.
	 * 
	 * @param slaveNodeID
	 */
	public void newSlaveOnline(NodeID slaveNodeID) {
		SlaveProcessesManager slaveProcesses = new SlaveProcessesManager();
		processesManagement.put(slaveNodeID, slaveProcesses);
	}

	/**
	 * Register the newly launched process.
	 * 
	 * @param slaveName
	 *            Which slave node the process is launched on
	 * @param threadID
	 *            The thread id that runs this process
	 * @param processName
	 *            The process's name
	 */
	public void newProcessLaunched(String slaveName, long threadID,
			String processName) {
		SlaveProcessesManager slaveProcesses = processesManagement.get(NodeID
				.fromString(slaveName));
		slaveProcesses.newProcessLaunched(threadID, processName);
	}

	/**
	 * Remove the process in management
	 * 
	 * @param slaveName
	 *            Which slave node the process is launched on
	 * @param threadID
	 *            The thread id that runs this process
	 */
	public void removeProcess(String slaveName, long threadID) {
		SlaveProcessesManager slaveProcesses = processesManagement.get(NodeID
				.fromString(slaveName));
		slaveProcesses.removeProcess(threadID);
	}

	/**
	 * Remove the slave in management
	 * 
	 * @param slaveName
	 *            Which slave node the process is launched on
	 */
	public void removeSlave(String slaveName) {
		processesManagement.remove(NodeID.fromString(slaveName));
	}

	/**
	 * get the process Name from thread ID
	 * 
	 * @param slaveName
	 *            Which slave node the process is launched on
	 * @param threadID
	 *            threadID running on the slave node
	 * @return processName
	 */
	public String getProcessName(String slaveName, long threadID) {
		SlaveProcessesManager slaveProcesses = processesManagement.get(NodeID
				.fromString(slaveName));
		return slaveProcesses.getProcessName(threadID);
	}

	/**
	 * Fina a slave node with least processes running on it.
	 * 
	 * @return the slave running minimum processes.
	 */
	public NodeID getAvailableSlave() {
		int minProcessNum = Integer.MAX_VALUE;
		NodeID availableNodeID = null;
		for (Entry<NodeID, SlaveProcessesManager> entry : processesManagement
				.entrySet()) {
			if (entry.getValue().getProcessNum() <= minProcessNum) {
				minProcessNum = entry.getValue().getProcessNum();
				availableNodeID = entry.getKey();
			}
		}
		return availableNodeID;
	}

	/**
	 * Print out the running processes detailed information
	 * 
	 */
	public void listStatus() {
		if (processesManagement.isEmpty()) {
			System.out.println("No process running!");
		}
		for (Entry<NodeID, SlaveProcessesManager> entry : processesManagement
				.entrySet()) {
			System.out.println("SlaveID: " + entry.getKey().toString() + "\t"
					+ "Num: " + entry.getValue().getProcessNum());
			entry.getValue().printProcesses();
		}
	}
}
