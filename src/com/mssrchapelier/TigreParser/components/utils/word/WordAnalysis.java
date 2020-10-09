package com.mssrchapelier.TigreParser.components.utils.word;

import java.util.ArrayList;
import java.util.ListIterator;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class WordAnalysis {

	// Format for surface, gloss: morpheme1-...-[unanalysedPart]-...-morphemeN

	final ArrayList<AnalysisSegment> segments;
	public final boolean isFinalAnalysis;

	public WordAnalysis (ArrayList<AnalysisSegment> segments) {
		try { validateSegmentList(segments); }
		catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Failed to create word analysis", e);
		}

		this.segments = segments;
		this.isFinalAnalysis = listContainsUnanalysed(segments);
	}

	public WordAnalysis (WordAnalysis analysis) {
		this.segments = AnalysisSegmentUtils.cloneList(analysis.segments);
		this.isFinalAnalysis = analysis.isFinalAnalysis;
	}

	public static WordAnalysis createWithEmptyAnalysis (String surface) {
		UnanalysedSegment segment = new UnanalysedSegment(surface);
		ArrayList<AnalysisSegment> segmentList = new ArrayList<>();
		segmentList.add(segment);
		return new WordAnalysis(segmentList);
	}

	public WordAnalysis insertReplacement (String replacement) {
		if (replacement.isEmpty()) {
			throw new IllegalArgumentException("Failed to insert replacement into word analysis: replacement string cannot be empty");
		}

		String[] segmentStrings = replacement.split("\\-");
		ArrayList<AnalysisSegment> segmentList = new ArrayList<>();
		for (int i = 0; i < segmentStrings.length; i++) {
			String segmentString = segmentStrings[i];
			segmentList.add(AnalysisSegmentUtils.parseAndBuild(segmentString));
		}
		WordAnalysis innerAnalysis = new WordAnalysis(segmentList);
		return innerAnalysis.insertInto(this);
	}

	public WordAnalysis insertInto (WordAnalysis enclosingAnalysis) {
		if (enclosingAnalysis.isFinalAnalysis) {
			throw new IllegalArgumentException("Failed to insert word analysis: the word analysis to insert into does not have unanalysed segments");
		}

		// analysis to insert
		WordAnalysis innerAnalysis = this;

		ArrayList<AnalysisSegment> newSegmentList = new ArrayList<>();
		for (AnalysisSegment outerSegment : enclosingAnalysis.segments) {
			if (outerSegment instanceof MorphemeAnalysis) {
				newSegmentList.add(AnalysisSegmentUtils.makeCopy(outerSegment));
			} else if (outerSegment instanceof UnanalysedSegment) {
				newSegmentList.addAll(AnalysisSegmentUtils.cloneList(innerAnalysis.segments));
			} else {
				throw new IllegalArgumentException("Failed to insert word analysis: analysis segment is an instance of an unknown class");
			}
		}
		return new WordAnalysis(newSegmentList);
	}

	private static void validateSegmentList (ArrayList<AnalysisSegment> segmentList) {
		if (segmentList.isEmpty()) {
			throw new IllegalArgumentException("Analysis segment list cannot be empty");
		}

		int unanalysedCount = 0;
		for (AnalysisSegment segment : segmentList) {
			if (segment instanceof UnanalysedSegment) { unanalysedCount++; }
			if (unanalysedCount > 1) {
				throw new IllegalArgumentException("Analysis segment list cannot contain more than one unanalysed segment");
			}
		}
	}

	private static boolean listContainsUnanalysed (ArrayList<AnalysisSegment> segmentList) {
		for (AnalysisSegment segment : segmentList) {
			if (segment instanceof UnanalysedSegment) { return false; }
		}
		return true;
	}

	public boolean isEmptyAnalysis () {
		if (this.segments.size() == 1
			&& !this.isFinalAnalysis) { return true; }
		else { return false; }
	}

	public String getUnanalysedPart () {
		return this.getUnanalysedSegment().surface;
	}

	// Replaces the unanalysed part of this WordAnalysis with the analysis specified in replacement (possibly non-final). Returns a WordAnalysis with the new analysis included.
	// Format for replacement: surface1:lex1-..:..-[unanalysedPart]-..:..-surfaceN:lexN

	private UnanalysedSegment getUnanalysedSegment () {
		for (AnalysisSegment segment : this.segments) {
			if (segment instanceof UnanalysedSegment) { return (UnanalysedSegment) segment; }
		}
		throw new IllegalStateException("No unanalysed segments");
	}
	
	public String getRawWord () {
		String rawWord = "";
		for (AnalysisSegment segment : this.segments) {
			rawWord += segment.surface;
		}
		return rawWord;
	}

	public String[] exportWithMarkup () {
		String wordSurface = "";
		String wordGloss = "";

		ListIterator<AnalysisSegment> segmentIterator = this.segments.listIterator();
		while (segmentIterator.hasNext()) {
			AnalysisSegment segment = segmentIterator.next();
			String[] exportedSegment = AnalysisSegmentUtils.exportWithMarkup(segment);
			wordSurface += exportedSegment[0];
			wordGloss += exportedSegment[1];
			if (segmentIterator.hasNext()) {
				wordSurface += "-";
				wordGloss += "-";
			}
		}

		String[] outputArray = new String[2];
		outputArray[0] = wordSurface;
		outputArray[1] = wordGloss;
		return outputArray;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(109, 113)
				.append(segments)
				.append(isFinalAnalysis)
				.toHashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof WordAnalysis)) { return false; }
		if (obj == this) { return true; }

		WordAnalysis rhs = (WordAnalysis) obj;
		return new EqualsBuilder().append(segments, rhs.segments)
					.append(isFinalAnalysis, rhs.isFinalAnalysis)
					.isEquals();
	}
}
