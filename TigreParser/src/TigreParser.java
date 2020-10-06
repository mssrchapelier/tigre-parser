import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TigreParser {
	private static final String DEFAULT_CONFIG_FILE_PATH = "config.json";
	private static final int DEFAULT_MAX_ANALYSES = 0;
	
	ConfigurationBuilder configurationBuilder;

	Tokeniser tokeniser;
	WordProcessor wordProcessor;
	
	int maxAnalyses;
	
	public void setMaxAnalyses (int maxAnalyses) {
		if (maxAnalyses < 0) { throw new IllegalArgumentException("maxAnalyses must be a non-negative integer"); }
		this.maxAnalyses = maxAnalyses;
	}

	public TigreParser () throws IOException, ConfigParseException {
		this(DEFAULT_MAX_ANALYSES, DEFAULT_CONFIG_FILE_PATH);
	}

	public TigreParser (int maxAnalyses) throws IOException, ConfigParseException {
		this(maxAnalyses, DEFAULT_CONFIG_FILE_PATH);
	}

	public TigreParser (String configFilePath) throws IOException, ConfigParseException {
		this(DEFAULT_MAX_ANALYSES, configFilePath);
	}

	public TigreParser (int maxAnalyses, String configFilePath) throws IOException, ConfigParseException {
		if (maxAnalyses < 0) { throw new IllegalArgumentException("maxAnalyses must be a non-negative integer"); }

		this.configurationBuilder = new ConfigurationBuilder().readConfig(configFilePath);
		
		this.setMaxAnalyses(maxAnalyses);
		
		this.tokeniser = new Tokeniser();
		
		Transliterator transliterator = new Transliterator(this.configurationBuilder.getTransliterationMapFilePath());
		Geminator geminator = new Geminator();

		PatternProcessor patternProcessor = new PatternProcessor.PatternProcessorBuilder()
								.readFrom(this.configurationBuilder.getPatternFilePaths())
								.build();
		
		VerbParadigm verbParadigm = new VerbParadigm.VerbParadigmBuilder()
							.readFrom(this.configurationBuilder.getVerbParadigmFilePath())
							.build();
		Conjugator conjugator = new Conjugator(verbParadigm);
		RootListGenerator rootListGenerator = new RootListGenerator();
		VerbProcessor verbProcessor = new VerbProcessor(conjugator, rootListGenerator);


		this.wordProcessor = new WordProcessor.WordProcessorBuilder()
							.setTransliterator(transliterator)
							.setGeminator(geminator)
							.setPatternProcessor(patternProcessor)
							.setVerbProcessor(verbProcessor)
							.build();
	}

	public void processFile (String inputPath, String outputPath) throws IOException {
		// process silently (nothing sent to System.out)
		this.processFile(inputPath, outputPath, false);
	}

	public void processFile (String inputPath, String outputPath, boolean printNotifications) throws IOException {
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
 
		return this.wordProcessor.processWord(ethiopicWord)
					.getAnalysesAsStringArrays();
	}

	private ArrayList<String> readInputLines (String inputFilePath) throws IOException {
		try (BufferedReader textReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFilePath), "UTF-8"))) {
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
		
			int numAnalysesToPrint = this.getNumAnalysesToPrint(entry);
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

	private int getNumAnalysesToPrint (WordEntry wordEntry) {
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

}
