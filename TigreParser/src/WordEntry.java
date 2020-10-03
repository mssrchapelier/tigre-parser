import java.util.ArrayList;

public class WordEntry {
	private String ethiopicOrtho;
	private ArrayList<WordAnalysis> analysisList;
	
	public WordEntry (String ethiopicOrtho, ArrayList<WordAnalysis> analysisList) {
		this.ethiopicOrtho = ethiopicOrtho;
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
}
