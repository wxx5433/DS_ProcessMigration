package MigratableProcess;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class TestCatProcess {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CatProcess cp = null;
		try {
			cp = new CatProcess(args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Thread t1 = new Thread(cp);
		
		t1.start();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Before suspend:  " + cp);
		
		try {
			cp.suspend();
			t1.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		t1 = null;
		
		
		
		try {
			System.out.println("before serializing!");
			FileOutputStream fos = new FileOutputStream("CatProcess.ser");
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(cp);
			System.out.println("after serializing!");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		try {
			FileInputStream fis = new FileInputStream("CatProcess.ser");
			ObjectInputStream in = new ObjectInputStream(fis);
			cp = (CatProcess)in.readObject();
			System.out.println("Get back the object!");
			cp.resume();
			System.out.println("Resumed Object:  " + cp);
			t1 = new Thread(cp);
			t1.start();
			System.out.println("Job finish? : " + cp.hasFinished());
			Thread.sleep(10000);
			System.out.println("Job finish? : " + cp.hasFinished());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
