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
		WordGlossPair wgPair = new WordGlossPair();
		wgPair.isFinalAnalysis = true;
		
		ListIterator<MorphemeDescriptionPair> it;
		MorphemeDescriptionPair curMorpheme;
		
		// appending prefixes
		
		it = cell.prefixes.listIterator();
		
		while (it.hasNext()) {
			curMorpheme = it.next();
			wgPair.surfaceForm += curMorpheme.surfaceForm;
			wgPair.lexicalForm += curMorpheme.lexicalForm;
			if (it.hasNext()) {
				wgPair.surfaceForm += "-";
				wgPair.lexicalForm += "-";
			}
		}
		
		if (!cell.prefixes.isEmpty()) {
			wgPair.surfaceForm += "-";
			wgPair.lexicalForm += "-";
		}
		
		// appending root
		
		for (int radIndex = 0; radIndex < stem.numRadicals; radIndex++) {
			// geminate if applicable
			char consToAdd = stem.rootAsLetters.get(radIndex);
			
			if (cell.geminationPattern[radIndex] > 0) {
				wgPair.surfaceForm += consToAdd;
			}
			
			if (cell.geminationPattern[radIndex] == 2 && LetterType.isSubjectToGemination(stem.rootAsLetters.get(radIndex))) {
				wgPair.surfaceForm += consToAdd;
			}
			// append vowel if applicable
			if (radIndex != stem.numRadicals - 1) {
				char charToAppend = cell.vowelPattern.surfaceForm.charAt(radIndex);
				if (charToAppend != '0') {
					wgPair.surfaceForm += charToAppend;
				}
			}
		}
		wgPair.lexicalForm += stem.rootAsString + ":";
		wgPair.lexicalForm += cell.vowelPattern.lexicalForm;
		
		// appending suffixes
		
		if (!cell.suffixes.isEmpty()) {
			wgPair.surfaceForm += "-";
			wgPair.lexicalForm += "-";
		}
		
		it = cell.suffixes.listIterator();
		
		while (it.hasNext()) {
			curMorpheme = it.next();
			wgPair.surfaceForm += curMorpheme.surfaceForm;
			wgPair.lexicalForm += curMorpheme.lexicalForm;
			if (it.hasNext()) {
				wgPair.surfaceForm += "-";
				wgPair.lexicalForm += "-";
			}
		}
		
		return wgPair;
	}
}
