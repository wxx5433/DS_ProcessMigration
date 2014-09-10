package Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import TransactionalIO.TransactionalFileInputStream;

public class TestInput {

	public static void main(String[] args) {
		TransactionalFileInputStream tfs = null;
		try {
			tfs = new TransactionalFileInputStream("/Users/wxx/test");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		/* read from the file */
		try {
			System.out.print((char)tfs.read());
			System.out.print((char)tfs.read());
			System.out.print((char)tfs.read());
			System.out.print((char)tfs.read());
			System.out.print((char)tfs.read());
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println();
		System.out.println(tfs);
		
		/* migrate and resume */
		try {
			tfs.migrate();
			tfs.resume();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/* test if reading from the stream is correct after migration */
		try {
			int ch;
			tfs.skip(1);  /* test offset attribute */
			System.out.println(tfs);
			
			/* migrate again */
			tfs.migrate();
			tfs.resume();
			System.out.println(tfs);
			
			/* read remaining characters */
			while ((ch = tfs.read()) != -1) {
				System.out.print((char)ch);
			}
			tfs.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
