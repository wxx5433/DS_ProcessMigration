package TransactionalIO;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * This class is a stream that reads byte from a file, which supports to be migrated to 
 * other machines, and resumes reading.
 * 
 * @author Xiaoxiang Wu(xiaoxiaw)
 * @author Ye Zhou
 */
public class TransactionalFileInputStream extends InputStream implements Serializable{
	/**
	 * The name that <code>TransactionalFileInputStream</code> object should read from.
	 */
	private String filename;
	
	/**
	 * The <code>FileInputStream</code> object used internally to implement reading operation.
	 * This attribute will not be serialized.
	 */
	private transient FileInputStream fis;
	
	/**
	 * This is position of the file pointer, which means the next character to be read.
	 */
	private long offset;
	
	/**
	 * To show if the <code>TransactionalFileInputStream</code> has just been migrated.
	 */
	private boolean migrated;
	
	/**
	 * This method initializes a <code>TransactionFileInputStream</code> to read from 
	 * a specific named file.
	 * 
	 * @param filename The name of the file this stream should read from
	 * @throws FileNotFoundException If the file does not exist
	 */
	public TransactionalFileInputStream(String filename) throws FileNotFoundException {
		this.filename = filename;
		fis = new FileInputStream(filename);
		offset = 0;
		migrated = false;
	}
	
	/**
	 * This method reads a byte from the input stream, and updates the offset of 
	 * the file pointer. 
	 * 
	 * @exception IOException If an error occurs when reading from the input stream
	 * @return The byte read from the stream
	 */
	@Override
	public int read() throws IOException {
		int val = fis.read();
		++offset;	
		return val;
	}

	/**
	 * This method closes the stream. An IOException will occur if continue to read from
	 * this stream.
	 * 
	 * @exception IOException If an error occurs when closing the stream
	 */
	@Override
	public void close() throws IOException {
		filename = null;
		offset = 0;
		fis.close();
	}

	/**
	 * This method skip specified number of bytes from the stream.
	 * 
	 * @param n The number of bytes attempt to skip
	 * @exception IOException If an error occurs when skipping the stream
	 * @return The actual number of bytes skipped
	 */
	@Override
	public long skip(long n) throws IOException {
		offset += n;
		return fis.skip(n);
	}
	
	/**
	 * This method provides a string representation of the object.
	 * 
	 * @return The string representation of the object
	 */
	@Override
	public String toString() {
		return "[filename: " + filename + ", offset: " + offset + "]";
	}

	/**
	 * This method puts the <code>TransactionalFileInputStream</code> object
	 * into a safe state, so that it can be migrated.
	 * 
	 * @throws IOException If an error occurs
	 */
	public void migrate() throws IOException {
		fis.close();
		migrated = true;
	}
	
	/**
	 * This methods reopen the <code>FileInputStream</code> object, and set 
	 * the file pointer to where it was when <code>migrate()</code> was called.
	 * 
	 * @throws IOException If an error occurs
	 */
	public void resume() throws IOException {
		fis = new FileInputStream(filename);
		fis.skip(offset);
		migrated = false;
	}

}
	
