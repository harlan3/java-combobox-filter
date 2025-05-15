package orbisoftware.comboboxfilter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CountriesDataAccess {

	public static List<TextEntry> getCountries() {

		List<TextEntry> list = new ArrayList<>();
		String filePath = "countries.txt";

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