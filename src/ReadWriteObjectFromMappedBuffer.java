import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

public class ReadWriteObjectFromMappedBuffer {
	static int length = Integer.MAX_VALUE / 2;

	private int currentPosition;
	private String mapName;
	private ByteBuffer mappedFileBuffer;

	private ByteBuffer createByteBuffer() throws IOException {
		ByteBuffer mappedByteBuffer = null;
		RandomAccessFile mappedFile;

		File f = new File("/tmp/values_ReadWriteObjectFromMappedBuffer_" + mapName);
		
		if(f.exists())
			throw new IOException("file: '" + f.getCanonicalPath() + " for this map-name: '"+this.mapName+"' already exsits. choose another map-name");
		System.out.println(f.getName());
		f.deleteOnExit();

		try {
			mappedFile = new RandomAccessFile(f, "rw");
			FileChannel fileChannel = mappedFile.getChannel();

			mappedByteBuffer = fileChannel.map(MapMode.READ_WRITE, 0, length);
			fileChannel.close();
			mappedFile.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}

		return mappedByteBuffer;
	}

	public ReadWriteObjectFromMappedBuffer(String mapName) throws IOException {
		currentPosition = 0;
		this.mapName = mapName;
		this.mappedFileBuffer = createByteBuffer();
	}

	public MapEntryInFile<?> writeToFile(Object o, MapEntryInFile<?> mapentry) throws IOException {
		byte[] asByteArray = getAsByteArray(o);

		int end = asByteArray.length;
		int startWritingPosition = -1;
		
		synchronized (this) {

			startWritingPosition = currentPosition;

			try {
				this.mappedFileBuffer.position(currentPosition);
			} catch (Exception e) {
				e.printStackTrace();
			}

			for (byte b : asByteArray) {
				this.mappedFileBuffer.put(b);
			}

			currentPosition += (end);
		}

		// System.out.println("written : " + currentPosition);
		// System.out.println("left    : " + (length - currentPosition));
		// System.out.println("b.capacity: " +
		// this.mappedFileBuffer.capacity());
		// System.out.println("b.position: " +
		// this.mappedFileBuffer.position());
		// System.out.println("b.remaining " +
		// this.mappedFileBuffer.remaining());
		// System.out.println("b.limit:    " + this.mappedFileBuffer.limit());
		return mapentry.setValues(startWritingPosition, end);
	}

	public Object readFromFile(MapEntryInFile mapEntry) throws IOException, ClassNotFoundException {

		if (mapEntry == null)
			return null;

		Object value = mapEntry.getValue();

		if (value != null)
			return value;

		byte[] data = null;
		synchronized (this) {
			data = new byte[mapEntry.getLength()];
			this.mappedFileBuffer.position(mapEntry.getIndex());
			this.mappedFileBuffer.get(data, 0, mapEntry.getLength());
		}
		Object o = fromByteArray(data);
		mapEntry.setValue(o);
		return o;
	}

	private Object fromByteArray(byte[] data) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		ObjectInputStream ois = new ObjectInputStream(bais);
		Object o = ois.readObject();
		return o;
	}

	private byte[] getAsByteArray(Object o) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(o);
		oos.flush();
		byte[] toReturn = bos.toByteArray();
		oos.close();
		bos.close();
		return toReturn;
	}
}
