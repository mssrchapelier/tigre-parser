import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import com.google.gson.stream.JsonReader;

public class PatternProcessor {
	final private static String unprocessedPartRegex = ".*\\[(?<unprocessed>.*)\\].*";
	final private static Pattern unprocessedExtractorPattern = Pattern.compile(unprocessedPartRegex);
	
	private ArrayList<ArrayList<PatternReplacePair>> patternCascade;

	private PatternProcessor (ArrayList<ArrayList<PatternReplacePair>> patternCascade) {
		this.patternCascade = patternCascade;
	}

	public ArrayList<WordGlossPair> processWord (final String transliteratedVariant) {
		ArrayList<WordGlossPair> analysisList = new ArrayList<>();

		WordGlossPair inputWgPair = WordGlossPair.createWithEmptyAnalysis(transliteratedVariant);
		analysisList.add(inputWgPair);

		for (ArrayList<PatternReplacePair> level : this.patternCascade) {
			analysisList = processLevel(analysisList, level);
		}
		return analysisList;
	}
	
	private static ArrayList<WordGlossPair> processLevel (ArrayList<WordGlossPair> inputWGPairs,
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
	
	public static class PatternProcessorBuilder {
		private ArrayList<ArrayList<PatternReplacePair>> patternCascade;

		public PatternProcessorBuilder () { this.patternCascade = new ArrayList<>(); }

		public PatternProcessorBuilder readFrom (String[] patternFilePaths) throws IOException {
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
				} catch (IOException e) {
					throw new IOException("Failed to read pattern file %s");
				}
			}
			return this;
		}
		
		public PatternProcessor build () { return new PatternProcessor(this.patternCascade); }
	}
}
