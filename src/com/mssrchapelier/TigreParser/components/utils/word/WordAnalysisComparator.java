package com.mssrchapelier.TigreParser.components.utils.word;

import java.util.Comparator;

public class WordAnalysisComparator implements Comparator<WordAnalysis> {

	@Override
	public int compare (WordAnalysis analysis_1, WordAnalysis analysis_2) {
		// final_analysis > non-final_analysis
		if (analysis_1.isFinalAnalysis
			&& !analysis_2.isFinalAnalysis) {
			return 1;
		}
		else if (!analysis_1.isFinalAnalysis
				&& analysis_2.isFinalAnalysis) {
			return -1;
		}
		else {
			// both final or both non-final
			int segmentCount_1 = analysis_1.segments.size();
			int segmentCount_2 = analysis_2.segments.size();
			
			if (analysis_1.isFinalAnalysis && analysis_2.isFinalAnalysis) {
				// both final: fewer_morphemes > more_morphemes
				if (segmentCount_1 < segmentCount_2) { return 1; }
				else if (segmentCount_1 == segmentCount_2) { return 0; }
				else { return -1; }
			} else {
				// both non-final: fewer_morphemes < more_morphemes
				if (segmentCount_1 < segmentCount_2) { return -1; }
				else if (segmentCount_1 == segmentCount_2) { return 0; }
				else { return 1; }
			}
		}
	}
}
