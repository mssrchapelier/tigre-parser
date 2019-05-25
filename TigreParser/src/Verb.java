import java.util.ArrayList;

public class Verb {
	
	ArrayList<MorphemeDescriptionPair> prefixes;
	VerbStem stem;
	MorphemeDescriptionPair vowelTemplate;
	ArrayList<MorphemeDescriptionPair> suffixes;
	VerbGrammemeSet grammemeSet;
	
	public Verb(ArrayList<MorphemeDescriptionPair> prefixes, VerbStem stem, MorphemeDescriptionPair vowelTemplate,
			ArrayList<MorphemeDescriptionPair> suffixes, VerbGrammemeSet grammemeSet) throws IllegalArgumentException {
		this.prefixes = prefixes;
		this.stem = stem;
		
		// Check whether the vowel template argument corresponds to root in stem argument.
		// E. g., passing the "aa" template with a quadriradical root such as "trgm" would be invalid.
		if (stem.getRoot().length != vowelTemplate.surfaceForm.length() + 1) {
			throw new IllegalArgumentException("Vowel template does not correspond to this root.");
		}
		
		this.vowelTemplate = vowelTemplate;
		this.suffixes = suffixes;
		this.grammemeSet = grammemeSet;
	}
	
}
