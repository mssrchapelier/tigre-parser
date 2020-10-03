import java.util.ListIterator;
import java.util.ArrayList;

public class Conjugator {

	VerbParadigm paradigm;
	
	public Conjugator (VerbParadigm paradigm) { this.paradigm = paradigm; }

	public ArrayList<WordAnalysis> conjugate (VerbStem stem) {
		ArrayList<VerbParadigmCell> singleVerbParadigm = this.paradigm.getSingleVerbParadigm(stem.stemDescription);
		ArrayList<WordAnalysis> formList = new ArrayList<>();
		for (VerbParadigmCell cell : singleVerbParadigm) {
			formList.add(cell.applyTo(stem));
		}
		return formList;
	}
}
