package ProcessManager;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import SlaveNode.NodeID;
import SlaveNode.SlaveProcessesManager;

public class ProcessManager {
	private ConcurrentHashMap<NodeID, SlaveProcessesManager> processesManagement = new ConcurrentHashMap<NodeID, SlaveProcessesManager>();

	/**
	 * Register the newly connected slave node.
	 * @param slaveNodeID
	 */
	public void newSlaveOnline(NodeID slaveNodeID) {
		SlaveProcessesManager slaveProcesses = new SlaveProcessesManager();
		processesManagement.put(slaveNodeID, slaveProcesses);
	}

	/**
	 * Register the newly launched process.
	 * @param slaveNodeID Which slave node the process is launched on
	 * @param threadID  The thread id that runs this process
	 * @param processName The process's name
	 */
	public void newProcessLaunched(NodeID slaveNodeID, long threadID,
			String processName) {
		SlaveProcessesManager slaveProcesses = processesManagement.get(slaveNodeID);
		slaveProcesses.newProcessLaunched(threadID, processName);
	}

	public void newProcessLaunched(String slaveName, long threadID,
			String processName) {
		SlaveProcessesManager slaveProcesses = processesManagement.get(NodeID
				.fromString(slaveName));
		slaveProcesses.newProcessLaunched(threadID, processName);
	}

	public void removeProcess(NodeID slaveNodeID, long threadID) {
		SlaveProcessesManager slaveProcesses = processesManagement.get(slaveNodeID);
		slaveProcesses.removeProcess(threadID);
	}

	public void removeProcess(String slaveName, long threadID) {
		SlaveProcessesManager slaveProcesses = processesManagement.get(NodeID
				.fromString(slaveName));
		slaveProcesses.removeProcess(threadID);
	}

	public void removeSlave(NodeID slaveNodeID) {
		processesManagement.remove(slaveNodeID);
	}

	public void removeSlave(String slaveName) {
		processesManagement.remove(NodeID.fromString(slaveName));
	}

	public String getProcessName(NodeID slaveNodeID, long threadID) {
		SlaveProcessesManager slaveProcesses = processesManagement.get(slaveNodeID);
		return slaveProcesses.getProcessName(threadID);
	}

	public String getProcessName(String slaveName, long threadID) {
		SlaveProcessesManager slaveProcesses = processesManagement.get(NodeID
				.fromString(slaveName));
		return slaveProcesses.getProcessName(threadID);
	}

	/**
	 * Fina a slave node with minimum processes running on it.
	 * @return the slave running minimum processes.
	 */
	public NodeID getAvailableSlave() {
		int minProcessNum = Integer.MAX_VALUE;
		NodeID availableNodeID = null;
		for (Entry<NodeID, SlaveProcessesManager> entry : processesManagement.entrySet()) {
			if (entry.getValue().getProcessNum() <= minProcessNum) {
				minProcessNum = entry.getValue().getProcessNum();
				availableNodeID = entry.getKey();
			}
		}
		return availableNodeID;
	}

	public void listStatus() {
		for (Entry<NodeID, SlaveProcessesManager> entry : processesManagement
				.entrySet()) 
			System.out.println("SlaveID: " + entry.getKey().toString() + "\t" + "Num: " + entry.getValue().getProcessNum());
	}
}
