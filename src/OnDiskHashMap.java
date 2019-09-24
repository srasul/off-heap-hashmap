import java.io.IOException;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OnDiskHashMap<K, V> extends AbstractMap<K, V> implements Runnable {

	private ConcurrentMap<K, MapEntryInFile<V>> diskReferenceMap = new ConcurrentHashMap<K, MapEntryInFile<V>>();
	private ReadWriteObjectFromMappedBuffer diskWriter = null;
	private ValueType valueHolderType = ValueType.NeverInMemory;
	private static final ScheduledExecutorService CHECKING_OLD_VALUES_EXECUTOR = Executors.newSingleThreadScheduledExecutor();
	private static final long clear_older_than = 5;
	private static final TimeUnit older_than_unit = TimeUnit.SECONDS;
	
	private class ValuesCollection implements Collection<V> {

		@Override
		public int size() {
			return size();
		}

		@Override
		public boolean isEmpty() {
			return OnDiskHashMap.this.isEmpty();
		}

		@Override
		public boolean contains(Object o) {
			return false;
		}

		@Override
		public Iterator<V> iterator() {
			return new Iterator<V>() {
				private Iterator<K> keyIterator = keySet().iterator();

				@Override
				public boolean hasNext() {
					return keyIterator.hasNext();
				}

				@Override
				public V next() {
					K key = keyIterator.next();
					return (V) get(key);
				}

				@Override
				public void remove() {
					// not supported
				}
			};
		}

		@Override
		public Object[] toArray() {
			return null;
		}

		@Override
		public <T> T[] toArray(T[] a) {
			return null;
		}

		@Override
		public boolean add(V e) {
			return false;
		}

		@Override
		public boolean remove(Object o) {
			return false;
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return false;
		}

		@Override
		public boolean addAll(Collection<? extends V> c) {
			return false;
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			return false;
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			return false;
		}

		@Override
		public void clear() {
			OnDiskHashMap.this.clear();
		}

	}

	public OnDiskHashMap(String name) {
		this(name, ValueType.NeverInMemory);
	}
	
	public OnDiskHashMap(String name, ValueType valueType) {
		try {
			this.valueHolderType = valueType;
			if(this.valueHolderType == ValueType.InMemoryWeakAndNullAfterTime) {
				CHECKING_OLD_VALUES_EXECUTOR.scheduleWithFixedDelay(this, 5, 5, TimeUnit.SECONDS);
			}
			diskWriter = new ReadWriteObjectFromMappedBuffer(name);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return null;
	}

	@Override
	public V put(K key, V value) {
		
		if(key == null) {
			throw new NullPointerException("key is null. It can't be null");
		}
		
		if(value == null) {
			throw new NullPointerException("value is null. It can't be null");
		}

		if (diskReferenceMap.putIfAbsent(key, createNewMapEntry(value)) == null) {
			// first entry
			MapEntryInFile<V> mapEntry = diskReferenceMap.get(key);
			try {
				mapEntry = (MapEntryInFile<V>) diskWriter.writeToFile(value, mapEntry);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			return null;
		} else {
			V prevValue = remove(key);
			if (prevValue != null) {
				this.put(key, value);
				return prevValue;
			} else {
				return null;
			}
		}
	}



	private MapEntryInFile<V> createNewMapEntry(V value) {
		if(valueHolderType == ValueType.InMemoryWeakAndNullAfterTime) {
			return new NullAfterSomeTimeValueEntry<V>(-1, -1, value);
		}else if(valueHolderType == ValueType.InMemoryWeakValue) {
			return new WeakValueCachedEntry<V>(-1, -1, value);
		}else {
			return new MapEntryInFile<V>(-1, -1, value);
		}
	}

	@Override
	public V remove(Object key) {
		MapEntryInFile mapEntry = diskReferenceMap.get(key);

		if (diskReferenceMap.remove(key, mapEntry)) {
			V prevValue = null;
			try {
				prevValue = (V) diskWriter.readFromFile(mapEntry);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			return prevValue;

		} else {
			return null;
		}

	}

	@Override
	public V get(Object key) {
		MapEntryInFile inFile = diskReferenceMap.get(key);

		V fromFile;
		try {
			fromFile = (V) diskWriter.readFromFile(inFile);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		return fromFile;
	}

	@Override
	public Set<K> keySet() {
		return new HashSet<K>(this.diskReferenceMap.keySet());
	}

	@Override
	public Collection<V> values() {
		return new ValuesCollection();
	}

	@Override
	public int size() {
		return this.diskReferenceMap.size();
	}

	@Override
	public boolean isEmpty() {
		return this.diskReferenceMap.isEmpty();
	}

	@Override
	public void clear() {
		for(K key: this.diskReferenceMap.keySet()) {
			this.remove(key);
		}
	}
	
	@Override
	public boolean containsKey(Object key) {
		return this.diskReferenceMap.containsKey(key);
	}
	
	@Override
	public boolean containsValue(Object value) {
		for(K key : keySet()) {
			V v = get(key);
			if(v.equals(value))
				return true;
		}
		return false;
	}

	@Override
	public void run() {
		for(Entry<K, MapEntryInFile<V>> entry : this.diskReferenceMap.entrySet()) {
			MapEntryInFile<V> mapEntryInFile = entry.getValue();
			
			if(mapEntryInFile instanceof NullAfterSomeTimeValueEntry) {
				NullAfterSomeTimeValueEntry<V> nullAfterSomeTimeValueEntry = (NullAfterSomeTimeValueEntry<V>)mapEntryInFile;
				
				if(nullAfterSomeTimeValueEntry.shouldClear(older_than_unit, clear_older_than)) {
					System.out.println("key: " + entry.getKey() + " is old. will be removed!");
					nullAfterSomeTimeValueEntry.setValue(null);
				}
			}
		}
	}
}
