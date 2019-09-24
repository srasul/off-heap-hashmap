
public class MapEntryInFile<T> implements Comparable<MapEntryInFile<T>> {
	private int index;
	private int length;
	

	public int getIndex() {
		return index;
	}

	public int getLength() {
		return length;
	}

	public MapEntryInFile(int index, int length, T entry) {
		super();
		this.index = index;
		this.length = length;
	}
	
	public MapEntryInFile<T> setValues(int index, int length) {
		this.index = index;
		this.length = length;
		
		return this;
	}

	@Override
	public String toString() {
		return "MapEntryInFile [index=" + index + ", length=" + length + "]";
	}
	
	@Override
	public int compareTo(MapEntryInFile<T> o) {
		int toReturn = Integer.valueOf(this.index).compareTo(Integer.valueOf(o.index));
		if (toReturn == 0) {
			return Integer.valueOf(this.length).compareTo(Integer.valueOf(o.length));
		} else {
			return toReturn;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + index;
		result = prime * result + length;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MapEntryInFile<?> other = (MapEntryInFile<?>) obj;
		if (index != other.index)
			return false;
		if (length != other.length)
			return false;
		return true;
	}

	public void setValue(T o) {
		
	}

	public Object getValue() {
		return null;
	}
	
}
