package com.mssrchapelier.TigreParser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Objects;

import com.mssrchapelier.TigreParser.components.utils.misc.ConfigParseException;
import com.mssrchapelier.TigreParser.components.utils.word.WordAnalysis;
import com.mssrchapelier.TigreParser.components.utils.word.WordEntry;

/**
 * <p>
 * The main class of the application which performs morphological analysis on Tigre words, as well as text fragments and files (with tokenisation).
 * The application's public API is mainly (apart from the standalone command-line interface {@link com.mssrchapelier.TigreParser.TigreParserCliLauncher TigreParserCliLauncher}) provided by this class.
 * </p>
 * 
 * <p>
 * To perform morphological analysis, client code should first instantiate the {@code TigreParser} class by calling the static method {@link #builder() builder()}. Two parameters may then be specified (both are optional):
 * <ul>
 * <li>
 * To specify the <strong>maximum number of analyses</strong> to return for each word (when calling the {@code TigreParser} instance's {@link #processFile(String, String) processFile}, {@link #analyseLine(String) analyseLine}
 * or {@link #analyseWord(String) analyseWord} methods), call the builder object's {@link TigreParserBuilder#setMaxAnalyses(int) setMaxAnalyses(int)} method.
 * If the maximum number of analyses is not specified, all non-empty analyses will be returned by the methods mentioned above.
 * </li>
 * <li>
 * To specify the path to a <strong>configuration file</strong>, call the {@link TigreParserBuilder#setConfigFilePath(String) setConfigFilePath(String)} method on the builder instance.
 * If the path to the configuration file is not specified, the default value, which equals {@code /res/config.json} (the resource inside the {@code .jar} file) or {@code res/config.json}
 * (if the resource is not present in the {@code .jar} file), will be used.
 * </li>
 * </ul>
 * To build the {@code TigreParser} instance, call {@link TigreParserBuilder#build() build()} on the builder object. This method throws an {@link java.io.IOException IOException} if one of the required configuration files could not be found,
 * or a {@link java.text.ParseException ParseException} if one of the files is malformed (see the exception's stack trace for details about where this occurred).
 * </p>
 * 
 * <p>
 * The maximum number of analyses to show for each word may be changed after instantiating the class by calling the {@link #setMaxAnalyses(int) setMaxAnalyses(int)} method on the instance.
 * The argument must be a non-negative integer; if set to 0, all non-empty analyses will be returned for each token.
 * </p>
 * 
 * <p>
 * Three public methods are available for morphological processing:
 * <ul>
 * <li>
 * To perform morphological analysis on a <strong>single word</strong>, call {@link #analyseWord(String) analyseWord(String)}, passing the word as the method's argument.
 * A two-dimensional {@code String} array will be returned: {@code String[k][2]}, where {@code k} is the number of analyses produced,
 * the individual analysis being a {@code String[2]}, with the zeroth element representing the <em>surface form</em> of the word
 * and the first element representing the corresponding <em>gloss</em> (both with morpheme boundaries marked as {@code "-"}). If some part of the word was not analysed
 * in an analysis, the corresponding part will be enclosed in square brackets ({@code [...]}) in the surface form and represented as {@code "#"} in the gloss.
 * </li>
 * 
 * <li>
 * To tokenise and perform analysis on a {@code String} containing a sequence of Tigre words (i. e. a <strong>sentence</strong> or another text fragment), call {@link #analyseLine(String) analyseLine(String)},
 * passing the {@code String} as the method's argument. A three-dimensional {@code String} array will be returned: {@code String[n][k_i][2]}, where {@code n} is
 * the number of tokens that the input {@code String} was split into, each element being an array of analyses for the corresponding word as described above.
 * </li>
 * 
 * <li>
 * To process a <strong>text file</strong> (i. e. tokenise it into words and perform analysis on all of them), call {@link #processFile(String, String) processFile(String, String)},
 * passing the path to the <em>input file</em> as the first argument and the path to the <em>output file</em> as the second argument. This method may throw an {@link java.io.IOException IOException}.
 * </li>
 * </ul>
 * </p>
 * 
 * <p>
 * All of the mentioned methods perform null checks on their respective {@code String} arguments using {@link Objects#requireNonNull(Object) Objects.requireNonNull}. Passing a null {@code String} to these methods
 * will thus result in a {@code NullPointerException} being thrown, which must then be handled by the calling code.
 * </p>
 * 
 * <p>
 * Passing {@code 0} to one of the constructors which accept an {@code int} as an argument (which is the same as leaving unspecified the maximum number of analyses for each word since {@code DEFAULT_MAX_ANALYSES}
 * is set to {@code 0}) will result in all non-empty analyses being returned for the word (by {@code analyseWord}) or for each token (by {@code analyseLine} or {@code processFile}), as mentioned above.
 * </p>
 * 
 */

