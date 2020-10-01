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

public class Builder {
	Tokeniser tokeniser;
	WordProcessor wordProcessor;
	
	int maxAnalyses;

	static String romanisationMapFilePath = "configs/romanization-map.file";
	static String verbParadigmFilePath = "configs/verb-paradigm.txt";

	// NB: The order of file names in patternFilePaths is NOT arbitrary (the levels generated are processed in this order).
	private static String[] patternFilePaths = { "configs/patterns/pref-coord.json",
							"configs/patterns/pref-relz.json",
							"configs/patterns/pref-neg.json",
							"configs/patterns/pref-iobj.json",
							"configs/patterns/suf-expl.json",
							"configs/patterns/suf-pron.json",
							"configs/patterns/pau_2.json",
							"configs/patterns/pass-ptcp-deriv-pref.json",
							"configs/patterns/nominal-stem.json",
							"configs/patterns/lexicon.json" };
	
	public void setMaxAnalyses (int maxAnalyses) {
		if (maxAnalyses < 0) { throw new IllegalArgumentException("maxAnalyses must be a non-negative integer"); }
		this.maxAnalyses = maxAnalyses;
	}

	public Builder (int maxAnalyses) throws IOException, ConfigParseException {
		if (maxAnalyses < 0) { throw new IllegalArgumentException("maxAnalyses must be a non-negative integer"); }
		
		this.setMaxAnalyses(maxAnalyses);
		
		this.tokeniser = new Tokeniser();
		
		Transliterator transliterator = new Transliterator(romanisationMapFilePath);
		Geminator geminator = new Geminator();

		PatternProcessor patternProcessor = new PatternProcessor.PatternProcessorBuilder()
								.readFrom(patternFilePaths)
								.build();
		
		VerbParadigm verbParadigm = new VerbParadigm.VerbParadigmBuilder()
							.readFrom(verbParadigmFilePath)
							.build();
		Conjugator conjugator = new Conjugator(verbParadigm);
		VerbProcessor verbProcessor = new VerbProcessor(conjugator);


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

				ArrayList<GeezAnalysisPair> analysisLists = this.processLine(line);
				this.writeLine(writer, analysisLists);
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

	private ArrayList<GeezAnalysisPair> processLine (String ethiopicLine) {
		ArrayList<String> ethiopicWords = this.tokeniser.tokenise(ethiopicLine);
		ArrayList<GeezAnalysisPair> processedWordList = new ArrayList<>();
		for (String ethiopicWord : ethiopicWords) {
			GeezAnalysisPair processedWord = wordProcessor.processWord(ethiopicWord);
			processedWordList.add(processedWord);
		}
		return processedWordList;
	}

	private void writeLine (PrintWriter writer, ArrayList<GeezAnalysisPair> analysisLists) throws IOException {
		// writer -> write to file
		for (GeezAnalysisPair analysisList : analysisLists) {
			String toPrint = "";
			toPrint += String.format("* * * * * * *"
						+ "%n%n"
						+ "Word: %s"
						+ "%n%n", analysisList.getEthiopicOrtho());
		
			int numAnalysesToPrint = this.getNumAnalysesToPrint(analysisList);
			for (int i = 0; i < numAnalysesToPrint; i++) {
				WordGlossPair analysis = analysisList.getAnalysis(i);
				toPrint += String.format("%s" + "%n" + "%s" + "%n%n",
							analysis.surfaceForm, analysis.lexicalForm);
			}
			writer.print(toPrint);
		}
	}

	private int getNumAnalysesToPrint (GeezAnalysisPair gaPair) {
		int numToPrint;
		int maxNum = this.maxAnalyses;
		int listSize = gaPair.getNumAnalyses();
		if (maxNum == 0) { numToPrint = listSize; }
		else { // == if (this.maxAnalyses > 0)
			if (listSize <= maxNum) { numToPrint = listSize; }
			else { numToPrint = maxNum; }
		}
		return numToPrint;
	}

}
