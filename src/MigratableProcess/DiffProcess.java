package MigratableProcess;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import TransactionalIO.TransactionalFileInputStream;
import TransactionalIO.TransactionalFileOutputStream;

/**
 * A Migratable process that will find difference between two files.
 * 
 * @author Xiaoxiang Wu(xiaoxiaw)
 * @author Ye Zhou
 */
public class DiffProcess extends MigratableProcess {

	private volatile boolean suspending;
	private boolean finished;
	private int lineNumber;
	private boolean file1_done;
	private boolean file2_done;

	private TransactionalFileInputStream inFile1;
	private TransactionalFileInputStream inFile2;
	private TransactionalFileOutputStream outFile;

	public DiffProcess(String[] args) throws Exception {
		super(args);

		if (args.length != 3) {
			System.out
					.println("Usage: DiffProcess <inputFile1> <inputFile2> <outputFile>");
			throw new Exception("Invalid arguments!");
		}

		inFile1 = new TransactionalFileInputStream(args[0]);
		inFile2 = new TransactionalFileInputStream(args[1]);
		outFile = new TransactionalFileOutputStream(args[2]);
		
		suspending = false;
		finished = false;
		lineNumber = 0;
		file1_done = false;
		file2_done = false;
	}

	/**
	 * Start a thread to compare two files.
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("run start!!");
		System.out.println("run:  " + inFile1);
		System.out.println("run:  " + inFile2);
		System.out.println("run:  " + outFile);

		PrintStream out = new PrintStream(outFile);
		DataInputStream in1 = new DataInputStream(inFile1);
		DataInputStream in2 = new DataInputStream(inFile2);

		String line1 = null, line2 = null;

		try {
			while (!suspending) {
				++lineNumber;
				if (!file1_done) {
					line1 = in1.readLine();
					if (line1 == null) {
						file1_done = true;
					}
				}
				if (!file2_done) {
					line2 = in2.readLine();
					if (line2 == null) {
						file2_done = true;
					}
				}

				/* finish with both files */
				if (file1_done && file2_done) {
					finished = true;
					break;
				}

				System.out.println("lineNumber: " + lineNumber);
				if (line1 == null) {  /* done with file1 */
					System.out.println("File2: " + line2);
					out.println("Line " + lineNumber);
					out.println("File2: " + line2);
					out.println();
				} else if (line2 == null) {  /* done with file2 */
					System.out.println("File1: " + line1);
					out.println("Line " + lineNumber);
					out.println("File1: " + line1);
					out.println();
				} else if (!line1.equals(line2)) {  /* lines in file1 & file2 are different */
					System.out.println("File1: " + line1);
					System.out.println("File2: " + line2);
					out.println("Line " + lineNumber);
					out.println("File1: " + line1);
					out.println("File2: " + line2);
					out.println();
				}

				try {
					Thread.sleep(15000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (EOFException e) {
			// TODO: handle exception
		} catch (IOException e) {
			// TODO: handle exception
		}
		if (suspending) {
			try {
				inFile1.migrate();
				inFile2.migrate();
				outFile.migrate();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		suspending = false;
	}

	@Override
	public void suspend() {
		// TODO Auto-generated method stub
		suspending = true;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "DiffProcess" + inFile1 + inFile2 + outFile;
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		try {
			inFile1.resume();
			inFile2.resume();
			outFile.resume();
			suspending = false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * If a job has finished yet
	 * @return true if the job has finished, false otherwise.
	 */
	public boolean hasFinished() {
		return finished;
	}

}
