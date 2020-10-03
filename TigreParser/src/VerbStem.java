import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class VerbStem {
	
	final VerbStemDescription stemDescription;
	final char[] rootConsonants;
	final String rootDictionaryGloss;
	
	VerbStem (VerbStem stem) {
		this.stemDescription = new VerbStemDescription(stem.stemDescription);
		this.rootConsonants = Arrays.copyOf(stem.rootConsonants, stem.rootConsonants.length);
		this.rootDictionaryGloss = stem.rootDictionaryGloss;
	}
	
	private VerbStem (Root root, VerbPreformative derivationalPrefix) {
		this.rootDictionaryGloss = root.dictionaryGloss;
		this.rootConsonants = Arrays.copyOf(root.consonants, root.consonants.length);
		this.stemDescription = new VerbStemDescription(NumRadicals.parseNumRadicals(this.rootConsonants.length),
								root.verbType,
								derivationalPrefix);
	}

	VerbStem createWithNoPrefix (Root root) {
		return new VerbStem(root, VerbPreformative.NO_PREFORMATIVE);
	}

	static ArrayList<VerbStem> generateWithPossiblePrefixes (Root root) {
		ArrayList<VerbStem> stemList = new ArrayList<>();
		for (VerbPreformative prefix : VerbPreformative.values()) {
			if (root.isCompatibleWithPrefix(prefix)) { stemList.add(new VerbStem(root, prefix)); }
		}
		return stemList;
	}
}
