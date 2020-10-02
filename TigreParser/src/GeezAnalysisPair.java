import java.util.ArrayList;

public class GeezAnalysisPair {
	private String ethiopicOrtho;
	private ArrayList<WordGlossPair> analysisList;
	
	public GeezAnalysisPair (String ethiopicOrtho, ArrayList<WordGlossPair> analysisList) {
		this.ethiopicOrtho = ethiopicOrtho;
		this.analysisList = analysisList;
	}

	public String getEthiopicOrtho () { return this.ethiopicOrtho; }
	
	public WordGlossPair getAnalysis (int i) { return this.analysisList.get(i); }

	public int getNumAnalyses () { return this.analysisList.size(); }

	public String[][] getAnalysesAsStringArrays () {
		int numAnalyses = this.analysisList.size();
		String[][] analysisArray = new String[numAnalyses][2];
		for (int i = 0; i < numAnalyses; i++) {
			WordGlossPair analysis = analysisList.get(i);
			analysisArray[i][0] = analysis.surface;
			analysisArray[i][1] = analysis.gloss;
		}
		return analysisArray;
	}
}
