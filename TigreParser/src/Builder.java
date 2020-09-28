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
	Conjugator conjugator;
	RegexIterator regexIterator;
	Transliterator transliterator;
	
	private static String unprocessedPartRegex = ".*\\[(?<unprocessed>.*)\\].*";
	private static Pattern unprocessedExtractorPattern = Pattern.compile(unprocessedPartRegex);
	
	// NB: The order of file names in patternFilePaths is NOT arbitrary (the levels generated are processed in this order).
	static String romanizationMapFilePath = "configs/romanization-map.file";
	static String verbParadigmFilePath = "configs/verb-paradigm.txt";

	private static String[] patternFilePaths = { "configs/patterns/pref-coord.json", "configs/patterns/pref-relz.json", "configs/patterns/pref-neg.json", "configs/patterns/pref-iobj.json", "configs/patterns/suf-expl.json", "configs/patterns/suf-pron.json", "configs/patterns/pau_2.json", "configs/patterns/pass-ptcp-deriv-pref.json", "configs/patterns/nominal-stem.json", "configs/patterns/lexicon.json" };
	
	public Builder () {
		try {
			this.regexIterator = RegexIterator.createWithPatterns(patternFilePaths);
			VerbParadigm verbParadigm = new VerbParadigm.VerbParadigmBuilder()
				.readFrom(verbParadigmFilePath)
				.build();
			this.conjugator = new Conjugator(verbParadigm);

			// ə in map file stands for disambiguation of cases like [kə][ka] from [kka] (geminated).
			// The actual [ə] sound may or may not occur in that position; this is determined by phonotactics.
			// The ə symbol MUST be removed from any fields of GeezAnalysisPair objects immediately after generating geminated variants.
			this.transliterator = new Transliterator(romanizationMapFilePath);
		} catch (IOException | ParseException | NullPointerException e) { e.printStackTrace(); }
	}
	
	public void processFile (String inputPath, String outputPath, int numAnalysesToShow) throws IllegalArgumentException, IOException {
		
		if (numAnalysesToShow < 0) { throw new IllegalArgumentException("Illegal argument: number of analyses to show must be a non-negative integer; 0 for showing all analyses"); }
		
		BufferedReader textReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputPath), "UTF-8"));
		PrintWriter writer = new PrintWriter(outputPath, "UTF-8");
		
		if (numAnalysesToShow == 0) {
			writer.print("Mode: Show all analyses\n\n");
		} else {
			writer.printf("Mode: Show first %d analyses%n%n", numAnalysesToShow);
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
			for (GeezAnalysisPair word : wordList) {
				
				writer.printf("* * * * * * *%n%nWord: %s%nAnalyses:%n%n", word.geezWord);
				for (String gemOrtho : word.geminatedOrthos) {
					word.analysisList.addAll(this.analyseGeminatedOrtho(gemOrtho));
				}
				Collections.sort(word.analysisList, Collections.reverseOrder(new WordGlossPairComparator()));
				
				
				int curNumAnalysesToPrint;
				// numAnalysesToShow >= 0: checked in the beginning of this method
				if (numAnalysesToShow == 0) {
					curNumAnalysesToPrint = word.analysisList.size(); 
				} else if (numAnalysesToShow < word.analysisList.size()) {
					curNumAnalysesToPrint = numAnalysesToShow;
				} else {
					curNumAnalysesToPrint = word.analysisList.size();
				}
				
				for (int i = 0; i < curNumAnalysesToPrint; i++) {
					WordGlossPair analysis = word.analysisList.get(i);
					writer.printf("%s%n%s%n%n", analysis.surfaceForm, analysis.lexicalForm);
				}
			}
		}
		
		System.out.print("\n\nProcessing completed!\n\n");
		
		textReader.close();
		writer.close();
	}
	
	private ArrayList<WordGlossPair> analyseGeminatedOrtho (String geminatedOrtho) {
		ArrayList<WordGlossPair> analysisList;
		WordGlossPair unanalysedWord = WordGlossPair.createWithEmptyAnalysis(geminatedOrtho);
		analysisList = this.regexIterator.analyseWord(unanalysedWord); 

		ArrayList<WordGlossPair> analyzedVerbs = new ArrayList<>();
		for (WordGlossPair analysis : analysisList) {
			if (!analysis.isFinalAnalysis) {
				ArrayList<WordGlossPair> verbAnalysisList = this.analyzeAsVerb(analysis);
				for (WordGlossPair verbAnalysis : verbAnalysisList) {
					analyzedVerbs.add(WordGlossPair.newInstance(verbAnalysis));
				}
			}
		}
		analysisList.addAll(analyzedVerbs);
		LinkedHashSet<WordGlossPair> analysisSet = new LinkedHashSet<>(analysisList);
		analysisList = new ArrayList<>(analysisSet);
		return analysisList;
	}
	
	private ArrayList<WordGlossPair> analyzeAsVerb (WordGlossPair wgPair) {
		ArrayList<WordGlossPair> analysisList = new ArrayList<>();
		// add the same unprocessed part as a variant to newLinesSet
		analysisList.add(WordGlossPair.newInstance(wgPair));
		// extract the part to be processed
		String lineToProcess = extractUnprocessedPart(wgPair);
		// run all patterns from this level on the unprocessed part of the current analysis
		ArrayList<Root> roots = RootListGenerator.getRoots(lineToProcess);

		for (Root root : roots) {
			LinkedHashSet<WordGlossPair> formSet = new LinkedHashSet<>();
			try {
				ArrayList<VerbStem> derivedStems = VerbStem.generateWithPossiblePrefixes(root);
 				for (VerbStem stem : derivedStems) {
					try {
						formSet.addAll(this.conjugator.conjugate(stem));
					} catch (NullPointerException e) { e.printStackTrace(); }
				}
				ArrayList<WordGlossPair> formList = new ArrayList<>(formSet);
				for (WordGlossPair form : formList) {
					if (form.getRawWord().equals(lineToProcess)) {
						analysisList.add(constructVerbWgPair(wgPair, form));
					}
				}
			} catch (IllegalArgumentException e) {  }
		}
		
		return analysisList;
	}
	
	private static String extractUnprocessedPart (WordGlossPair wgPair) {
		Matcher m = unprocessedExtractorPattern.matcher(wgPair.surfaceForm);
		if (m.find()) {
			return m.group("unprocessed")
				.replaceAll("[\\[\\]]", "");
		} else { return ""; }
	}
	
	private static WordGlossPair constructVerbWgPair (WordGlossPair oldWgPair, WordGlossPair stemAnalysis) {
		WordGlossPair newWgPair = new WordGlossPair();
		
		newWgPair.surfaceForm = oldWgPair.surfaceForm.replaceAll("\\[.*\\]", stemAnalysis.surfaceForm);
		newWgPair.lexicalForm = oldWgPair.lexicalForm.replaceAll("#", stemAnalysis.lexicalForm);
		newWgPair.isFinalAnalysis = true;
		
		return newWgPair;
	}
	
}
