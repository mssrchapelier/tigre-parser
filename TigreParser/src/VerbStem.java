import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class VerbStem {
	
	final public ArrayList<Character> rootAsLetters;
	final public VerbPreformative derivationalPrefix;
	final public VerbType verbType;
	final public int numRadicals;
	
	final public String rootAsString;
	
	public VerbStem (VerbStem originalStem) {
		this.rootAsLetters = new ArrayList<>(originalStem.rootAsLetters);
		this.rootAsString = originalStem.rootAsString;
		this.numRadicals = originalStem.numRadicals;
		this.derivationalPrefix = originalStem.derivationalPrefix;
		this.verbType = originalStem.verbType;
	}
	
	private VerbStem (Root root, VerbPreformative derivationalPrefix) throws IllegalArgumentException {
		
		this.rootAsString = root.toString();
		
		ArrayList<Character> rootAsLetters = new ArrayList<>();
		for (int i = 0; i < root.size(); i++) {
			rootAsLetters.add(root.consTemplate.get(i).consonant);
		}
		this.rootAsLetters = rootAsLetters;
		
		this.verbType = root.determineVerbType();
		this.derivationalPrefix = derivationalPrefix;
		this.numRadicals = this.rootAsLetters.size();
	}

	public VerbStem createWithNoPrefix (Root root) throws IllegalArgumentException {
		return new VerbStem(root, VerbPreformative.NO_PREFORMATIVE);
	}

	public static ArrayList<VerbStem> generateWithPossiblePrefixes (Root root) throws IllegalArgumentException {
		ArrayList<VerbStem> stemList = new ArrayList<>();
		for (VerbPreformative prefix : VerbPreformative.values()) {
			if (root.combinesWithPrefix(prefix)) { stemList.add(new VerbStem(root, prefix)); }
		}
		return stemList;
	}
}
