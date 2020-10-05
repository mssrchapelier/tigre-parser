import com.google.gson.stream.JsonReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.io.IOException;

class ConfigurationBuilder {
	private String transliterationMapFilePath;
	private String verbParadigmFilePath;
	private String[] patternFilePaths;

	ConfigurationBuilder () {}

	ConfigurationBuilder readConfig (String filePath) throws IOException {
		try (JsonReader reader = new JsonReader(new FileReader(filePath))) {
			reader.beginObject();

			reader.nextName();
			this.transliterationMapFilePath = reader.nextString();

			reader.nextName();
			this.verbParadigmFilePath = reader.nextString();

			reader.nextName();
			ArrayList<String> patternFilePaths = new ArrayList<>();
			reader.beginArray();
			while (reader.hasNext()) {
				patternFilePaths.add(reader.nextString());
			}
			reader.endArray();
			this.patternFilePaths = patternFilePaths.toArray(new String[0]);

			reader.endObject();

			return this;
		}
	}

	String getTransliterationMapFilePath () {
		if (this.transliterationMapFilePath == null) {
			throw new IllegalStateException("transliterationMapFilePath has not been set; call readConfig() first");
		}
		return this.transliterationMapFilePath;
	}

	String getVerbParadigmFilePath () {
		if (this.verbParadigmFilePath == null) {
			throw new IllegalStateException("verbParadigmFilePath has not been set; call readConfig() first");
		}
		return this.verbParadigmFilePath;
	}

	String[] getPatternFilePaths () {
		if (this.patternFilePaths == null) {
			throw new IllegalStateException("patternFilePaths has not been set; call readConfig() first");
		}
		return this.patternFilePaths;
	}
}
