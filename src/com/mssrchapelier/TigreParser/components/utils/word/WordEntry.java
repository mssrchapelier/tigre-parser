package com.mssrchapelier.TigreParser.components.utils.word;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;

public class WordEntry {
	private String ethiopicOrtho;
	private ArrayList<WordAnalysis> analysisList;
	
	public WordEntry (String ethiopicOrtho, ArrayList<WordAnalysis> analysisList) {
		this.ethiopicOrtho = ethiopicOrtho;
		analysisList = removeDuplicateAnalyses(analysisList);
		Collections.sort(analysisList, Collections.reverseOrder(new WordAnalysisComparator()));
		this.analysisList = analysisList;
	}

	public String getEthiopicOrtho () { return this.ethiopicOrtho; }
	
	public WordAnalysis getAnalysis (int i) { return this.analysisList.get(i); }

	public int getNumAnalyses () { return this.analysisList.size(); }

	public String[][] getAnalysesAsStringArrays () {
		int numAnalyses = this.analysisList.size();
		String[][] analysisArray = new String[numAnalyses][2];
		for (int i = 0; i < numAnalyses; i++) {
			WordAnalysis analysis = analysisList.get(i);
			analysisArray[i] = analysis.exportWithMarkup();
		}
		return analysisArray;
	}
	
	private static ArrayList<WordAnalysis> removeDuplicateAnalyses (ArrayList<WordAnalysis> inputList) {
		LinkedHashSet<WordAnalysis> outputSet = new LinkedHashSet<>(inputList);
		return new ArrayList<>(outputSet);
	}
}
