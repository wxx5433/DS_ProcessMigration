package MasterNode;

import java.util.Scanner;

public class TerminalThread implements Runnable {
	private MasterNode masterNode;
	private boolean stop;

	public TerminalThread(MasterNode masterNode) {
		this.masterNode = masterNode;
		stop = false;
	}

	@SuppressWarnings("resource")
	@Override
	public void run() {
		while (!stop) {
			System.out
					.print("****************************************\n"
							+ "Please enter your command:\n"
							+ "1.Launch a new process on a automatically selected slave:\n"
							+ "exp:launch GrepProcess\n"
							+ "2.Launch a new process on a specific slave\n"
							+ "exp:launch GrepProcess 128.237.213.96:8888\n"
							+ "3.Migrate a process from one slave to another slave\n"
							+ "exp:migrate 128.237.213.96:8888 10110 128.237.213.97:3456\n"
							+ "4.Migrate a process from one slave to a automatically selected slave\n"
							+ "exp:migrate 128.237.213.96:8888 10110\n"
							+ "5.List all online slaves and their status\n"
							+ "exp:list\n"
							+ "6.Terminate a slaves\n"
							+ "exp:terminate 128.237.213.96:8888\n"
							+ "7.Terminate a process in a slave\n"
							+ "exp:processterminate 128.237.213.96:8888 10110\n"
							+ "8.Exit\n" + "exp:exit\n");
			// get user inputs
			Scanner keyboard = new Scanner(System.in);
			String command = keyboard.nextLine();
			masterNode.newCommand(command);
		}
	}

	public void terminate() {
		stop = true;
	}
}