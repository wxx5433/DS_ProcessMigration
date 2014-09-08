package TransactionalIO;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;

/**
 * This class is a stream that writes byte to a file, which supports to be migrated to 
 * other machines, and resumes reading.
 * 
 * @author Xiaoxiang Wu(xiaoxiaw)
 * @author Ye Zhou
 */
public class TransactionalFileOutputStream extends OutputStream implements Serializable{
	
	/**
	 * The name that <code>TransactionalFileInputStream</code> object should write to.
	 */
	private String filename;
	
	/**
	 * The <code>RandomAccessFile</code> object used internally to implement writing operation.
	 * This attribute will not be serialized.
	 */
	private transient RandomAccessFile randomAccessFile;

	/**
	 * This is position of the file pointer, which means the next character to be wrote.
	 */
	private long offset;
	
	/**
	 * To show if the <code>TransactionalFileOutputStream</code> has just been migrated.
	 */
	private boolean migrated;
	
	/**
	 * This method initializes a <code>TransactionFileOutputStream</code> to write to 
	 * a specific named file.
	 * 
	 * @param filename The name of the file this stream should write to
	 * @throws FileNotFoundException If the file does not exist
	 */
	public TransactionalFileOutputStream(String filename) throws FileNotFoundException {
		this.filename = filename;
		randomAccessFile = new RandomAccessFile(filename, "rw");
		offset = 0;
		migrated = false;
	}

	/**
	 * This method writes a byte to the output stream, and updates the offset of 
	 * the file pointer. 
	 * 
	 * @param val The byte value written to output stream
	 * @exception IOException If an error occurs 
	 */
	@Override
	public void write(int val) throws IOException {
		randomAccessFile.write(val);
		++offset;
	}

	/**
	 * This method closes the stream. An IOException will occur if continue to write to
	 * this stream.
	 * 
	 * @exception IOException If an error occurs when closing the stream
	 */
	@Override
	public void close() throws IOException {
		offset = 0;
		filename = null;
		randomAccessFile.close();
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
	 * This method puts the <code>TransactionalFileOutputStream</code> object
	 * into a safe state, so that it can be migrated.
	 * 
	 * @throws IOException If an error occurs
	 */
	public void migrate() throws IOException {
		randomAccessFile.close();
		migrated = true;
	}
	
	/**
	 * This methods reopen the <code>RandomAccessFile</code> object, and set 
	 * the file pointer to where it was when <code>migrate()</code> was called.
	 * 
	 * @throws IOException If an error occurs
	 */
	public void resume() {
		try {
			randomAccessFile = new RandomAccessFile(filename, "rw");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			randomAccessFile.seek(offset);
		} catch (IOException e) {
			e.printStackTrace();
		}
		migrated = false;
	}
}
