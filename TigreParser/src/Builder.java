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

import com.google.gson.stream.JsonReader;

public class Builder {
	VerbParadigm verbParadigm;
	ArrayList<ArrayList<PatternReplacePair>> patternCascade;
	RegexIterator regexIterator;
	Transliterator transliterator;
	
	private static String unprocessedPartRegex = ".*\\[(?<unprocessed>.*)\\].*";
	private static Pattern unprocessedExtractorPattern = Pattern.compile(unprocessedPartRegex);
	
	// NB: The order of file names in patternFilePaths is NOT arbitrary (the levels generated are processed in this order).
	static String[] patternFilePaths = { "configs/patterns/pref-coord.json", "configs/patterns/pref-relz.json", "configs/patterns/pref-neg.json", "configs/patterns/pref-iobj.json", "configs/patterns/suf-expl.json", "configs/patterns/suf-pron.json", "configs/patterns/pau_2.json", "configs/patterns/pass-ptcp-deriv-pref.json", "configs/patterns/nominal-stem.json", "configs/patterns/lexicon.json" };
	static String romanizationMapFilePath = "configs/romanization-map.file";
	static String verbParadigmFilePath = "configs/verb-paradigm.txt";

	public Builder () {
		try {
			this.verbParadigm = new VerbParadigmBuilder().build(verbParadigmFilePath);
			this.patternCascade = readPatterns();
			this.regexIterator = new RegexIterator();
			
			// ə in map file stands for disambiguation of cases like [kə][ka] from [kka] (geminated).
			// The actual [ə] sound may or may not occur in that position; this is determined by phonotactics.
			// The ə symbol MUST be removed from any fields of GeezAnalysisPair objects immediately after generating geminated variants.
			this.transliterator = new Transliterator(romanizationMapFilePath);
		} catch (IOException | ParseException e) { e.printStackTrace(); }
	}
	
	private static ArrayList<ArrayList<PatternReplacePair>> readPatterns () throws IOException {
		ArrayList<ArrayList<PatternReplacePair>> patternList = new ArrayList<>();
		
		for (String path : patternFilePaths) {
			JsonReader reader = new JsonReader (new InputStreamReader(new FileInputStream(path), "UTF-8"));
			ArrayList<PatternReplacePair> curPatterns = new ArrayList<>();
			reader.beginArray();
			while (reader.hasNext()) {
				reader.beginObject();
				while (reader.hasNext()) {
					// skip comment
					reader.nextName();
					reader.nextString();
					// read regex
					reader.nextName();
					String regex = reader.nextString();
					// read replacement
					reader.nextName();
					String replacement = reader.nextString();
					// create new PRPair
					curPatterns.add(new PatternReplacePair(regex, replacement));
				}
				reader.endObject();
			}
			reader.endArray();
			reader.close();
			patternList.add(curPatterns);
		}
		
		return patternList;
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
		WordGlossPairComparator wgpComparator = new WordGlossPairComparator();
		
		for (String line : lines) {
			counter++;
			
			System.out.printf("Processing line: %d out of %d%n", counter, lines.size());
			
			ArrayList<GeezAnalysisPair> wordList = transliterator.buildWordListFromLine(line);
			for (GeezAnalysisPair word : wordList) {
				
				writer.printf("* * * * * * *%n%nWord: %s%nAnalyses:%n%n", word.geezWord);
				for (String gemOrtho : word.geminatedOrthos) {
					word.analysisList.addAll(this.analyzeLine(gemOrtho));
				}
				Collections.sort(word.analysisList, Collections.reverseOrder(wgpComparator));
				
				
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
	
	private ArrayList<WordGlossPair> analyzeLine (String line) {
		ArrayList<WordGlossPair> analysisList = new ArrayList<>();
		analysisList.add(new WordGlossPair("[" + line + "]", "#", false));
		for (ArrayList<PatternReplacePair> curPatterns : patternCascade) {
			ArrayList<WordGlossPair> newAnalysisList = regexIterator.processLevel(analysisList, curPatterns);
			analysisList.clear();
			for (WordGlossPair analysis : newAnalysisList) {
				analysisList.add(WordGlossPair.newInstance(analysis));
			}
		}
		ArrayList<WordGlossPair> analyzedVerbs = new ArrayList<>();
		for (WordGlossPair analysis : analysisList) {
			if (!analysis.isFinalAnalysis) {
				ArrayList<WordGlossPair> newAnalysisList = this.analyzeAsVerb(analysis);
				for (WordGlossPair verbAnalysis : newAnalysisList) {
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
		lineToProcess = lineToProcess.replaceAll("[\\[\\]]", "");
		// run all patterns from this level on the unprocessed part of the current analysis
		RootListGenerator rootListGenerator = new RootListGenerator(lineToProcess);
		try {
			rootListGenerator.buildRootList();
		} catch (IllegalArgumentException e) {}
		for (Root root : rootListGenerator.roots) {
			LinkedHashSet<WordGlossPair> formSet = new LinkedHashSet<>();
			try {
				VerbStem baseStem = new VerbStem(root);
				ArrayList<VerbStem> derivedStems = new ArrayList<>();
				for (VerbPreformative prefix : VerbPreformative.values()) {
					switch (prefix) {
						case NO_PREFORMATIVE:
						case A:
						case T:
						case AT:
						case ATTA:
							if (baseStem.setDerivationalPrefix(prefix)) {
								VerbStem stemToAdd = VerbStem.newInstance(baseStem);
								derivedStems.add(stemToAdd);
							}
							break;
						default: break; 
					}
				}
				for (VerbStem stem : derivedStems) {
					formSet.addAll(this.verbParadigm.buildAllForms(stem));
				}
				ArrayList<WordGlossPair> formList = new ArrayList<>(formSet);
				for (WordGlossPair form : formList) {
					if (form.getRawWord().equals(lineToProcess)) {
						analysisList.add(constructVerbWgPair(wgPair, form));
					}
				}
			} catch (IllegalArgumentException e) {}
		}
		
		return analysisList;
	}
	
	private static String extractUnprocessedPart (WordGlossPair wgPair) {
		Matcher m = unprocessedExtractorPattern.matcher(wgPair.surfaceForm);
		if (m.find()) {
			return m.group("unprocessed");
		}
		return "";
	}
	
	private static WordGlossPair constructVerbWgPair (WordGlossPair oldWgPair, WordGlossPair stemAnalysis) {
		WordGlossPair newWgPair = new WordGlossPair();
		boolean isFinalAnalysis = true;
		
		/*
		String newSurfaceForm = oldWgPair.surfaceForm;
		newSurfaceForm = newSurfaceForm.replaceAll("\\[.*\\]", stemAnalysis.surfaceForm);
		newWgPair.surfaceForm = newSurfaceForm;
		*/

		newWgPair.surfaceForm = oldWgPair.surfaceForm.replaceAll("\\[.*\\]", stemAnalysis.surfaceForm);

		/*
		String newLexForm = oldWgPair.lexicalForm;
		newLexForm = newLexForm.replaceAll("#", stemAnalysis.lexicalForm);
		newWgPair.lexicalForm = newLexForm;
		*/
		newWgPair.lexicalForm = oldWgPair.lexicalForm.replaceAll("#", stemAnalysis.lexicalForm);

		newWgPair.isFinalAnalysis = isFinalAnalysis;
		
		return newWgPair;
	}
	
}
