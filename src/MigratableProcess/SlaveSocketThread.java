package MigratableProcess;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class SlaveSocketThread implements Runnable {
	private NodeID masterNodeID;
	private boolean stop;
	private SlaveNode slaveNode;

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
			SendOnlineInfo(socket);

			ObjectInputStream input = new ObjectInputStream(
					socket.getInputStream());
			while (!stop) {
				// receive commands from masterNode
				String command = (String) input.readObject();
				// send feedback
				Object feedback = slaveNode.recieveCommand(command);
				System.out.println((CatProcess)feedback);
				ObjectOutputStream out = new ObjectOutputStream(
						socket.getOutputStream());
				out.writeObject(feedback);
				System.out.println((CatProcess)feedback);
				out.reset();
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
			PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
			out.println(slaveNode.getSlaveName());
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
