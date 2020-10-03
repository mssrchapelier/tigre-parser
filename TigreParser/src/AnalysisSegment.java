abstract class AnalysisSegment {
	final String surface;

	AnalysisSegment (String surface) { this.surface = surface; }

	AnalysisSegment (AnalysisSegment segment) { this.surface = segment.surface; }

	String getRawSurface () { return this.surface; }
}

