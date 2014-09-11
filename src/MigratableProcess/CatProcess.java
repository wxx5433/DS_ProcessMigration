package MigratableProcess;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import TransactionalIO.TransactionalFileInputStream;
import TransactionalIO.TransactionalFileOutputStream;

/**
 * 
 * 
 * @author Xiaoxiang Wu(xiaoxiaw)
 * @author Ye Zhou
 */
public class CatProcess extends MigratableProcess {
	/**
	 * The flag to show if the process is suspended.
	 */
	private volatile boolean suspending;
	private boolean finished;

	private TransactionalFileInputStream inFile;
	private TransactionalFileOutputStream outFile;

	public CatProcess(String[] args) throws Exception {
		super(args);
		if (args.length != 2) {
			System.out.println("Usage: CatProcess <inputFile> <outputFile>");
			throw new Exception("Invalid arguments!");
		}

		try {
			inFile = new TransactionalFileInputStream(args[0]);
			outFile = new TransactionalFileOutputStream(args[1]);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		suspending = false;
		finished = false;
	}

	@Override
	public void run() {
		System.out.println("run start!!");
		System.out.println("run:  " + inFile);
		System.out.println("run:  " + outFile);
		System.out.println(suspending);

		PrintStream out = new PrintStream(outFile);
		System.out.println(suspending);
		DataInputStream in = new DataInputStream(inFile);
		System.out.println(suspending);

		try {
			while (!suspending) {
				String line = in.readLine();
				if (line == null) {
					finished = true;
					break;
				}

				System.out.println(line);
				out.println(line);

				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {

				}
			}
		} catch (EOFException e) {
			// TODO: handle exception
		} catch (IOException e) {
			// TODO: handle exception
		}
		/*
		 * make sure to write the last line we read to the output file before we
		 * close the file
		 */
		if (suspending) {
			System.out.println("Migrate!!!");
			try {
				inFile.migrate();
				outFile.migrate();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		suspending = false;
		// try {
		// in.close();
		// out.close();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

	@Override
	public void suspend() {
		suspending = true;
	}

	@Override
	public String toString() {
		return "CatProcess" + inFile;
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		try {
			inFile.resume();
			outFile.resume();
			suspending = false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean hasFinished() {
		return finished;
	}

}
