package Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import MigratableProcess.DiffProcess;

public class TestDiffProcess {

	public static void main(String[] args) throws InterruptedException {
		DiffProcess diffProcess = null;
		try {
			diffProcess = new DiffProcess(args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Thread t1 = new Thread(diffProcess);
		t1.start();
		
		try {
			Thread.sleep(3000);
			System.out.println("before suspend: " + diffProcess);
			diffProcess.suspend();
			t1.join();
			t1 = null;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			System.out.println("before serializing!");
			FileOutputStream fos = new FileOutputStream("DiffProcess.ser");
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(diffProcess);
			System.out.println(diffProcess);
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
			FileInputStream fis = new FileInputStream("DiffProcess.ser");
			ObjectInputStream in = new ObjectInputStream(fis);
			diffProcess = (DiffProcess)in.readObject();
			System.out.println("Get back the object!");
			System.out.println("Resumed Object:  " + diffProcess);
			diffProcess.resume();
			t1 = new Thread(diffProcess);
			t1.start();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
