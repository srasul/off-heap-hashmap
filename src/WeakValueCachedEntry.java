import java.lang.ref.WeakReference;

public class WeakValueCachedEntry<V> extends MapEntryInFile<V> {

	private WeakReference<V> entry;

	public WeakValueCachedEntry(int index, int length, V entry) {
		super(index, length, entry);
		setValue(entry);
	}

	@Override
	public void setValue(V value) {
		this.entry = new WeakReference<V>(value);
	}

	@Override
	public V getValue() {
		if (this.entry != null)
			return this.entry.get();
		else
			return null;
	}

}
