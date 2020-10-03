// Processes finite verb forms (passed as Strings).

import java.util.ArrayList;

public class VerbProcessor {
	private Conjugator conjugator;
	private RootListGenerator rootListGenerator;
	
	public VerbProcessor (Conjugator conjugator, RootListGenerator rootListGenerator) {
		this.conjugator = conjugator;
		this.rootListGenerator = rootListGenerator;
	}

	public ArrayList<WordAnalysis> processWord (String word) {
		ArrayList<WordAnalysis> analysisList = new ArrayList<>();
		
		ArrayList<Root> roots = this.rootListGenerator.getRoots(word);

		for (Root root : roots) {
			ArrayList<VerbStem> derivedStems = VerbStem.generateWithPossiblePrefixes(root);
 			for (VerbStem stem : derivedStems) {
				ArrayList<WordAnalysis> allFormsWithRoot = this.conjugator.conjugate(stem);
				for (WordAnalysis form : allFormsWithRoot) {
					if (form.getRawWord().equals(word)) {
						analysisList.add(form);
					}
				}
			}
		}
		
		return analysisList;
	}
}
