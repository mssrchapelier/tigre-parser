package com.mssrchapelier.TigreParser.components.utils.word;

public abstract class AnalysisSegment {
	final String surface;

	public AnalysisSegment (String surface) { this.surface = surface; }

	public AnalysisSegment (AnalysisSegment segment) { this.surface = segment.surface; }

	public String getRawSurface () { return this.surface; }
}

