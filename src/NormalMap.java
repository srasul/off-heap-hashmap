import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.swing.JOptionPane;


public class NormalMap {

	public static void main(String[] args) {
			Map<String, TestDataClass> normalMap = new HashMap<String, TestDataClass>();
			
			JOptionPane.showMessageDialog(null, "about to fill data");
			
			for (int i = 0; i < 500; i++) {
				TestDataClass testData = new TestDataClass(50);
				String key = UUID.randomUUID().toString();
				normalMap.put(key, testData);
			}
			
			System.gc();
			
			JOptionPane.showMessageDialog(null, "filled data. Map has now: " + normalMap.size() + " items");
		}

}
