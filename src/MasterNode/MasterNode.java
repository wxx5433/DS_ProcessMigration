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

	public MasterNode(int portNum) {
		this.portNum = portNum;
	}

	private void start() {
		socketListener = new SocketListenThread(this, this.portNum);
		listen = new Thread(socketListener);
		terminalThread = new TerminalThread(this);
		terminal = new Thread(terminalThread);
		listen.start();
		terminal.start();
	}

	public void newSlaveOnline(String slaveName, Socket socket,
			ObjectOutputStream out) {
		NodeID slaveNodeID = NodeID.fromString(slaveName);
		slavesManagement.put(slaveNodeID, socket);
		slavesOutputMap.put(slaveNodeID, out);
		System.out.println(slaveNodeID.toString() + " add to management!");
	}

	public void newCommand(String command) {
		System.out.println(command + "input");
		if (command.startsWith("launch")) {
			launchNewProcess(command);
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
		String destSlave = null;
		if (commandArray.length == 3) {
			destSlave = commandArray[2];
		} else {
			destSlave = getAvailableDestSlave().toString();
		}
		sendCommand(destSlave, command);

		String feedback = getFeedback(destSlave);
		System.out.println(feedback);

		System.out.println(feedback);
		int threadID = Integer.parseInt(feedback);
		processManager.newProcessLaunched(destSlave, threadID, processName);
		System.out.println(feedback);
	}

	private Socket getSlaveSocket(NodeID slaveNodeID) {
		return slavesManagement.get(slaveNodeID);
	}

	private ObjectOutputStream getSlaveSocketStream(NodeID slaveNodeID) {
		return slavesOutputMap.get(slaveNodeID);
	}

	private MigratableProcess getMigratedProcess(String destSlave) {
		MigratableProcess feedback = null;
		Socket socket = getSlaveSocket(NodeID.fromString(destSlave));
		ObjectInputStream feedBackStream = recieveFeedBackStream(socket);
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
		Socket socket = getSlaveSocket(NodeID.fromString(destSlave));
		ObjectInputStream feedBackStream = recieveFeedBackStream(socket);
		try {
			feedback = (String) feedBackStream.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return feedback;
	}

	private ObjectInputStream recieveFeedBackStream(Socket socket) {
		ObjectInputStream inputObjChannel = null;
		try {
			inputObjChannel = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return inputObjChannel;
	}

	private void sendCommand(String slaveName, String command) {
		NodeID slaveNodeID = NodeID.fromString(slaveName);
		ObjectOutputStream outputStream = getSlaveSocketStream(slaveNodeID);
		try {
			outputStream.writeObject(command);
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
