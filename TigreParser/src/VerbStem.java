import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class VerbStem {
	
	final VerbStemDescription stemDescription;
	final ArrayList<Character> rootAsLetters;
	
	// --- FOR TESTING ---
	// implement getRootAsString method, remove rootAsString field
	final String rootAsString;
	
	VerbStem (VerbStem stem) {
		this.stemDescription = stem.stemDescription;
		this.rootAsLetters = new ArrayList<>(stem.rootAsLetters);
		this.rootAsString = stem.rootAsString;
	}
	
	private VerbStem (Root root, VerbPreformative derivationalPrefix) {
		
		this.rootAsString = root.toString();
		
		ArrayList<Character> rootAsLetters = new ArrayList<>();
		for (int i = 0; i < root.size(); i++) {
			rootAsLetters.add(root.consTemplate.get(i).consonant);
		}
		this.rootAsLetters = rootAsLetters;
		
		this.stemDescription = new VerbStemDescription(NumRadicals.parseNumRadicals(Integer.toString(this.rootAsLetters.size())),
								root.determineVerbType(),
								derivationalPrefix);
	}

	VerbStem createWithNoPrefix (Root root) {
		return new VerbStem(root, VerbPreformative.NO_PREFORMATIVE);
	}

	static ArrayList<VerbStem> generateWithPossiblePrefixes (Root root) {
		ArrayList<VerbStem> stemList = new ArrayList<>();
		for (VerbPreformative prefix : VerbPreformative.values()) {
			if (root.combinesWithPrefix(prefix)) { stemList.add(new VerbStem(root, prefix)); }
		}
		return stemList;
	}
}
