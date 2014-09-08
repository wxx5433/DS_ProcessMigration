package MigratableProcess;

import java.util.concurrent.ConcurrentHashMap;

public class SlaveProcesses {
	private ConcurrentHashMap<Long, String> slaveProcessesManagement = new ConcurrentHashMap<Long, String>();

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
