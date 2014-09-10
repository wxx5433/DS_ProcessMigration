package SlaveNode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import MigratableProcess.MigratableProcess;

/**
 * This class is used to initialize a socket thread for slave nodes to 
 * have a socked connection with master node.
 * @author Xiaoxiang Wu(xiaoxiaw)
 * @author Ye Zhou
 *
 */
public class SlaveSocketThread implements Runnable {
	private NodeID masterNodeID;
	private boolean stop;
	private SlaveNode slaveNode;
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;

	public SlaveSocketThread(SlaveNode slaveNode, NodeID masterNodeID) {
		this.slaveNode = slaveNode;
		this.masterNodeID = masterNodeID;
		stop = false;
	}

	@Override
	public void run() {
		Socket socket;
		try {
			socket = new Socket(masterNodeID.getHostName(), masterNodeID.getPort());
			inputStream= new ObjectInputStream(socket.getInputStream());
			outputStream = new ObjectOutputStream(socket.getOutputStream());
			/* tell master node that the new slave node is online! */
			SendOnlineInfo(socket);
			inputStream= new ObjectInputStream(
					socket.getInputStream());
			
			while (!stop) {
				/* receive commands from masterNode */
				Object recievedData = inputStream.readObject();
				if(recievedData instanceof String){
					String command = (String) recievedData;
					/* get feedback of the command executed on slave node */
					Object feedback = slaveNode.executeCommand(command);
					/* send feedback to master node */
					outputStream.writeObject(feedback);
					outputStream.reset();
				}else if (recievedData instanceof MigratableProcess){
					/* 
					 * This slave node is asked to execute 
					 * a process migrated from other slave node 
					 */
					MigratableProcess migratableProcess = (MigratableProcess) recievedData;
					Object feedback = slaveNode.launchMigratedProcess(migratableProcess);
					/* send feedback to master node */
					outputStream.writeObject(feedback);
					outputStream.reset();
				}
			}
			socket.close();
		} catch (IOException e) {
			System.out.println(slaveNode.getSlaveName()
					+ ":Exception founded when trying to connect to manager: "
					+ e);
			e.printStackTrace();
			System.exit(0);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void SendOnlineInfo(Socket sock) throws IOException {
		try {
			outputStream.writeObject(slaveNode.getSlaveName());
			outputStream.reset();
			System.out.println(slaveNode.getSlaveName() + ":Registerred");
		} catch (java.net.ConnectException e) {
			sock.close();
			System.out.println(slaveNode.getSlaveName()
					+ ":Unable to register to the manager!");
			System.exit(0);
		}
	}

	public void terminate() {
		stop = true;
	}

}
