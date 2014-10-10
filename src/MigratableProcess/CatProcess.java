package MigratableProcess;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import TransactionalIO.TransactionalFileInputStream;
import TransactionalIO.TransactionalFileOutputStream;

/**
 * A migratable process that can read data in a file and then write
 * it to another file.
 * 
 * @author Xiaoxiang Wu(xiaoxiaw)
 * @author Ye Zhou
 */
public class CatProcess extends MigratableProcess {

	private TransactionalFileInputStream inFile;
	private TransactionalFileOutputStream outFile;

	public CatProcess(String[] args) throws Exception {
		super(args);
		if (args.length != 2) {
			System.out.println("Usage: CatProcess <inputFile> <outputFile>");
			throw new Exception("Invalid arguments!");
		}

		inFile = new TransactionalFileInputStream(args[0]);
		outFile = new TransactionalFileOutputStream(args[1]);
		
		suspending = false;
		finished = false;
	}

	@Override
	public void run() {
		System.out.println("run start!!");
		System.out.println("run:  " + inFile);
		System.out.println("run:  " + outFile);

		PrintStream out = new PrintStream(outFile);
		DataInputStream in = new DataInputStream(inFile);

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
		if (suspending && !finished) {
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
}
