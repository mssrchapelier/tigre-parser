package com.mssrchapelier.TigreParser.components.utils.word;

import java.util.ArrayList;

public final class AnalysisSegmentUtils {

	private AnalysisSegmentUtils () {}

	public static AnalysisSegment makeCopy (AnalysisSegment inputSegment) {
		if (inputSegment instanceof MorphemeAnalysis) {
			return new MorphemeAnalysis((MorphemeAnalysis) inputSegment);
		} else if (inputSegment instanceof UnanalysedSegment) {
			return new UnanalysedSegment((UnanalysedSegment) inputSegment);
		} else {
			throw new IllegalArgumentException("Failed to create copy of analysis segment: input segment is an instance of an unknown class");
		}
	}

	public static AnalysisSegment parseAndBuild (String inputString) {
		// Format of inputString:
		// - for MorphemeAnalysis: "surface:gloss"
		// - for UnanalysedSegment: "[surface]:#"
		String[] morphemeParts = inputString.split("\\:");
		if (morphemeParts.length != 2) {
			throw new IllegalArgumentException("Failed to parse analysis segment: replacement is not formatted properly (must contain exactly one surface/gloss separator)");
		}
		String surface = morphemeParts[0];
		String gloss = morphemeParts[1];

		if (surface.charAt(0) == '['
			&& surface.charAt(surface.length() - 1) == ']'
			&& gloss.charAt(0) == '#') {
			// remove the first and the last characters (square brackets)
			surface = surface.substring(1, surface.length() - 1);
			return new UnanalysedSegment(surface);
		} else {
			return new MorphemeAnalysis(surface, gloss);
		}
	}

	static String[] exportWithMarkup (AnalysisSegment segment) {
		String[] outputArray = new String[2];
		if (segment instanceof UnanalysedSegment) {
			UnanalysedSegment unanalysedSegment = (UnanalysedSegment) segment;
			outputArray[0] = String.format("[%s]", unanalysedSegment.surface);
			outputArray[1] = "#";
		} else if (segment instanceof MorphemeAnalysis) {
			MorphemeAnalysis morphemeAnalysis = (MorphemeAnalysis) segment;
			outputArray[0] = morphemeAnalysis.surface;
			outputArray[1] = morphemeAnalysis.gloss;
		} else {
			throw new IllegalArgumentException("Failed to export analysis segment to string array: segment is an instance of an unknown class");
		}
		return outputArray;
	}

	public static ArrayList<AnalysisSegment> cloneList (ArrayList<? extends AnalysisSegment> inputList) {
		ArrayList<AnalysisSegment> outputList = new ArrayList<>();
		for (AnalysisSegment segment : inputList) {
			outputList.add(makeCopy(segment));
		}
		return outputList;
	}

}