public class TigreParser {
	private static final int DEFAULT_MAX_ANALYSES = 0;
	
	private ConfigBuilder configBuilder;

	private Tokeniser tokeniser;
	private WordProcessor wordProcessor;
	
	int maxAnalyses;
	
	public static TigreParserBuilder builder () {
		return new TigreParserBuilder();
	}
	
	public void setMaxAnalyses (int maxAnalyses) {
		if (maxAnalyses <= 0) {
			this.maxAnalyses = DEFAULT_MAX_ANALYSES;
		} else {
			this.maxAnalyses = maxAnalyses;
		}
	}

	private TigreParser () {}

	public void processFile (String inputPath, String outputPath) throws IOException {
		// process silently (nothing sent to System.out)
		this.processFile(inputPath, outputPath, false);
	}

	public void processFile (String inputPath, String outputPath, boolean printNotifications) throws IOException {
		Objects.requireNonNull(inputPath);
		Objects.requireNonNull(outputPath);
		
		if (printNotifications) {
			String message = String.format("Processing file: %s", inputPath);
			System.out.println(message);
		}

		ArrayList<String> lines = this.readInputLines(inputPath);

		try (PrintWriter writer = new PrintWriter(outputPath, "UTF-8")) {
			this.writeFileHeader(writer);

			int curLineNumber = 0;

			for (String line : lines) {

				curLineNumber++;
				if (printNotifications) {
					String message = String.format("Processing line: %d out of %d",
									curLineNumber, lines.size());
					System.out.println(message);
				}

				ArrayList<WordEntry> wordEntries = this.processLine(line);
				this.writeLine(writer, wordEntries);
			}

			if (printNotifications) {
				String message = String.format("Processing completed, output written to file: %s",
							outputPath);
				System.out.println(message);
			}
		}
	}
	
	public String[][][] analyseLine (String ethiopicLine) {
		Objects.requireNonNull(ethiopicLine);
		
		ArrayList<String> tokens = this.tokeniser.tokenise(ethiopicLine);
		String[][][] outputArray = new String[tokens.size()][][];
		for (int i = 0; i < tokens.size(); i++) {
			outputArray[i] = this.analyseWord(tokens.get(i));
		}
		return outputArray;
	}
	
	public String[][] analyseWord (String ethiopicWord) {
		/*
		 * Returns a two-dimensional String array analysisArray[n][2] (with n analyses for this word).
		 * For i-th analysis,
		 *
		 * - analysisArray[i][0] is the romanised representation of the word (with morpheme boundaries);
		 * - analysisArray[i][1] is the gloss.
		 * 
		 * Example: አሰይድ -> { {">-a-sayd", "1.SG-CAUS-syd:IMPF"}, {">asayd", "(>syd).PL"} }.
		 *
		 * Note: If there are no analyses, the returned value is { {"[input_string]", "#"} }.
		 * 
		 */
		Objects.requireNonNull(ethiopicWord);
 
		WordEntry wordEntry = this.wordProcessor.processWord(ethiopicWord);
		int cutoff = this.getNumAnalysesInOutput(wordEntry);
		String[][] outputArray = new String[cutoff][];
		for (int i = 0; i < cutoff; i++) {
			WordAnalysis analysis = wordEntry.getAnalysis(i);
			outputArray[i] = analysis.exportWithMarkup();
			if (outputArray[i].length != 2) {
				throw new IllegalArgumentException("Illegal object returned by WordAnalysis.exportWithMarkup: String[2] expected");
			}
		}
		return outputArray;
	}

