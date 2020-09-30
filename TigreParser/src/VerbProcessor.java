// Processes finite verb forms (passed as Strings).

import java.util.ArrayList;

public class VerbProcessor {
	private Conjugator conjugator;
	
	public VerbProcessor (Conjugator conjugator) { this.conjugator = conjugator; }

	public ArrayList<WordGlossPair> processWord (String word) {
		ArrayList<WordGlossPair> analysisList = new ArrayList<>();
		
		ArrayList<Root> roots = RootListGenerator.getRoots(word);

		for (Root root : roots) {
			try {
				ArrayList<VerbStem> derivedStems = VerbStem.generateWithPossiblePrefixes(root);
 				for (VerbStem stem : derivedStems) {
					try {
						ArrayList<WordGlossPair> allFormsWithRoot = this.conjugator.conjugate(stem);
						for (WordGlossPair form : allFormsWithRoot) {
							if (form.getRawWord().equals(word)) {
								analysisList.add(form);
							}
						}
					} catch (NullPointerException e) {
						System.out.println(e.getMessage());
						e.printStackTrace();
					}
				}
			} catch (IllegalArgumentException e) { e.printStackTrace(); }
		}
		
		return analysisList;
	}
}