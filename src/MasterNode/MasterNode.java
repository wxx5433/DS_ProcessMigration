package MasterNode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import MigratableProcess.MigratableProcess;
import ProcessManager.ProcessManager;
import SlaveNode.NodeID;

public class MasterNode {
	private ConcurrentHashMap<NodeID, Socket> slavesManagement = new ConcurrentHashMap<NodeID, Socket>();
	private ConcurrentHashMap<NodeID, ObjectOutputStream> slavesOutputMap = new ConcurrentHashMap<NodeID, ObjectOutputStream>();
	private ConcurrentHashMap<NodeID, ObjectInputStream> slavesInputMap = new ConcurrentHashMap<NodeID, ObjectInputStream>();
	private static final int DEFAULT_PORT_NUM = 10000;
	private int portNum;
	private SocketListenThread socketListener;
	private TerminalThread terminalThread;
	private Thread listen;
	private Thread terminal;
	private ProcessManager processManager;

	public MasterNode() {
		portNum = DEFAULT_PORT_NUM;
	}

	/**
	 * Initialize MasterNode on a specific node
	 * 
	 * @param portNum
	 */
	public MasterNode(int portNum) {
		this.portNum = portNum;
	}

	private void start() {
		/* start a listen thread to accept socket connection */
		socketListener = new SocketListenThread(this, this.portNum);
		listen = new Thread(socketListener);
		listen.start();
		/* start a terminal thread to accept users' input */
		terminalThread = new TerminalThread(this);
		terminal = new Thread(terminalThread);
		terminal.start();
		/* start a process manager to monitor all processes run on slave nodes */
		processManager = new ProcessManager();
	}

	public void newSlaveOnline(String slaveName, Socket socket,
			ObjectOutputStream out, ObjectInputStream input) {
		NodeID slaveNodeID = NodeID.fromString(slaveName);
		slavesManagement.put(slaveNodeID, socket);
		slavesOutputMap.put(slaveNodeID, out);
		slavesInputMap.put(slaveNodeID, input);
		System.out.println(slaveNodeID.toString() + " add to management!");
		processManager.newSlaveOnline(slaveNodeID);
	}

	/**
	 * Parse users' input command
	 * 
	 * @param command
	 */
	public void parseCommand(String command) {
		System.out.println(command + "input");
		if (command.startsWith("launch")) {
			launchNewProcess(command);
		} else if (command.startsWith("targetlaunch")) {
			targetLaunchNewProcess(command);
		} else if (command.startsWith("migrate")) {
			migrateProcess(command);
		} else if (command.startsWith("list")) {
			listStatus();
		} else if (command.startsWith("terminate")) {
			terminateSlave(command);
		} else if (command.startsWith("processterminate")) {
			terminateProcess(command);
		} else if (command.equals("exit")) {
			stop();
		}
	}

	private void terminateProcess(String command) {
		String[] commandArray = command.split(" ");
		String slaveName = commandArray[1];
		long threadID = Long.parseLong(commandArray[2]);
		sendCommand(slaveName, command);
		String feedback = getFeedback(slaveName);
		if (feedback.equals("OK")) {
			processManager.removeProcess(slaveName, threadID);
		}
	}

	private void terminateSlave(String command) {
		String[] commandArray = command.split(" ");
		String slaveName = commandArray[1];
		sendCommand(slaveName, command);
		String feedback = getFeedback(slaveName);
		if (feedback.equals("OK")) {
			processManager.removeSlave(slaveName);
		}
	}

	private void listStatus() {
		System.out
				.println("list all the status of the slaves and running process!");
		processManager.listStatus();
	}

	private void migrateProcess(String command) {
		String[] commandArray = command.split(" ");
		String migratedDestSlave = null;
		String migrateSourceSlave = commandArray[1];
		long threadIDSource = Long.parseLong(commandArray[2]);
		if (commandArray.length == 4) {
			migratedDestSlave = commandArray[3];
		} else {
			migratedDestSlave = getAvailableDestSlave().toString();
		}
		sendCommand(migrateSourceSlave, command);
		MigratableProcess migratableProcess = getMigratedProcess(migrateSourceSlave);
		sendMigratableProcess(migratedDestSlave, migratableProcess);
		String feedback = getFeedback(migratedDestSlave);
		int threadID = Integer.parseInt(feedback);
		System.out.println("get the migrated process rerun threadID: "
				+ threadID);
		processManager.newProcessLaunched(migratedDestSlave, threadID,
				processManager.getProcessName(migrateSourceSlave,
						threadIDSource));
		processManager.removeProcess(migrateSourceSlave, threadIDSource);
	}

	private void sendMigratableProcess(String migratedDestSlave,
			MigratableProcess migratableProcess) {
		NodeID slaveNodeID = NodeID.fromString(migratedDestSlave);
		ObjectOutputStream outputStream = getSlaveSocketStream(slaveNodeID);
		try {
			outputStream.writeObject(migratableProcess);
			outputStream.reset();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private NodeID getAvailableDestSlave() {
		return processManager.getAvailableSlave();
	}

	private void launchNewProcess(String command) {
		String[] commandArray = command.split(" ");
		String processName = commandArray[1];
		String destSlave = getAvailableDestSlave().toString();
		sendCommand(destSlave, command);
		String feedback = getFeedback(destSlave);
		System.out.println("launch new process on " + destSlave
				+ " threadID is " + feedback);
		int threadID = Integer.parseInt(feedback);
		processManager.newProcessLaunched(destSlave, threadID, processName);
	}

	private void targetLaunchNewProcess(String command) {
		String[] commandArray = command.split(" ");
		String destSlave = commandArray[1];
		String processName = commandArray[2];
		sendCommand(destSlave, command);
		String feedback = getFeedback(destSlave);
		System.out.println(feedback);
		int threadID = Integer.parseInt(feedback);
		processManager.newProcessLaunched(destSlave, threadID, processName);
		System.out.println(feedback);
	}

	private ObjectOutputStream getSlaveSocketStream(NodeID slaveNodeID) {
		return slavesOutputMap.get(slaveNodeID);
	}

	private MigratableProcess getMigratedProcess(String destSlave) {
		MigratableProcess feedback = null;
		ObjectInputStream feedBackStream = recieveFeedBackStream(NodeID
				.fromString(destSlave));
		try {
			feedback = (MigratableProcess) feedBackStream.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return feedback;
	}

	private String getFeedback(String destSlave) {
		String feedback = null;
		ObjectInputStream feedBackStream = recieveFeedBackStream(NodeID
				.fromString(destSlave));
		try {
			feedback = (String) feedBackStream.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return feedback;
	}

	private ObjectInputStream recieveFeedBackStream(NodeID slaveNodeID) {
		return getInputStream(slaveNodeID);
	}

	private ObjectInputStream getInputStream(NodeID slaveNodeID) {
		return slavesInputMap.get(slaveNodeID);
	}

	private void sendCommand(String slaveName, String command) {
		NodeID slaveNodeID = NodeID.fromString(slaveName);
		ObjectOutputStream outputStream = getSlaveSocketStream(slaveNodeID);
		try {
			outputStream.writeObject(command);
			outputStream.reset();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void stop() {
		listen.interrupt();
		terminal.interrupt();
		System.exit(0);
	}

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			MasterNode masterNode = new MasterNode();
			masterNode.start();
		} else if (args.length == 1) {
			MasterNode masterNode = new MasterNode(Integer.parseInt(args[0]));
			masterNode.start();
		}
	}
}
