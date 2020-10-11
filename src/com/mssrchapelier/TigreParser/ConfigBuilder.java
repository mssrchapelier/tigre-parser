package com.mssrchapelier.TigreParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.google.gson.stream.JsonReader;
import com.mssrchapelier.TigreParser.components.VerbProcessor.VerbProcessor;
import com.mssrchapelier.TigreParser.components.utils.misc.ConfigParseException;

class ConfigBuilder {
	private final static String DEFAULT_CONFIG_FILE_PATH_JAR = "/res/config.json";
	private final static String DEFAULT_CONFIG_FILE_PATH_FILESYSTEM = "res/config.json";
	
	// If true, this object should be reading resources using resource-based methods.
	// If false, this object should be reading resources using File-based methods.
	private final boolean isReadingResources;
	private final String configFilePath;
	
	private String transliterationMapFilePath;
	private String verbParadigmFilePath;
	private ArrayList<String> patternFilePaths;

	ConfigBuilder (String configFilePath) throws IOException {
		this.isReadingResources = false;
		this.configFilePath = new File(configFilePath).getPath();
		this.readConfig();
	}
	
	ConfigBuilder () throws IOException {
		boolean resourceExists = (this.getClass().getResource(DEFAULT_CONFIG_FILE_PATH_JAR) != null);
		if (resourceExists) {
			this.isReadingResources = true;
			this.configFilePath = DEFAULT_CONFIG_FILE_PATH_JAR;
		} else {
			this.isReadingResources = false;
			File configFile = new File(DEFAULT_CONFIG_FILE_PATH_FILESYSTEM);
			if (!configFile.exists()) {
				throw new IOException("Failed to locate the configuration file");
			}
			this.configFilePath = configFile.getPath();
		}
		this.readConfig();
	}
	
	private InputStream getStream (String filePath) throws IOException {
		if (this.isReadingResources) {
			return this.getClass().getResourceAsStream(filePath);
		} else {
			return new FileInputStream(filePath);
		}
	}
	
	private String setPath (String filePath) {
		String path = Paths.get(this.configFilePath)
							.getParent()
							.resolve(filePath)
							.toString();
		if (this.isReadingResources) {
			path = path.replace("\\", "/");
		}
		return path;
	}
	
	private void readConfig () throws IOException {
		try (
				InputStream inputStream = this.getStream(this.configFilePath);
				InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
				JsonReader reader = new JsonReader(inputStreamReader)
			) {
			reader.beginObject();
			
			reader.nextName();
			this.transliterationMapFilePath = this.setPath(reader.nextString());
			
			reader.nextName();
			this.verbParadigmFilePath = this.setPath(reader.nextString());
			
			reader.nextName();
			
			this.patternFilePaths = new ArrayList<>();
			reader.beginArray();
			while (reader.hasNext()) {
				String patternFilePath = this.setPath(reader.nextString());
				this.patternFilePaths.add(patternFilePath);
			}
			reader.endArray();
			
			reader.endObject();
		}
	}
	
	WordProcessor constructWordProcessor () throws IOException, ConfigParseException {
		Transliterator transliterator = this.constructTransliterator();
		Geminator geminator = new Geminator();
		PatternProcessor patternProcessor = this.constructPatternProcessor();
		VerbProcessor verbProcessor = this.constructVerbProcessor();
		
		return new WordProcessor.WordProcessorBuilder()
								.setTransliterator(transliterator)
								.setGeminator(geminator)
								.setPatternProcessor(patternProcessor)
								.setVerbProcessor(verbProcessor)
								.build();
	}
	
	private Transliterator constructTransliterator () throws IOException, ConfigParseException {
		if (this.transliterationMapFilePath == null) {
			throw new IllegalStateException("transliteration map file path is null");
		}
		
		try (InputStream stream = this.getStream(this.transliterationMapFilePath)) {
			return new Transliterator(stream);
		} catch (ConfigParseException cause) {
			String message = String.format("Failed to read file: %s%n", this.transliterationMapFilePath);
			throw new ConfigParseException(message, cause);
		}
	}
	
	private VerbProcessor constructVerbProcessor () throws IOException, ConfigParseException {
		if (this.verbParadigmFilePath == null) {
			throw new IllegalStateException("verb paradigm file path is null");
		}
		
		try (InputStream stream = this.getStream(this.verbParadigmFilePath)) {
			return new VerbProcessor(stream);
		} catch (ConfigParseException cause) {
			String message = String.format("Failed to read file: %s%n", this.verbParadigmFilePath);
			throw new ConfigParseException(message, cause);
		}
	}
	
	private PatternProcessor constructPatternProcessor () throws IOException, ConfigParseException {
		if (this.patternFilePaths == null) {
			throw new IllegalStateException("pattern file path array is null");
		}
		
		PatternProcessor.PatternProcessorBuilder processorBuilder = new PatternProcessor.PatternProcessorBuilder();
		
		for (String path : this.patternFilePaths) {
			
			try (InputStream stream = this.getStream(path)) {
				processorBuilder.readLevel(stream);
			} catch (ConfigParseException cause) {
				String message = String.format("Failed to read file: %s%n", path);
				throw new ConfigParseException(message, cause);
			}
		}
		
		return processorBuilder.build();
	}
}
