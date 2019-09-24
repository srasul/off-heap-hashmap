import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

/**
 * entry where the value is nulled after it not accessed for some time
 * @author srasul
 *
 * @param <V>
 */
public class NullAfterSomeTimeValueEntry<V> extends MapEntryInFile<V> {
	private WeakReference<V> entry;
	private long lastSetOrGet = -1;
	
	public NullAfterSomeTimeValueEntry(int index, int length, V entry) {
		super(index, length, entry);
		this.entry = new WeakReference<V>(entry);
		setValue(entry);
	}

	@Override
	public void setValue(V value) {
		if(value != null) {
			lastSetOrGet = System.currentTimeMillis();
		}else {
			lastSetOrGet = -1;
			this.entry = null;
		}
		this.entry = new WeakReference<V>(value);
	}
	
	public boolean shouldClear(TimeUnit unit, Long olderThan) {
		if(lastSetOrGet == -1)
			return false;
		
		long age = System.currentTimeMillis() - lastSetOrGet;
		
		if(age > unit.toMillis(olderThan)) {
			return true;
		}
		return false; 
	}
	
	@Override
	public V getValue() {
		if(this.entry != null) {
			lastSetOrGet = System.currentTimeMillis();
			return this.entry.get();
		}
		
		return null;
	}

}
