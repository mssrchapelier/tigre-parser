package com.mssrchapelier.TigreParser.components.utils.word;

public class MorphemeAnalysis extends AnalysisSegment {
	
	final String gloss; // e. g. "POSS.2.F.SG"
	
	public MorphemeAnalysis (String surface, String gloss) {
		super(surface);
		this.gloss = gloss;
	}

	public MorphemeAnalysis (MorphemeAnalysis analysis) {
		super(analysis);
		this.gloss = analysis.gloss;
	} 
	
}
