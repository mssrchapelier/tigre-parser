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

	public ArrayList<WordAnalysis> processWord (final String geminatedOrtho) throws ConfigParseException {
		ArrayList<WordAnalysis> analysisList = new ArrayList<>();

		WordAnalysis emptyAnalysis = WordAnalysis.createWithEmptyAnalysis(geminatedOrtho);
		analysisList.add(emptyAnalysis);

		for (ArrayList<ReplaceRule> level : this.patternCascade) {
			analysisList = processLevel(analysisList, level);
		}

		return analysisList;
	}
	
	private static ArrayList<WordAnalysis> processLevel (ArrayList<WordAnalysis> inputAnalysisList,
			ArrayList<ReplaceRule> patternLevel) throws ConfigParseException {
		LinkedHashSet<WordAnalysis> newAnalysisSet = new LinkedHashSet<>();
		for (WordAnalysis inputAnalysis : inputAnalysisList) {
			// add the same unprocessed part as a variant to newAnalysisSet
			newAnalysisSet.add(new WordAnalysis(inputAnalysis));
			// extract the part to be processed
			if (!inputAnalysis.isFinalAnalysis) {
				String unprocessedPart = inputAnalysis.getUnanalysedPart();

				// run all patterns from this level on the unprocessed part of the current analysis
				for (ReplaceRule replaceRule : patternLevel) {
					Pattern pattern = Pattern.compile(replaceRule.matchPattern);
					Matcher matcher = pattern.matcher(unprocessedPart);
					if (matcher.find()) {
						// replace and add to newAnalysisSet
						String replacement = matcher.replaceFirst(replaceRule.replacePattern);
						WordAnalysis newAnalysis = inputAnalysis.insertReplacement(replacement);
						newAnalysisSet.add(newAnalysis);
					}
				}
			}
		}
		return new ArrayList<WordAnalysis>(newAnalysisSet);
	}
	
	public static class PatternProcessorBuilder {
		private ArrayList<ArrayList<ReplaceRule>> patternCascade;

		public PatternProcessorBuilder () { this.patternCascade = new ArrayList<>(); }

		public PatternProcessorBuilder readFrom (String[] patternFilePaths) throws IOException {
			for (String path : patternFilePaths) {
				try (JsonReader reader = new JsonReader (new InputStreamReader(new FileInputStream(path), "UTF-8"))) {
					ArrayList<ReplaceRule> level = new ArrayList<>();
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
							// create new ReplaceRule
							level.add(new ReplaceRule(regex, replacement));
						}
						reader.endObject();
					}
					reader.endArray();
					// add level to this.patternCascade
					this.patternCascade.add(level);
				} catch (IOException e) {
					throw new IOException(String.format("Failed to read pattern file %s", path));
				}
			}
			return this;
		}
		
		public PatternProcessor build () {
			return new PatternProcessor(this.patternCascade);
		}
	}
}
