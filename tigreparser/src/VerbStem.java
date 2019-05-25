
public class VerbStem {
	
	private Letter[] rootAsLetters;
	private VerbPreformative derivationalPrefix;
	private VerbType verbType;
	private int numRadicals;
	
	// Warning: Not updated if rootAsLetters or verbType are changed.
	private String rootAsString;
	
	public VerbStem () {
		this.rootAsLetters = new Letter[0];
		this.derivationalPrefix = VerbPreformative.NO_PREFORMATIVE;
		this.verbType = VerbType.UNKNOWN;
		this.derivationalPrefix = VerbPreformative.UNKNOWN;
		this.numRadicals = 0;
		this.rootAsString = "";
	}
	
	public VerbStem (Root root) throws IllegalArgumentException {
		if (root.size() < 3 || root.size() > 5) {
			throw new IllegalArgumentException("Root length must be between 3 and 5 consonants.");
		}
		
		int longACount = 0;
		for (ConsDescription cd : root.consTemplate) {
			if (cd.followedByLongA) { longACount++; }
			if (longACount > 1) { throw new IllegalArgumentException("Root cannot contain more than two consonants followed by [a:] (long A)."); }
		}
		
		this.rootAsString = root.toString();
		
		this.rootAsLetters = new Letter[root.size()];
		for (int i = 0; i < root.size(); i++) {
			this.rootAsLetters[i] = root.consTemplate.get(i).consonant;
		}
		
		this.verbType = determineVerbType(root);
		this.derivationalPrefix = VerbPreformative.NO_PREFORMATIVE;
		this.numRadicals = this.rootAsLetters.length;
	}
	
	public static VerbStem newInstance (VerbStem stem) {
		VerbStem newStem = new VerbStem();
		Letter[] newRootAsLetters = new Letter[stem.rootAsLetters.length];
		for (int i = 0; i < stem.rootAsLetters.length; i++) {
			newRootAsLetters[i] = Letter.newInstance(stem.rootAsLetters[i]);
		}
		newStem.rootAsLetters = newRootAsLetters;
		newStem.rootAsString = stem.rootAsString;
		newStem.numRadicals = stem.numRadicals;
		newStem.derivationalPrefix = stem.derivationalPrefix;
		newStem.verbType = stem.verbType;
		return newStem;
	}
	
	public Letter[] getRoot() {
		return this.rootAsLetters;
	}
	
	public String getRootAsString () {
		return this.rootAsString;
	}

	public VerbPreformative getDerivationalPrefix() {
		return this.derivationalPrefix;
	}

	public VerbType getVerbType() {
		return this.verbType;
	}
	
	public int getNumRadicals () {
		return this.numRadicals;
	}
	
	public Letter getRootConsonant (int i) throws IllegalArgumentException {
		if (i < 0 || i > this.rootAsLetters.length - 1) {
			throw new IllegalArgumentException("Argument must be between 0 and this root's length.");
		} else {
			return this.rootAsLetters[i];
		}
	}
	
	public boolean setDerivationalPrefix (VerbPreformative prefix) {
		switch (prefix) {
			case T: this.derivationalPrefix = VerbPreformative.T; return true;
			case A: if (this.verbType == VerbType.D || this.rootAsLetters[0].isLaryngeal()) {
						return false;
					} else {
						this.derivationalPrefix = prefix;
						return true;
					}
			case ATTA: if (this.rootAsLetters[0].isLaryngeal()) {
						return false;
					} else {
						this.derivationalPrefix = prefix;
						return true;
					}
			case AT: if (this.verbType == VerbType.A) {
						// Check if any of the consonants is a laryngeal. If yes, assigning type A is possible; otherwise it isn't.
						for (Letter consonant : this.rootAsLetters) {
							if (consonant.isLaryngeal()) { this.derivationalPrefix = prefix; return true; }
						}
						return false;
					} else {
						this.derivationalPrefix = prefix;
						return true;
					}
			default:
				this.derivationalPrefix = prefix;
				return true;
		}
	}
	
	public static VerbType determineVerbType (Root root) {
		if (root.size() == 3 && root.consTemplate.get(1).isGeminated) {
			return VerbType.B;
		} else if (root.size() == 4
				&& root.consTemplate.get(1).followedByLongA
				&& root.consTemplate.get(1).consonant.equals(root.consTemplate.get(2).consonant)) {
			return VerbType.D;
		} else if ((root.size() == 3 && root.consTemplate.get(0).followedByLongA)
				|| (root.size() == 4 && root.consTemplate.get(1).followedByLongA)
				|| (root.size() == 5 && root.consTemplate.get(2).followedByLongA)) {
			return VerbType.C;
		} else {
			return VerbType.A;
		}
	}
}
