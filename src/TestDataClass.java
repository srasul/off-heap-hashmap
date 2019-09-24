import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TestDataClass implements Serializable {
	private static final long serialVersionUID = -2940975500692010106L;

	private List<String> strings;
	private Map<String, Set<String>> map;

	public TestDataClass(int size) {
		strings = new ArrayList<String>();
		map = new HashMap<String, Set<String>>();

		for (int i = 0; i < size; i++) {
			strings.add(UUID.randomUUID().toString());
		}

		for (int i = 0; i < size; i++) {
			map.put(UUID.randomUUID().toString(), createSetOfSize(size));
		}
	}

	private Set<String> createSetOfSize(int size) {
		Set<String> set = new HashSet<String>();
		for (int i = 0; i < size; i++) {
			set.add(UUID.randomUUID().toString());
		}
		return set;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((map == null) ? 0 : map.hashCode());
		result = prime * result + ((strings == null) ? 0 : strings.hashCode());
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
		TestDataClass other = (TestDataClass) obj;
		if (map == null) {
			if (other.map != null)
				return false;
		} else if (!map.equals(other.map))
			return false;
		if (strings == null) {
			if (other.strings != null)
				return false;
		} else if (!strings.equals(other.strings))
			return false;
		return true;
	}

	public int getSize() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(out);
			oos.writeObject(this);
			oos.flush();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return out.toByteArray().length;

	}

	public static void main(String[] args) {

		TestDataClass initial = new TestDataClass(10);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(out);
			oos.writeObject(initial);
			oos.flush();

		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(out.toByteArray()));

			TestDataClass clonedInitial = (TestDataClass) is.readObject();

			System.out.println(initial.equals(clonedInitial));

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}
}
