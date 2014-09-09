package ProcessManager;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import SlaveNode.NodeID;
import SlaveNode.SlaveProcesses;

public class ProcessManager {
	private ConcurrentHashMap<NodeID, SlaveProcesses> processesManagement = new ConcurrentHashMap<NodeID, SlaveProcesses>();

	public void newSlaveOnline(NodeID slaveNodeID) {
		SlaveProcesses slaveProcesses = new SlaveProcesses();
		processesManagement.put(slaveNodeID, slaveProcesses);
	}

	public void newProcessLaunched(NodeID slaveNodeID, long threadID,
			String processName) {
		SlaveProcesses slaveProcesses = processesManagement.get(slaveNodeID);
		slaveProcesses.newProcessLaunched(threadID, processName);
	}

	public void newProcessLaunched(String slaveName, long threadID,
			String processName) {
		SlaveProcesses slaveProcesses = processesManagement.get(NodeID
				.fromString(slaveName));
		slaveProcesses.newProcessLaunched(threadID, processName);
	}

	public void removeProcess(NodeID slaveNodeID, long threadID) {
		SlaveProcesses slaveProcesses = processesManagement.get(slaveNodeID);
		slaveProcesses.removeProcess(threadID);
	}

	public void removeProcess(String slaveName, long threadID) {
		SlaveProcesses slaveProcesses = processesManagement.get(NodeID
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
		SlaveProcesses slaveProcesses = processesManagement.get(slaveNodeID);
		return slaveProcesses.getProcessName(threadID);
	}

	public String getProcessName(String slaveName, long threadID) {
		SlaveProcesses slaveProcesses = processesManagement.get(NodeID
				.fromString(slaveName));
		return slaveProcesses.getProcessName(threadID);
	}

	public NodeID getAvailableSlave() {
		int minProcessNum = Integer.MAX_VALUE;
		NodeID availableNodeID = null;
		for (Entry<NodeID, SlaveProcesses> entry : processesManagement
				.entrySet()) {
			if (entry.getValue().getProcessNum() <= minProcessNum) {
				minProcessNum = entry.getValue().getProcessNum();
				availableNodeID = entry.getKey();
			}
		}
		return availableNodeID;
	}

	public void listStatus() {
		for (Entry<NodeID, SlaveProcesses> entry : processesManagement
				.entrySet()) 
			System.out.println("SlaveID: " + entry.getKey().toString() + "\t" + "Num: " + entry.getValue().getProcessNum());
	}
}