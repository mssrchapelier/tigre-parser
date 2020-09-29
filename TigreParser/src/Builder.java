import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Builder {
	PatternProcessor patternProcessor;
	VerbProcessor verbProcessor;
	Transliterator transliterator;
	
	final int maxAnalysesToShow;

	// NB: The order of file names in patternFilePaths is NOT arbitrary (the levels generated are processed in this order).
	static String romanizationMapFilePath = "configs/romanization-map.file";
	static String verbParadigmFilePath = "configs/verb-paradigm.txt";

	private static String[] patternFilePaths = { "configs/patterns/pref-coord.json", "configs/patterns/pref-relz.json", "configs/patterns/pref-neg.json", "configs/patterns/pref-iobj.json", "configs/patterns/suf-expl.json", "configs/patterns/suf-pron.json", "configs/patterns/pau_2.json", "configs/patterns/pass-ptcp-deriv-pref.json", "configs/patterns/nominal-stem.json", "configs/patterns/lexicon.json" };
	
	public Builder (int maxAnalysesToShow) throws IOException, ParseException {
		if (maxAnalysesToShow < 0) { throw new IllegalArgumentException("maxAnalysesToShow must be a non-negative integer"); }
		
		this.maxAnalysesToShow = maxAnalysesToShow;
		this.patternProcessor = new PatternProcessor.PatternProcessorBuilder()
			.readFrom(patternFilePaths)
			.build();
		VerbParadigm verbParadigm = new VerbParadigm.VerbParadigmBuilder()
			.readFrom(verbParadigmFilePath)
			.build();
		Conjugator conjugator = new Conjugator(verbParadigm);
		this.verbProcessor = new VerbProcessor(conjugator);

		// ə in map file stands for disambiguation of cases like [kə][ka] from [kka] (geminated).
		// The actual [ə] sound may or may not occur in that position; this is determined by phonotactics.
		// The ə symbol MUST be removed from any fields of GeezAnalysisPair objects immediately after generating geminated variants.
		this.transliterator = new Transliterator(romanizationMapFilePath);
	}
	
	public void processFile (String inputPath, String outputPath) throws IOException {
		
		BufferedReader textReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputPath), "UTF-8"));
		PrintWriter writer = new PrintWriter(outputPath, "UTF-8");
		
		if (this.maxAnalysesToShow == 0) {
			writer.print("Mode: Show all analyses\n\n");
		} else {
			writer.printf("Mode: Show first %d analyses%n%n", this.maxAnalysesToShow);
		}
		
		ArrayList<String> lines = new ArrayList<>();
		
		String curLine = "";
		while ((curLine = textReader.readLine()) != null) {
			lines.add(curLine);
		}
		
		System.out.printf("Lines in total: %d%n", lines.size());
		int counter = 0;
		
		for (String line : lines) {
			counter++;
			
			System.out.printf("Processing line: %d out of %d%n", counter, lines.size());
			
			ArrayList<GeezAnalysisPair> wordList = transliterator.buildWordListFromLine(line);
			for (GeezAnalysisPair geezWord : wordList) {
				
				writer.printf("* * * * * * *%n%nWord: %s%nAnalyses:%n%n", geezWord.ethiopicOrtho);
				for (String gemOrtho : geezWord.geminatedOrthos) {
					geezWord.analysisList.addAll(this.analyseWord(gemOrtho));
				}
				Collections.sort(geezWord.analysisList, Collections.reverseOrder(new WordGlossPairComparator()));
				
				int curNumAnalysesToPrint;
				// this.maxAnalysesToShow >= 0: checked in the beginning of this method
				if (this.maxAnalysesToShow == 0) {
					curNumAnalysesToPrint = geezWord.analysisList.size(); 
				} else if (this.maxAnalysesToShow < geezWord.analysisList.size()) {
					curNumAnalysesToPrint = this.maxAnalysesToShow;
				} else {
					curNumAnalysesToPrint = geezWord.analysisList.size();
				}
				
				for (int i = 0; i < curNumAnalysesToPrint; i++) {
					WordGlossPair analysis = geezWord.analysisList.get(i);
					writer.printf("%s%n%s%n%n", analysis.surfaceForm, analysis.lexicalForm);
				}
			}
		}
		
		System.out.print("\n\nProcessing completed!\n\n");
		
		textReader.close();
		writer.close();
	}
	
	private ArrayList<WordGlossPair> analyseWord (String transliteratedVariant) {
		ArrayList<WordGlossPair> analysisList = this.patternProcessor.processWord(transliteratedVariant); 
		analysisList = this.parseVerbsInList(analysisList);
		analysisList = removeEmptyAnalyses(analysisList);
		analysisList = removeDuplicates(analysisList);
		return analysisList;
	}

	private ArrayList<WordGlossPair> parseVerbsInList (ArrayList<WordGlossPair> inputList) {
		ArrayList<WordGlossPair> outputList = new ArrayList<>();
		for (WordGlossPair inputAnalysis : inputList) {
			outputList.add(WordGlossPair.newInstance(inputAnalysis));
			if (!inputAnalysis.isFinalAnalysis) {
				ArrayList<WordGlossPair> verbAnalysisList = this.verbProcessor.processWord(inputAnalysis.getUnanalysedPart());
				for (WordGlossPair verbAnalysis : verbAnalysisList) {
					outputList.add(verbAnalysis.insertInto(inputAnalysis));
				}
			}
		}
		return outputList;
	}

	private static ArrayList<WordGlossPair> removeEmptyAnalyses (ArrayList<WordGlossPair> inputList) {
		ArrayList<WordGlossPair> outputList = new ArrayList<>();
		for (WordGlossPair pair : inputList) {
			if (!pair.isEmptyAnalysis()) { outputList.add(WordGlossPair.newInstance(pair)); }
		}
		return outputList;
	}
	
	private static ArrayList<WordGlossPair> removeDuplicates (ArrayList<WordGlossPair> inputList) {
		LinkedHashSet<WordGlossPair> outputSet = new LinkedHashSet<>(inputList);
		return new ArrayList<>(outputSet);
	}
}
