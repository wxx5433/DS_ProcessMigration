package SlaveNode;

import java.util.concurrent.ConcurrentHashMap;

/**
 * This class manages all processes running on a slave node.
 * @author Xiaoxiang Wu(xiaoxiaw)
 * @author Ye Zhou
 */
public class SlaveProcessesManager {
	private ConcurrentHashMap<Long, String> slaveProcessesManagement = new ConcurrentHashMap<Long, String>();

	/**
	 * add new process to this slave node.
	 * @param threadID
	 * @param processName
	 */
	public void newProcessLaunched(long threadID, String processName) {
		slaveProcessesManagement.put(threadID, processName);
	}

	public int getProcessNum() {
		return slaveProcessesManagement.size();
	}

	public boolean getProcessStatus(long threadID) {
		return slaveProcessesManagement.containsKey(threadID);
	}

	public String getProcessName(long threadID) {
		return slaveProcessesManagement.get(threadID);
	}

	public boolean removeProcess(long threadID) {
		if (slaveProcessesManagement.remove(threadID) != null)
			return true;
		else
			return false;
	}

}
