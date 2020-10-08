package com.mssrchapelier.TigreParser;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.stream.JsonReader;
import com.mssrchapelier.TigreParser.components.utils.misc.ConfigParseException;
import com.mssrchapelier.TigreParser.components.utils.word.WordAnalysis;

class PatternProcessor {
	
	private ArrayList<ArrayList<ReplaceRule>> patternCascade;

	private PatternProcessor (ArrayList<ArrayList<ReplaceRule>> patternCascade) {
		this.patternCascade = patternCascade;
	}

	ArrayList<WordAnalysis> processWord (final String geminatedOrtho) {
		ArrayList<WordAnalysis> analysisList = new ArrayList<>();

		WordAnalysis emptyAnalysis = WordAnalysis.createWithEmptyAnalysis(geminatedOrtho);
		analysisList.add(emptyAnalysis);

		for (ArrayList<ReplaceRule> level : this.patternCascade) {
			analysisList = processLevel(analysisList, level);
		}

		return analysisList;
	}
	
	private static ArrayList<WordAnalysis> processLevel (ArrayList<WordAnalysis> inputAnalysisList,
			ArrayList<ReplaceRule> patternLevel) {
		LinkedHashSet<WordAnalysis> newAnalysisSet = new LinkedHashSet<>();
		for (WordAnalysis inputAnalysis : inputAnalysisList) {
			// add the same unprocessed part as a variant to newAnalysisSet
			newAnalysisSet.add(new WordAnalysis(inputAnalysis));
			// extract the part to be processed
			if (!inputAnalysis.isFinalAnalysis) {
				String unprocessedPart = inputAnalysis.getUnanalysedPart();

				// run all patterns from this level on the unprocessed part of the current analysis
				for (ReplaceRule replaceRule : patternLevel) {
					Pattern pattern = Pattern.compile(replaceRule.regex);
					Matcher matcher = pattern.matcher(unprocessedPart);
					if (matcher.find()) {
						// replace and add to newAnalysisSet
						String replacement = matcher.replaceFirst(replaceRule.replacement);
						WordAnalysis newAnalysis = inputAnalysis.insertReplacement(replacement);
						newAnalysisSet.add(newAnalysis);
					}
				}
			}
		}
		return new ArrayList<WordAnalysis>(newAnalysisSet);
	}
	
	static class PatternProcessorBuilder {
		private ArrayList<ArrayList<ReplaceRule>> patternCascade;

		PatternProcessorBuilder () { this.patternCascade = new ArrayList<>(); }

		PatternProcessorBuilder readFrom (String[] patternFilePaths) throws IOException, ConfigParseException {
			try {
				for (String path : patternFilePaths) {
					try (
						LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(path));
						JsonReader reader = new JsonReader(lineNumberReader)
					) {
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
								try {
									level.add(new ReplaceRule(regex, replacement));
								} catch (ConfigParseException cause) {
									String description = "Failed to parse replacement rule";
									throw new ConfigParseException.ConfigParseExceptionBuilder().appendDescription(description)
																		.appendFilePath(path)
																		.appendLineNumber(lineNumberReader.getLineNumber())
																		.appendCause(cause)
																		.build();
					}
							}
							reader.endObject();
						}
						reader.endArray();
						// add level to this.patternCascade
						this.patternCascade.add(level);
					} 
				}
				return this;
			} catch (ConfigParseException e) {
				// reset this.patternCascade
				this.patternCascade = new ArrayList<>();
				throw e;
			}
		}
		
		PatternProcessor build () {
			return new PatternProcessor(this.patternCascade);
		}
	}
}