	private ArrayList<String> readInputLines (String inputFilePath) throws IOException {
		try (
				InputStreamReader inputStream = new InputStreamReader(new FileInputStream(inputFilePath), "UTF-8");
				BufferedReader textReader = new BufferedReader(inputStream)) {
			ArrayList<String> lines = new ArrayList<>();
			String curLine = "";
			while ((curLine = textReader.readLine()) != null) {
				lines.add(curLine);
			}
			return lines;
		}
	}

	private void writeFileHeader (PrintWriter writer) throws IOException {
		String modeMessage = "Mode: ";
		modeMessage += ( this.maxAnalyses == 0 ) ?
				"Show all analyses" :
				String.format("Show first %d analyses", this.maxAnalyses);
		modeMessage += String.format("%n%n");
		writer.print(modeMessage);
	}

	private ArrayList<WordEntry> processLine (String ethiopicLine) {
		ArrayList<String> ethiopicWords = this.tokeniser.tokenise(ethiopicLine);
		ArrayList<WordEntry> processedWordList = new ArrayList<>();
		for (String ethiopicWord : ethiopicWords) {
			WordEntry processedWord = wordProcessor.processWord(ethiopicWord);
			processedWordList.add(processedWord);
		}
		return processedWordList;
	}

	private void writeLine (PrintWriter writer, ArrayList<WordEntry> entryList) throws IOException {
		// writer -> write to file
		for (WordEntry entry : entryList) {
			String toPrint = "";
			toPrint += String.format("* * * * * * *"
						+ "%n%n"
						+ "Word: %s"
						+ "%n%n", entry.getEthiopicOrtho());
		
			int numAnalysesToPrint = this.getNumAnalysesInOutput(entry);
			for (int i = 0; i < numAnalysesToPrint; i++) {
				WordAnalysis analysis = entry.getAnalysis(i);
				String[] exportedAnalysis = analysis.exportWithMarkup();
				String surface = exportedAnalysis[0];
				String gloss = exportedAnalysis[1];
				toPrint += String.format("%s" + "%n" + "%s" + "%n%n",
								surface, gloss);
			}
			writer.print(toPrint);
		}
	}

	private int getNumAnalysesInOutput (WordEntry wordEntry) {
		int numToPrint;
		int maxNum = this.maxAnalyses;
		int listSize = wordEntry.getNumAnalyses();
		if (maxNum == 0) { numToPrint = listSize; }
		else { // == if (this.maxAnalyses > 0)
			if (listSize <= maxNum) { numToPrint = listSize; }
			else { numToPrint = maxNum; }
		}
		return numToPrint;
	}
	
	public static class TigreParserBuilder {
		private int maxAnalyses;
		private String configFilePath;
		
		private TigreParserBuilder () {}
		
		public TigreParserBuilder setMaxAnalyses (final int maxAnalyses) {
			if (maxAnalyses >= 0) {
				this.maxAnalyses = maxAnalyses;
			}
			return this;
		}
		
		public TigreParserBuilder setConfigFilePath (final String path) {
			Objects.requireNonNull(path);
			this.configFilePath = path;
			return this;
		}
		
		public TigreParser build () throws IOException, ParseException {
			TigreParser parser = new TigreParser();
			if (this.configFilePath != null) {
				parser.configBuilder = new ConfigBuilder(configFilePath);	
			} else {
				parser.configBuilder = new ConfigBuilder();
			}
			
			parser.setMaxAnalyses(maxAnalyses);
			parser.tokeniser = new Tokeniser();
			
			try {
				parser.wordProcessor = parser.configBuilder.constructWordProcessor();
			} catch (ConfigParseException e) {
				String message = "Failed to parse one of the configuration files (see stack trace for details)";
				throw new ParseException(message, 0);
			}
			
			return parser;
		}
	}

}
