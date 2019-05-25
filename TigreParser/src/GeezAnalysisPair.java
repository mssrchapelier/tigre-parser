import java.util.ArrayList;

public class GeezAnalysisPair {
	String geezWord;
	String ungeminatedOrtho;
	ArrayList<String> geminatedOrthos;
	ArrayList<WordGlossPair> analysisList;
	
	public GeezAnalysisPair() {
		this.geezWord = "";
		this.ungeminatedOrtho = "";
		this.geminatedOrthos = new ArrayList<>();
		this.analysisList = new ArrayList<>();
	}
}
