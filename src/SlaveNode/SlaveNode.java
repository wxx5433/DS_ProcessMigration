package SlaveNode;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import MigratableProcess.MigratableProcess;

/**
 * <code>SlaveNode</code> restores all the running thread information and their
 * related <code>MigratableProcess</code>. It also needs to analyze the command
 * or serialized data from <code>MasterNode</code>
 * 
 * @author Xiaoxiang Wu(xiaoxiaw)
 * @author Ye Zhou(yezhou)
 */
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

	/**
	 * Start the socket connection thread
	 * 
	 */
	public void start() {
		slaveSocket = new SlaveSocketThread(this, masterNodeID);
		socketThread = new Thread(slaveSocket);
		socketThread.start();
	}

	/**
	 * get the slave name
	 * 
	 * @return the slave Name including hostname/IP address and port
	 */
	public String getSlaveName() {
		return slaveNodeID.toString();
	}

	/**
	 * execute command recieved from master node
	 * 
	 * @param command
	 * @return status or threadID info
	 */
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
			String threadID = targetLaunch(command, 1);
			return threadID;
		} else if (command.startsWith("targetlaunch")) {
			System.out.println("targetlaunch");
			String threadID = targetLaunch(command, 2);
			return threadID;
		}
		return "error";
	}

	/**
	 * Analyze the command and get out the information about the processName and
	 * its parameters
	 * 
	 * @param command
	 * @param pos
	 *            offset under different commands
	 * @return command running result
	 */
	private String targetLaunch(String command, int pos) {
		System.out.println("recived command is: " + command);
		String commandBak = command;
		String[] commandArray = commandBak.split(" ");
		String processName = commandArray[pos];
		System.out.println("process Name is: " + processName);
		int blankPos = command.indexOf(" ");
		String[] args = null;
		blankPos = command.indexOf(" ", blankPos + 1);
		if (pos == 2)
			blankPos = command.indexOf(" ", blankPos + 1);
		if (blankPos != -1) {
			String argsStr = command.substring(blankPos + 1, command.length());
			args = argsStr.split(" ");
		}
		for (int i = 0; i < args.length; i++) {
			System.out.println(args[i]);
		}

		return launchNewProcess(processName, args);
	}

	/**
	 * Start new process and return its threadID
	 * 
	 * @param processName
	 * @param args
	 * @return threadID
	 */
	private String launchNewProcess(String processName, String[] args) {
		try {
			Class<?> migratableProcessClass = Class
					.forName("MigratableProcess." + processName);
			Constructor<?> constructor = migratableProcessClass
					.getConstructor(String[].class);
			System.out.println(constructor.getName());
			System.out.println("tur: " + constructor.isVarArgs());
			MigratableProcess migratableProcess = (MigratableProcess) constructor
					.newInstance(new Object[] { args });
			Thread runProcess = new Thread((Runnable) migratableProcess);
			runProcess.start();
			long threadID = runProcess.getId();
			threadManager.put(threadID, runProcess);
			processManager.put(threadID, (MigratableProcess) migratableProcess);
			return String.valueOf(threadID);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Launch a process migrated from other slaves
	 * 
	 * @param migratableProcess
	 * @return relaunched process threadID
	 */
	public String launchMigratedProcess(MigratableProcess migratableProcess) {
		migratableProcess.resume();
		Thread runProcess = new Thread(migratableProcess);
		runProcess.start();
		long threadID = runProcess.getId();
		System.out.println("rerun threadID is: " + threadID);
		threadManager.put(threadID, runProcess);
		processManager.put(threadID, migratableProcess);
		return String.valueOf(threadID);
	}

	/**
	 * terminate a specific thread
	 * 
	 * @param command
	 */
	private void processTerminate(String command) {
		String[] commandArray = command.split(" ");
		long threadID = Long.parseLong(commandArray[2]);
		Thread threadStop = threadManager.get(threadID);
		threadStop.interrupt();
		processManager.remove(threadID);
		threadManager.remove(threadID);
	}

	/**
	 * migrate process from this slave node
	 * 
	 * @param command
	 * @return migratableProcess
	 */
	private Object migrateProcess(String command) {
		String[] commandArray = command.split(" ");
		long threadID = Long.parseLong(commandArray[2]);
		MigratableProcess migratableProcess = processManager.get(threadID);
		System.out.println(migratableProcess);
		migratableProcess.suspend();
		System.out.println(migratableProcess);
		return migratableProcess;
	}

	/**
	 * Stop slave node
	 */
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
