import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;

public class WordProcessor {
	private Transliterator transliterator;
	private Geminator geminator;
	private PatternProcessor patternProcessor;
	private VerbProcessor verbProcessor;

	private WordProcessor () {}

	public static class WordProcessorBuilder {
		private Transliterator transliterator;
		private Geminator geminator;
		private PatternProcessor patternProcessor;
		private VerbProcessor verbProcessor;

		public WordProcessorBuilder () {}

		public WordProcessorBuilder setTransliterator (Transliterator transliterator) {
			this.transliterator = transliterator;
			return this;
		}

		public WordProcessorBuilder setGeminator (Geminator geminator) {
			this.geminator = geminator;
			return this;
		}

		public WordProcessorBuilder setPatternProcessor (PatternProcessor patternProcessor) {
			this.patternProcessor = patternProcessor;
			return this;
		}

		public WordProcessorBuilder setVerbProcessor (VerbProcessor verbProcessor) {
			this.verbProcessor = verbProcessor;
			return this;
		}

		public WordProcessor build () {
			if (this.transliterator == null
				|| this.geminator == null
				|| this.patternProcessor == null
				|| this.verbProcessor == null) {
				throw new IllegalStateException("WordProcessorBuilder: some of the fields haven't been initialised");
			}

			WordProcessor wordProcessor = new WordProcessor();

			wordProcessor.setTransliterator(this.transliterator);
			wordProcessor.setGeminator(this.geminator);
			wordProcessor.setPatternProcessor(this.patternProcessor);
			wordProcessor.setVerbProcessor(this.verbProcessor);

			return wordProcessor;
		}
	}

	public void setTransliterator (Transliterator transliterator) {	this.transliterator = transliterator; }
	public void setGeminator (Geminator geminator) { this.geminator = geminator; }
	public void setPatternProcessor (PatternProcessor patternProcessor) { this.patternProcessor = patternProcessor; }
	public void setVerbProcessor (VerbProcessor verbProcessor) { this.verbProcessor = verbProcessor; }

	public WordEntry processWord (String inputWord) {
		ArrayList<WordAnalysis> analysisList = new ArrayList<>();
		try {
			String romanisedOrtho = this.transliterator.romanise(inputWord);
			ArrayList<String> geminatedOrthos = this.geminator.geminate(romanisedOrtho);
			for (String geminatedOrtho : geminatedOrthos) {
				analysisList.addAll(this.analyseGeminatedOrtho(geminatedOrtho));
			}
			analysisList = removeDuplicates(analysisList);
			Collections.sort(analysisList, Collections.reverseOrder(new WordAnalysisComparator()));
		} catch (NotEthiopicScriptException e) {
			// do nothing; analysisList remains empty
		}
		return new WordEntry(inputWord, analysisList);
	}

	private ArrayList<WordAnalysis> analyseGeminatedOrtho (String geminatedOrtho) {
		ArrayList<WordAnalysis> analysisList = this.patternProcessor.processWord(geminatedOrtho); 
		analysisList = this.parseVerbsInList(analysisList);
		analysisList = removeEmptyAnalyses(analysisList);
		return analysisList;
	}

	private ArrayList<WordAnalysis> parseVerbsInList (ArrayList<WordAnalysis> inputList) {
		ArrayList<WordAnalysis> outputList = new ArrayList<>();
		for (WordAnalysis inputAnalysis : inputList) {
			outputList.add(new WordAnalysis(inputAnalysis));
			if (!inputAnalysis.isFinalAnalysis) {
				String unanalysedPart = inputAnalysis.getUnanalysedPart();
				ArrayList<WordAnalysis> verbAnalysisList = this.verbProcessor.processWord(unanalysedPart);
				for (WordAnalysis verbAnalysis : verbAnalysisList) {
					outputList.add(verbAnalysis.insertInto(inputAnalysis));
				}
			}
		}
		return outputList;
	}

	private static ArrayList<WordAnalysis> removeEmptyAnalyses (ArrayList<WordAnalysis> inputList) {
		ArrayList<WordAnalysis> outputList = new ArrayList<>();
		for (WordAnalysis analysis : inputList) {
			if (!analysis.isEmptyAnalysis()) { outputList.add(new WordAnalysis(analysis)); }
		}
		return outputList;
	}
	
	private static ArrayList<WordAnalysis> removeDuplicates (ArrayList<WordAnalysis> inputList) {
		LinkedHashSet<WordAnalysis> outputSet = new LinkedHashSet<>(inputList);
		return new ArrayList<>(outputSet);
	}
}
