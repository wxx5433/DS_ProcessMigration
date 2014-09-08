package MigratableProcess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketListenThread implements Runnable {
	private MasterNode masterNode;
	private int portNum;
	private boolean stop;

	public SocketListenThread(MasterNode masterNode, int portNum) {
		this.masterNode = masterNode;
		this.portNum = portNum;
		stop = false;
	}

	@Override
	public void run() {
		ServerSocket listener;
		try {
			listener = new ServerSocket(portNum);
			while (!stop) {
				try {
					Socket socket = listener.accept();
					BufferedReader input = new BufferedReader(
							new InputStreamReader(socket.getInputStream()));
					String slaveName = input.readLine();
					ObjectOutputStream out = new ObjectOutputStream(
							socket.getOutputStream());
					System.out.println(slaveName + ":online!");
					masterNode.newSlaveOnline(slaveName, socket, out);
				} catch (IOException e) {
					System.out.println("Error occur when listening:");
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			System.out.println("Error accur when creating server:");
			e.printStackTrace();
		}
	}

	public void terminate() {
		stop = true;
	}
}
