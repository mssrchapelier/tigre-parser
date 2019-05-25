import java.util.ArrayList;
import java.util.LinkedHashMap;

public class VerbParadigm {
	
	/*
	 * Structure of paradigm:
	 * paradigm -- number_of_radicals : typeParadigm
	 * typeParadigm -- verb_type : derivativesParadigm
	 * derivativesParadigm -- derivational_prefix : paradigm_cells
	 * 
	 */
	LinkedHashMap<
					Integer, LinkedHashMap< // number of radicals
											VerbType, LinkedHashMap< // verb type (A, B, C, D)
																	VerbPreformative, ArrayList<VerbParadigmCell> // derivational prefix (t-, a-, at- ...)
																	>
											>
					> paradigm;
	
	public VerbParadigm () {
		this.paradigm = new LinkedHashMap<>();
	}
	
	public ArrayList<WordGlossPair> buildAllForms (VerbStem stem) {
		
		ArrayList<WordGlossPair> wordList = new ArrayList<>();
		ArrayList<VerbParadigmCell> cellList = new ArrayList<>();
		try {
			cellList = this.paradigm.get(stem.getNumRadicals())
					.get(stem.getVerbType())
					.get(stem.getDerivationalPrefix());
		} catch (NullPointerException e) { }
		
		for (VerbParadigmCell cell : cellList) {
			wordList.add(new VerbFormBuilder(cell, stem).build());
		}
		
		return wordList;
	}
	
}
