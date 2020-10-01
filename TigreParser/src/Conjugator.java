import java.util.ListIterator;
import java.util.ArrayList;

public class Conjugator {

	VerbParadigm paradigm;
	
	public Conjugator (VerbParadigm paradigm) { this.paradigm = paradigm; }

	public ArrayList<WordGlossPair> conjugate (VerbStem stem) {
		ArrayList<VerbParadigmCell> singleVerbParadigm = this.paradigm.getSingleVerbParadigm(stem.numRadicals, stem.verbType, stem.derivationalPrefix);
		ArrayList<WordGlossPair> formList = new ArrayList<>();
		for (VerbParadigmCell cell : singleVerbParadigm) {
			formList.add(constructSingleForm(cell, stem));
		}
		return formList;
	}

	private static WordGlossPair constructSingleForm (VerbParadigmCell cell, VerbStem stem) {
		
		String surfaceForm = "";
		String lexicalForm = "";

		ListIterator<MorphemeDescriptionPair> it;
		MorphemeDescriptionPair curMorpheme;
		
		// appending prefixes
		
		it = cell.prefixes.listIterator();
		
		while (it.hasNext()) {
			curMorpheme = it.next();
			surfaceForm += curMorpheme.surfaceForm;
			lexicalForm += curMorpheme.lexicalForm;
			if (it.hasNext()) {
				surfaceForm += "-";
				lexicalForm += "-";
			}
		}
		
		if (!cell.prefixes.isEmpty()) {
			surfaceForm += "-";
			lexicalForm += "-";
		}
		
		// appending root
		
		for (int radIndex = 0; radIndex < stem.numRadicals; radIndex++) {
			// geminate if applicable
			char consToAdd = stem.rootAsLetters.get(radIndex);
			
			if (cell.geminationPattern[radIndex] > 0) {
				surfaceForm += consToAdd;
			}
			
			if (cell.geminationPattern[radIndex] == 2 && LetterType.isSubjectToGemination(stem.rootAsLetters.get(radIndex))) {
				surfaceForm += consToAdd;
			}
			// append vowel if applicable
			if (radIndex != stem.numRadicals - 1) {
				char charToAppend = cell.vowelPattern.surfaceForm.charAt(radIndex);
				if (charToAppend != '0') {
					surfaceForm += charToAppend;
				}
			}
		}
		lexicalForm += stem.rootAsString + ":";
		lexicalForm += cell.vowelPattern.lexicalForm;
		
		// appending suffixes
		
		if (!cell.suffixes.isEmpty()) {
			surfaceForm += "-";
			lexicalForm += "-";
		}
		
		it = cell.suffixes.listIterator();
		
		while (it.hasNext()) {
			curMorpheme = it.next();
			surfaceForm += curMorpheme.surfaceForm;
			lexicalForm += curMorpheme.lexicalForm;
			if (it.hasNext()) {
				surfaceForm += "-";
				lexicalForm += "-";
			}
		}
		
		return new WordGlossPair(surfaceForm, lexicalForm);
	}
}
