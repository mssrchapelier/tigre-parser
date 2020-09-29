import java.util.ArrayList;

public class GeezAnalysisPair {
	String ethiopicOrtho;
	String ungeminatedOrtho;
	ArrayList<String> geminatedOrthos;
	ArrayList<WordGlossPair> analysisList;
	
	public GeezAnalysisPair() {
		this.ethiopicOrtho = "";
		this.ungeminatedOrtho = "";
		this.geminatedOrthos = new ArrayList<>();
		this.analysisList = new ArrayList<>();
	}
}
