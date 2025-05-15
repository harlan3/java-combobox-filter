package orbisoftware.comboboxfilter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StateNamesDataAccess {

	public static List<TextEntry> getStateNames() {

		List<TextEntry> list = new ArrayList<>();
		String filePath = "state_names.txt";

		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

			String line;

			while ((line = reader.readLine()) != null) {

				TextEntry e = new TextEntry();
				e.setName(line);
				list.add(e);
			}
		} catch (IOException e) {
			System.err.println("Error reading file: " + e.getMessage());
		}

		return list;
	}
}