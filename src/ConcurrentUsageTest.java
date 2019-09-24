import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ConcurrentUsageTest {
	ConcurrentMap<String, TestDataClass> testDataMap = new ConcurrentHashMap<String, TestDataClass>();
	Map<String, TestDataClass> onFileHashMap;
	ExecutorService executorService = Executors.newCachedThreadPool();

	public ConcurrentUsageTest(String mapName) {
		fillTestDate();
		onFileHashMap = new OnDiskHashMap<String, TestDataClass>(mapName);
	}

	void fillTestDate() {
		Random r = new Random();
		for (int i = 0; i < 1000; i++) {
			TestDataClass testData = new TestDataClass(r.nextInt(10));
			String key = UUID.randomUUID().toString();
			testDataMap.put(key, testData);
		}
	}

	private class WriteRunnable implements Runnable {
		private String key;
		private TestDataClass value;
		private Long writeTook;

		public WriteRunnable(String key, TestDataClass value) {
			super();
			this.key = key;
			this.value = value;
		}

		@Override
		public void run() {
			long before = System.currentTimeMillis();
			onFileHashMap.put(key, value);
			writeTook = System.currentTimeMillis() - before;
		}

		public Long getWriteTookTime() {
			return writeTook;
		}
	}

	private WriteRunnable createWriteRunnable(final String key, final TestDataClass value) {
		return new WriteRunnable(key, value);
	}

	private class ReadCallable implements Callable<TestDataClass> {
		private String key;
		private TestDataClass expectedValue;

		private ReadCallable(String key, TestDataClass expectedValue) {
			this.key = key;
			this.expectedValue = expectedValue;
		}

		@Override
		public TestDataClass call() throws Exception {
			return onFileHashMap.get(key);
		}

		public TestDataClass getExpectedValue() {
			return this.expectedValue;
		}

	}

	private ReadCallable createReadCallable(final String key, TestDataClass expectedValue) {
		return new ReadCallable(key, expectedValue);
	}

	public void doParallelWrite() {
		Set<Future<?>> futures = new HashSet<Future<?>>();
		for (String key : this.testDataMap.keySet()) {
			WriteRunnable runnable = createWriteRunnable(key, this.testDataMap.get(key));
			Future<?> future = executorService.submit(runnable);
			futures.add(future);
		}

		for (Future<?> f : futures) {
			try {
				f.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}

		System.out.println(this.testDataMap.equals(this.onFileHashMap));
	}

	public void doParallelReads() {
		Set<Future<TestDataClass>> futures = new HashSet<Future<TestDataClass>>();
		for (String key : this.testDataMap.keySet()) {
			ReadCallable callable = new ReadCallable(key, this.testDataMap.get(key));
			Future<TestDataClass> future = executorService.submit(callable);

			TestDataClass testDataClassRead;
			try {
				testDataClassRead = future.get();
				TestDataClass expected = this.testDataMap.get(key);
				if (!testDataClassRead.equals(expected)) {
					System.out.println("Got the unexpected!");
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Reads all done!");
	}

	public static void main(String[] args) {
		ConcurrentUsageTest concurrentUsageTest = new ConcurrentUsageTest("map1");
		concurrentUsageTest.doParallelWrite();
		concurrentUsageTest.doParallelReads();
		
		concurrentUsageTest = new ConcurrentUsageTest("map2");
		concurrentUsageTest.doParallelWrite();
		concurrentUsageTest.doParallelReads();
		
		concurrentUsageTest = new ConcurrentUsageTest("map3");
		concurrentUsageTest.doParallelWrite();
		concurrentUsageTest.doParallelReads();

	}
}
