package SlaveNode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import MigratableProcess.MigratableProcess;

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
			socket = new Socket(masterNodeID.getHostName(),
					masterNodeID.getPort());
			inputStream= new ObjectInputStream(
					socket.getInputStream());
			outputStream = new ObjectOutputStream(socket.getOutputStream());
			SendOnlineInfo(socket);

			while (!stop) {
				// receive commands from masterNode
				Object recievedData = inputStream.readObject();
				if(recievedData instanceof String){
					String command = (String) recievedData;
					// send feedback
					Object feedback = slaveNode.recieveCommand(command);
					outputStream.writeObject(feedback);
					outputStream.reset();
				}else if (recievedData instanceof MigratableProcess){
					MigratableProcess migratableProcess = (MigratableProcess) recievedData;
					Object feedback = slaveNode.launchMigratedProcess(migratableProcess);
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
