import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexIterator {
	// everything inside square brackets
	private static String unprocessedPartRegex = ".*\\[(?<unprocessed>.*)\\].*";
	private static Pattern unprocessedExtractorPattern = Pattern.compile(unprocessedPartRegex);
	
	private Pattern processorPattern;
	private Matcher processorMatcher;
	
	public RegexIterator() {
		this.processorPattern = Pattern.compile("");
		this.processorMatcher = this.processorPattern.matcher("");
	}
	
	public ArrayList<WordGlossPair> processLevel (ArrayList<WordGlossPair> inputWGPairs,
			ArrayList<PatternReplacePair> patterns) {
		LinkedHashSet<WordGlossPair> newLinesSet = new LinkedHashSet<>();
		for (WordGlossPair inputWGPair : inputWGPairs) {
			// add the same unprocessed part as a variant to newLinesSet
			newLinesSet.add(WordGlossPair.newInstance(inputWGPair));
			// extract the part to be processed
			if (!inputWGPair.isFinalAnalysis) {
				String wordToProcess = extractUnprocessedPart(inputWGPair);
				// run all patterns from this level on the unprocessed part of the current analysis
				for (PatternReplacePair pattern : patterns) {
					this.processorPattern = Pattern.compile(pattern.matchPattern);
					this.processorMatcher = this.processorPattern.matcher(wordToProcess);
					if (this.processorMatcher.find()) {
						String replacement = this.processorMatcher.replaceAll(pattern.replacePattern);
						// add the replacement to newLinesSet
						newLinesSet.add(constructWgPair(inputWGPair, replacement));
					}
				}
			}
		}
		
		return new ArrayList<WordGlossPair>(newLinesSet);
	}
	
	static String extractUnprocessedPart (WordGlossPair wgPair) {
		Matcher m = unprocessedExtractorPattern.matcher(wgPair.surfaceForm);
		if (m.find()) {
			return m.group("unprocessed");
		}
		return "";
	}
	
	// Replaces the unanalysed part of oldWgPair with the analysis specified in replacement (possibly non-final). Returns a WordGlossPair with the new analysis included.
	static WordGlossPair constructWgPair (WordGlossPair oldWgPair, String replacement) throws IllegalArgumentException {
		WordGlossPair newWgPair = new WordGlossPair();
		String[] morphemes = replacement.split("\\-");
		String analysisSurface = "";
		String analysisLex = "";
		boolean isFinalAnalysis = true;
		for (int i = 0; i < morphemes.length; i++) {
			String morpheme = morphemes[i];
			String[] morphemeParts = morpheme.split("\\:");
			if (morphemeParts.length != 2) { throw new IllegalArgumentException("replacement is not formatted properly"); }
			if (morphemeParts[0].charAt(0) == '[' && morphemeParts[1].charAt(0) == '#') {
				// unanalyzed part -> the returned WGPair is not a final analysis
				isFinalAnalysis = false;
			}
			analysisSurface += morphemeParts[0];
			analysisLex += morphemeParts[1];
			
			if (i < morphemes.length - 1) {
				analysisSurface += "-";
				analysisLex += "-";
			}
		}
		
		String newSurfaceForm = oldWgPair.surfaceForm;
		newSurfaceForm = newSurfaceForm.replaceAll("\\[.*\\]", analysisSurface);
		newWgPair.surfaceForm = newSurfaceForm;
		
		String newLexForm = oldWgPair.lexicalForm;
		newLexForm = newLexForm.replaceAll("#", analysisLex);
		newWgPair.lexicalForm = newLexForm;
		
		newWgPair.isFinalAnalysis = isFinalAnalysis;
		
		return newWgPair;
	}
	
}
