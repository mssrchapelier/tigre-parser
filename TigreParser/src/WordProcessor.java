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

	public GeezAnalysisPair processWord (String ethiopicOrtho) {
		try {
			String romanisedOrtho = this.transliterator.romanise(ethiopicOrtho);
			ArrayList<String> geminatedOrthos = this.geminator.geminate(romanisedOrtho);
			ArrayList<WordGlossPair> analysisList = new ArrayList<>();
			for (String geminatedOrtho : geminatedOrthos) {
				analysisList.addAll(this.analyseGeminatedOrtho(geminatedOrtho));
			}
			analysisList = removeDuplicates(analysisList);
			Collections.sort(analysisList, Collections.reverseOrder(new WordGlossPairComparator()));
	
			return new GeezAnalysisPair(ethiopicOrtho, analysisList);
		} catch (NotEthiopicScriptException e) {
			return constructWithEmptyAnalysis(ethiopicOrtho);
		}
	}

	private static GeezAnalysisPair constructWithEmptyAnalysis (String inputWord) {
		WordGlossPair analysis = WordGlossPair.createWithEmptyAnalysis(inputWord);
		ArrayList<WordGlossPair> analysisList = new ArrayList<>();
		analysisList.add(analysis);
		return new GeezAnalysisPair(inputWord, analysisList);
	}

	private ArrayList<WordGlossPair> analyseGeminatedOrtho (String geminatedOrtho) {
		ArrayList<WordGlossPair> analysisList = this.patternProcessor.processWord(geminatedOrtho); 
		analysisList = this.parseVerbsInList(analysisList);
		analysisList = removeEmptyAnalyses(analysisList);
		return analysisList;
	}

	private ArrayList<WordGlossPair> parseVerbsInList (ArrayList<WordGlossPair> inputList) {
		ArrayList<WordGlossPair> outputList = new ArrayList<>();
		for (WordGlossPair inputAnalysis : inputList) {
			outputList.add(WordGlossPair.newInstance(inputAnalysis));
			if (!inputAnalysis.isFinalAnalysis) {
				String unanalysedPart = inputAnalysis.getUnanalysedPart();
				ArrayList<WordGlossPair> verbAnalysisList = this.verbProcessor.processWord(unanalysedPart);
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
