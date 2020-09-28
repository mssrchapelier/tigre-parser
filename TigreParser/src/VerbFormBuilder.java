import java.util.ListIterator;

public class VerbFormBuilder {
	VerbParadigmCell cell;
	VerbStem stem;
	
	public VerbFormBuilder (VerbParadigmCell cell, VerbStem stem) throws IllegalArgumentException {
		if (stem.rootAsLetters.size() != cell.acceptedNumRadicals ||
				stem.verbType != cell.acceptedVerbType ||
				stem.derivationalPrefix != cell.acceptedDerivPrefix) {
			throw new IllegalArgumentException ("Stem does not correspond to paradigm cell.");
		}
		
		this.cell = cell;
		this.stem = stem;
	}
	
	public WordGlossPair build () {
		WordGlossPair word = new WordGlossPair();
		word.isFinalAnalysis = true;
		
		ListIterator<MorphemeDescriptionPair> it;
		MorphemeDescriptionPair curMorpheme;
		
		// appending prefixes
		
		it = cell.prefixes.listIterator();
		
		while (it.hasNext()) {
			curMorpheme = it.next();
			word.surfaceForm += curMorpheme.surfaceForm;
			word.lexicalForm += curMorpheme.lexicalForm;
			if (it.hasNext()) {
				word.surfaceForm += "-";
				word.lexicalForm += "-";
			}
		}
		
		if (!cell.prefixes.isEmpty()) {
			word.surfaceForm += "-";
			word.lexicalForm += "-";
		}
		
		// appending root
		
		for (int radIndex = 0; radIndex < stem.numRadicals; radIndex++) {
			// geminate if applicable
			char consToAdd = stem.rootAsLetters.get(radIndex);
			
			if (cell.geminationPattern[radIndex] > 0) {
				word.surfaceForm += consToAdd;
			}
			
			if (cell.geminationPattern[radIndex] == 2 && LetterType.isSubjectToGemination(stem.rootAsLetters.get(radIndex))) {
				word.surfaceForm += consToAdd;
			}
			// append vowel if applicable
			if (radIndex != stem.numRadicals - 1) {
				char charToAppend = cell.vowelPattern.surfaceForm.charAt(radIndex);
				if (charToAppend != '0') {
					word.surfaceForm += charToAppend;
				}
			}
		}
		word.lexicalForm += stem.rootAsString + ":";
		word.lexicalForm += cell.vowelPattern.lexicalForm;
		
		// appending suffixes
		
		if (!cell.suffixes.isEmpty()) {
			word.surfaceForm += "-";
			word.lexicalForm += "-";
		}
		
		it = cell.suffixes.listIterator();
		
		while (it.hasNext()) {
			curMorpheme = it.next();
			word.surfaceForm += curMorpheme.surfaceForm;
			word.lexicalForm += curMorpheme.lexicalForm;
			if (it.hasNext()) {
				word.surfaceForm += "-";
				word.lexicalForm += "-";
			}
		}
		
		return word;
	}
}
