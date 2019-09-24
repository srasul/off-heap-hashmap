import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JOptionPane;


public class BasicTest {
	public static void main(String[] args) {
		testRemoveAfterSomeTime();
	}

	private static void testEquality() {
		Map<String, TestDataClass> normalMap = new HashMap<String, TestDataClass>();

		OnDiskHashMap<String, TestDataClass> diskMap = new OnDiskHashMap<String, TestDataClass>("disk_map");

		JOptionPane.showInputDialog("start filling!");

		Random r = new Random();
		int len = 0;
		for (int i = 0; i < 10; i++) {
			TestDataClass testData = new TestDataClass(r.nextInt(50));
			String key = UUID.randomUUID().toString();
			normalMap.put(key, testData);
			diskMap.put(key, testData);
		}

		JOptionPane.showInputDialog("" + diskMap.size());

		for (String key : normalMap.keySet()) {

			TestDataClass fromFile = diskMap.get(key);
			TestDataClass inMap = normalMap.get(key);
			if (!inMap.equals(fromFile)) {
				System.out.println("ARE DIFFERENT!!!");
			}else {
//				System.out.println("values are same!!");
			}
		}
		
		System.out.println("All Done!");
		// System.out.println(prev.equals(td));
	}
	
	
	public static void testRemoveAfterSomeTime() {
		
		final Map<String, TestDataClass> normalMap = new HashMap<String, TestDataClass>();

		final OnDiskHashMap<String, TestDataClass> diskMap = new OnDiskHashMap<String, TestDataClass>("disk_map", ValueType.InMemoryWeakAndNullAfterTime);

		JOptionPane.showInputDialog("start filling!");

		Random r = new Random();
		int len = 0;
		for (int i = 0; i < 100; i++) {
			TestDataClass testData = new TestDataClass(r.nextInt(100));
			String key = UUID.randomUUID().toString();
			normalMap.put(key, testData);
			diskMap.put(key, testData);
		}
		
		System.gc();

		JOptionPane.showInputDialog("" + Runtime.getRuntime().freeMemory());
		System.out.println(Runtime.getRuntime().freeMemory());

		final AtomicInteger counter = new AtomicInteger(0);
		Runnable run = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				for (String key : normalMap.keySet()) {

					TestDataClass fromFile = diskMap.get(key);
					TestDataClass inMap = normalMap.get(key);
					if (!inMap.equals(fromFile)) {
						System.out.println("ARE DIFFERENT!!!");
					}else {
//						System.out.println("values are same!!");
					}
				}

				counter.incrementAndGet();
				
				if(counter.get() > 10) {
					System.out.println("about to stop checking...");
					throw new RuntimeException();
				}
			}
		};
		
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(run, 0, 1, TimeUnit.SECONDS);
		
		
		System.out.println("All Done!");
		// System.out.println(prev.equals(td));
	}
	
}
