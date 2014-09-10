package SlaveNode;

import java.util.HashMap;
import MigratableProcess.CatProcess;
import MigratableProcess.MigratableProcess;

public class SlaveNode {
	private static final String DEFAULT_MASTER_ADDRESS = "localhost";
	private static final int DEFAULT_MASTER_PORT = 10000;
	private static final String DEFAULT_SLAVE_ADDRESS = "localhost";
	private static final int DEFAULT_SLAVE_PORT = 8888;
	private NodeID slaveNodeID;
	private NodeID masterNodeID;
	private Thread socketThread;
	private SlaveSocketThread slaveSocket;
	private HashMap<Long, Thread> threadManager = new HashMap<Long, Thread>();
	private HashMap<Long, MigratableProcess> processManager = new HashMap<Long, MigratableProcess>();

	public SlaveNode() {
		slaveNodeID = new NodeID(DEFAULT_SLAVE_ADDRESS, DEFAULT_SLAVE_PORT);
		masterNodeID = new NodeID(DEFAULT_MASTER_ADDRESS, DEFAULT_MASTER_PORT);
	}

	public SlaveNode(String slaveName, int portNum, String masterAddress,
			int masterPort) {
		slaveNodeID = new NodeID(slaveName, portNum);
		masterNodeID = new NodeID(masterAddress, masterPort);
	}

	public void start() {
		slaveSocket = new SlaveSocketThread(this, masterNodeID);
		socketThread = new Thread(slaveSocket);
		socketThread.start();
	}

	public String getSlaveName() {
		return slaveNodeID.toString();
	}

	/* execute command send from master node */
	public Object executeCommand(String command) {
		System.out.println(command);
		if (command.startsWith("terminate")) {
			System.out.println("terminate");
			stop();
			return "OK";
		} else if (command.startsWith("migrate")) {
			System.out.println("migrate");
			return migrateProcess(command);
		} else if (command.startsWith("processterminate")) {
			System.out.println("process terminate");
			processTerminate(command);
			return "OK";
		} else if (command.startsWith("launch")) {
			System.out.println("launch");
			String threadID = launchNewProcess(command);
			return threadID;
		} else if (command.startsWith("getmigrated")) {
			System.out.println("getmigrated");
			String threadID = launchNewProcess(command);
			return threadID;
		}
		return "error";
	}

	@SuppressWarnings("null")
	private String launchNewProcess(String command) {
		String[] commandArray = command.split(" ");
		String processName = commandArray[1];
		if (processName.equals("CatProcess")) {
			String inputFilePath = commandArray[3];
			String outputFilePath = commandArray[4];
			String[] args = new String[2];
			args[0] = inputFilePath;
			args[1] = outputFilePath;
			MigratableProcess migratableProcess = null;
			try {
				migratableProcess = new CatProcess(args);
			} catch (Exception e) {
				e.printStackTrace();
			}
			Thread runProcess = new Thread(migratableProcess);
			runProcess.start();
			long threadID = runProcess.getId();
			threadManager.put(threadID, runProcess);
			processManager.put(threadID, migratableProcess);
			return String.valueOf(threadID);
		}
		return null;
	}
	
	public String launchMigratedProcess(MigratableProcess migratableProcess) {
		Thread runProcess = new Thread(migratableProcess);
		runProcess.start();
		long threadID = runProcess.getId();
		threadManager.put(threadID, runProcess);
		processManager.put(threadID, migratableProcess);
		return String.valueOf(threadID);
	}

	private void processTerminate(String command) {
		String[] commandArray = command.split(" ");
		long threadID = Long.parseLong(commandArray[2]);
		Thread threadStop = threadManager.get(threadID);
		threadStop.interrupt();
		processManager.remove(threadID);
		threadManager.remove(threadID);
	}

	private Object migrateProcess(String command) {
		String[] commandArray = command.split(" ");
		long threadID = Long.parseLong(commandArray[2]);
		MigratableProcess migratableProcess = processManager.get(threadID);
		System.out.println(migratableProcess);
		migratableProcess.suspend();
		System.out.println(migratableProcess);
		return migratableProcess;
	}

	private void stop() {
		slaveSocket.terminate();
		System.exit(0);
	}

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			SlaveNode slaveNode = new SlaveNode();
			slaveNode.start();
		} else if (args.length == 4) {
			SlaveNode slaveNode = new SlaveNode(args[0],
					Integer.parseInt(args[1]), args[2],
					Integer.parseInt(args[3]));
			slaveNode.start();
		}
	}
}
