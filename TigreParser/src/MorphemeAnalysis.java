class MorphemeAnalysis extends AnalysisSegment {
	
	final String gloss; // e. g. "POSS.2.F.SG"
	
	MorphemeAnalysis (String surface, String gloss) {
		super(surface);
		this.gloss = gloss;
	}

	MorphemeAnalysis (MorphemeAnalysis analysis) {
		super(analysis);
		this.gloss = analysis.gloss;
	} 
	
}
