package MasterNode;

import java.util.Scanner;

/**
 * This is the class that the <code>MasterNode</code> use to run a terminal
 * thread, so that it can receive users' inupt.
 * 
 * @author Xiaoxiang Wu(xiaoxiaw)
 * @author Ye Zhou
 */
public class TerminalThread implements Runnable {
	/**
	 * The masterNode which this terminal thread belongs to.
	 */
	private MasterNode masterNode;
	private boolean stop;

	public TerminalThread(MasterNode masterNode) {
		this.masterNode = masterNode;
		stop = false;
	}

	@Override
	public void run() {
		System.out
				.print("****************************************\n"
						+ "Please enter your command:\n"
						+ "1.Launch a new process on a automatically selected slave:\n"
						+ "exp:launch GrepProcess\n"
						+ "2.Launch a new process on a target slave\n"
						+ "exp:targetlaunch 128.237.213.96:8888 GrepProcess \n"
						+ "3.Migrate a process from one slave to another slave\n"
						+ "exp:migrate 128.237.213.96:8888 10110 128.237.213.97:3456\n"
						+ "4.Migrate a process from one slave to a automatically selected slave\n"
						+ "exp:migrate 128.237.213.96:8888 10110\n"
						+ "5.List all online slaves and their status\n"
						+ "exp:list\n" + "6.Terminate a slaves\n"
						+ "exp:terminate 128.237.213.96:8888\n"
						+ "7.Terminate a process in a slave\n"
						+ "exp:processterminate 128.237.213.96:8888 10110\n"
						+ "8.Exit\n" + "exp:exit\n");
		while (!stop) {
			/* Receive User's command */
			Scanner keyboard = new Scanner(System.in);
			String command = keyboard.nextLine();
			/* parse the command */
			masterNode.parseCommand(command);
		}
	}

	/**
	 * Terminate the terminal thread.
	 */
	public void terminate() {
		stop = true;
	}
}
