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
	
	private ArrayList<ArrayList<ReplaceRule>> patternCascade;

	private PatternProcessor (ArrayList<ArrayList<ReplaceRule>> patternCascade) {
		this.patternCascade = patternCascade;
	}

	public ArrayList<WordGlossPair> processWord (final String geminatedOrtho) {
		ArrayList<WordGlossPair> analysisList = new ArrayList<>();

		WordGlossPair emptyAnalysis = WordGlossPair.createWithEmptyAnalysis(geminatedOrtho);
		analysisList.add(emptyAnalysis);

		for (ArrayList<ReplaceRule> level : this.patternCascade) {
			analysisList = processLevel(analysisList, level);
		}

		return analysisList;
	}
	
	private static ArrayList<WordGlossPair> processLevel (ArrayList<WordGlossPair> inputAnalysisList,
			ArrayList<ReplaceRule> patternLevel) {
		LinkedHashSet<WordGlossPair> newLinesSet = new LinkedHashSet<>();
		for (WordGlossPair inputAnalysis : inputAnalysisList) {
			// add the same unprocessed part as a variant to newLinesSet
			newLinesSet.add(WordGlossPair.newInstance(inputAnalysis));
			// extract the part to be processed
			if (!inputAnalysis.isFinalAnalysis) {
				String wordToProcess = extractUnprocessedPart(inputAnalysis);

				// run all patterns from this level on the unprocessed part of the current analysis
				for (ReplaceRule replaceRule : patternLevel) {
					Pattern pattern = Pattern.compile(replaceRule.matchPattern);
					Matcher matcher = pattern.matcher(wordToProcess);
					if (matcher.find()) {
						String replacement = matcher.replaceAll(replaceRule.replacePattern);
						// add the replacement to newLinesSet
						newLinesSet.add(constructWgPair(inputAnalysis, replacement));
					}
				}
			}
		}
		
		return new ArrayList<WordGlossPair>(newLinesSet);
	}
	
	static String extractUnprocessedPart (WordGlossPair wgPair) {
		Matcher m = unprocessedExtractorPattern.matcher(wgPair.surfaceForm);
		if (m.find()) {	return m.group("unprocessed"); }
		else { return ""; }
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
				// unanalysed part -> the returned WGPair is not a final analysis
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
		private ArrayList<ArrayList<ReplaceRule>> patternCascade;

		public PatternProcessorBuilder () { this.patternCascade = new ArrayList<>(); }

		public PatternProcessorBuilder readFrom (String[] patternFilePaths) throws IOException {
			for (String path : patternFilePaths) {
				try (JsonReader reader = new JsonReader (new InputStreamReader(new FileInputStream(path), "UTF-8"))) {
					ArrayList<ReplaceRule> curPatterns = new ArrayList<>();
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
							curPatterns.add(new ReplaceRule(regex, replacement));
						}
						reader.endObject();
					}
					reader.endArray();
					this.patternCascade.add(curPatterns);
				} catch (IOException e) {
					throw new IOException(String.format("Failed to read pattern file %s", path));
				}
			}
			return this;
		}
		
		public PatternProcessor build () { return new PatternProcessor(this.patternCascade); }
	}
}
