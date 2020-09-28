import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import com.google.gson.stream.JsonReader;

public class RegexIterator {
	// everything inside square brackets
	private static String unprocessedPartRegex = ".*\\[(?<unprocessed>.*)\\].*";
	private static Pattern unprocessedExtractorPattern = Pattern.compile(unprocessedPartRegex);
	
	private ArrayList<ArrayList<PatternReplacePair>> patternCascade;
	
	private RegexIterator() {}

	public static RegexIterator createWithPatterns (String[] patternFilePaths) throws IOException {
		RegexIterator regexIterator = new RegexIterator();
		regexIterator.readPatterns(patternFilePaths);
		return regexIterator;
	}

	// initialising patternCascade: reading patterns from files
	public void readPatterns (String[] patternFilePaths) throws IOException {
		this.patternCascade = new ArrayList<>();
		for (String path : patternFilePaths) {
			try (JsonReader reader = new JsonReader (new InputStreamReader(new FileInputStream(path), "UTF-8"))) {
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
				this.patternCascade.add(curPatterns);
			} catch (IOException e) { throw e; }
		}
	}
	
	public ArrayList<WordGlossPair> analyseWord (final WordGlossPair inputWgPair) throws IllegalArgumentException {
		if (inputWgPair.isFinalAnalysis) { throw new IllegalArgumentException("Argument must be a non-final analysis"); }
		ArrayList<WordGlossPair> levelInputList = new ArrayList<>();
		levelInputList.add(WordGlossPair.newInstance(inputWgPair));
		for (ArrayList<PatternReplacePair> curPatterns : this.patternCascade) {
			ArrayList<WordGlossPair> levelOutputList = this.processLevel(levelInputList, curPatterns);
			levelInputList.clear();
			for (WordGlossPair analysis : levelOutputList) {
				levelInputList.add(WordGlossPair.newInstance(analysis));
			}
		}
		return levelInputList;
	}
	
	private ArrayList<WordGlossPair> processLevel (ArrayList<WordGlossPair> inputWGPairs,
			ArrayList<PatternReplacePair> patternLevel) {
		LinkedHashSet<WordGlossPair> newLinesSet = new LinkedHashSet<>();
		for (WordGlossPair inputWGPair : inputWGPairs) {
			// add the same unprocessed part as a variant to newLinesSet
			newLinesSet.add(WordGlossPair.newInstance(inputWGPair));
			// extract the part to be processed
			if (!inputWGPair.isFinalAnalysis) {
				String wordToProcess = extractUnprocessedPart(inputWGPair);
				// run all patterns from this level on the unprocessed part of the current analysis
				for (PatternReplacePair prPair : patternLevel) {
					Pattern pattern = Pattern.compile(prPair.matchPattern);
					Matcher matcher = pattern.matcher(wordToProcess);
					if (matcher.find()) {
						String replacement = matcher.replaceAll(prPair.replacePattern);
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
		if (m.find()) {	return m.group("unprocessed"); }
		return "";
	}
	
	// Replaces the unanalysed part of oldWgPair with the analysis specified in replacement (possibly non-final). Returns a WordGlossPair with the new analysis included.
	static WordGlossPair constructWgPair (WordGlossPair oldWgPair, String replacement) throws IllegalArgumentException {
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
		
		WordGlossPair newWgPair = new WordGlossPair();
		newWgPair.surfaceForm = oldWgPair.surfaceForm.replaceAll("\\[.*\\]", analysisSurface);
		newWgPair.lexicalForm = oldWgPair.lexicalForm.replaceAll("#", analysisLex);
		newWgPair.isFinalAnalysis = isFinalAnalysis;
		return newWgPair;
	}
	
}
