package TransactionalIO;

import java.io.FileNotFoundException;
import java.io.IOException;

public class TestOutput {

	public static void main(String[] args) {
		TransactionalFileOutputStream tfos = null;
		try {
			tfos = new TransactionalFileOutputStream("testWrite");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
			System.out.println(tfos);
			tfos.write('h');
			tfos.migrate();
			System.out.println(tfos);
			tfos.write('i');
			tfos.write('\n');
			System.out.println(tfos);
			byte[] word = "hello!".getBytes();
			tfos.write(word);
			System.out.println(tfos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

}
