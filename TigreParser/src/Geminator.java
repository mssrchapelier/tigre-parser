import java.util.ArrayList;
import java.util.LinkedHashSet;
import org.apache.commons.math3.util.CombinatoricsUtils;
import java.util.Iterator;

public class Geminator {

	public Geminator () {}

	public ArrayList<String> geminate (String romanisedWord) {

		// The parameter romanisedWord is assumed to be the output of Transliterator.romanise(ethiopicWord), i. e. the romanised representation of an Ethiopic word which assumes conformity to pattern: (CV)+, where V includes schwa.
		// Returns possible geminated variants of romanisedWord, with schwa removed from all words.
		// NB: These geminated variants do not include the schwa sound which may be present in the phonetic representations of Tigre words (its presence is determined by phonotactics).

		ArrayList<String> geminatedVariants = new ArrayList<>();
		ArrayList<Integer> geminablePositions = generateGeminablePositions(romanisedWord);

		ArrayList<int[]> geminationPatterns = generateGeminationPatterns(geminablePositions);

		for (int[] geminationPattern : geminationPatterns) {
			String geminatedVariant = constructGeminatedOrtho(romanisedWord, geminationPattern);
			geminatedVariants.add(geminatedVariant);
		}
		
		geminatedVariants = removeDuplicates(geminatedVariants);
		return geminatedVariants;
	}

	private static ArrayList<Integer> generateGeminablePositions (String romanisedWord) {
		// Determine positions of geminable consonants in romanisedWord.

		ArrayList<Integer> geminablePositions = new ArrayList<>();

		// Index i starts with 2:
		// - the first letter is always a consonant; the first consonant can never be geminated
		// - the second letter is a vowel or a schwa in ungemWordWithSchwas
		for (int curPos = 2; curPos < romanisedWord.length(); curPos++) {
			char curChar = romanisedWord.charAt(curPos);
			if (LetterType.isSubjectToGemination(curChar)) {
				boolean isGeminablePosition = true;

				int nextVowelPos = curPos + 1;
				int nextConsPos = curPos + 2;

				// if this is not the last consonant
				if (nextConsPos < romanisedWord.length()) {
					char nextVowel = romanisedWord.charAt(nextVowelPos);
					// if the next vowel is schwa
					if (LetterType.isSchwa(nextVowel)) {
						char nextCons = romanisedWord.charAt(nextConsPos);
						// if the next consonant is not the same as the current one
						if (curChar == nextCons) {
							// the current position can't be geminated
							isGeminablePosition = false;
						}
					}
				}
				if (isGeminablePosition) { geminablePositions.add(curPos); }
			}
		}
		return geminablePositions;
	}

	private static ArrayList<int[]> generateGeminationPatterns (ArrayList<Integer> geminablePositions) {
		// e. g. {9, 18, 27, 36, 45} => {}, {9}, {18}, {27}, ..., {9, 18}, ..., {9, 18, 27}, ..., {9, 18, 27, 36, 45}
		ArrayList<int[]> geminationPatterns = new ArrayList<>();
		
		// add an empty pattern: no letters are geminated
		geminationPatterns.add(new int[0]);

		for (int k = 1; k <= geminablePositions.size(); k++) {
			// combinations of k out of n elements, where n = size of geminablePositions, k from 1 to n
			// e. g. geminablePositions == {9, 18, 27, 36, 45} => {0}, {1}, {2}, ..., {0, 1}, ..., {0, 1, 2}, ..., {0, 1, 2, 3, 4}
			Iterator<int[]> indexCombinationsIterator = CombinatoricsUtils.combinationsIterator(geminablePositions.size(), k);

			while (indexCombinationsIterator.hasNext()) {
				// e. g. {2, 4}
				int[] indexCombination = indexCombinationsIterator.next();
				
				// e. g. indexCombination == {2, 4} and geminablePositions == {9, 18, 27, 36, 45} => geminationPattern == {27, 45}
				int[] geminationPattern = new int[indexCombination.length];
				for (int i = 0; i < indexCombination.length; i++) {
					geminationPattern[i] = geminablePositions.get(indexCombination[i]);
				}
				geminationPatterns.add(geminationPattern);
			}
		}

		return geminationPatterns;
	}

	private static String constructGeminatedOrtho (String ungeminatedWord, int[] geminationPattern) {

		String geminatedOrtho = "";

		if (geminationPattern.length == 0) {
			geminatedOrtho = ungeminatedWord;
		} else {

			// Note: geminationPattern is iterated manually below. Wrapping geminationPattern into a List<Integer> and getting its Iterator is not done because of concerns about possible overhead that could be caused by boxing ints into Integers (this method is expected to be called dozens of times for each Ethiopic word).

			// Marks current position in geminationPattern, e. g. geminationPattern == {23, 41, 47}, curArrayIndex == 1 => curPositionToGeminate == 41.
			int curArrayIndex = 0;

			int curPositionToGeminate = geminationPattern[curArrayIndex];

			// for each letter in ungeminatedWord
			for (int curLetterIndex = 0; curLetterIndex < ungeminatedWord.length(); curLetterIndex++) {
				char curLetter = ungeminatedWord.charAt(curLetterIndex);

				// add this letter to geminatedOrtho
				geminatedOrtho += curLetter;

				// if this position is to be geminated
				if (curLetterIndex == curPositionToGeminate) {

					// add this letter once more to geminatedOrtho (i. e. geminate)
					geminatedOrtho += curLetter;
					
					// set next position to geminate if any left
					curArrayIndex++;
					if (curArrayIndex < geminationPattern.length) {
						curPositionToGeminate = geminationPattern[curArrayIndex];
					}
				}
			}
		}

		geminatedOrtho = removeSchwas(geminatedOrtho);
		return geminatedOrtho;
	}

	private static String removeSchwas (String inputWord) {
		String outputWord = "";
		for (int i = 0; i < inputWord.length(); i++) {
			char letter = inputWord.charAt(i);
			if (!LetterType.isSchwa(letter)) { outputWord += letter; }
		}
		return outputWord;
	}

	private static ArrayList<String> removeDuplicates (ArrayList<String> inputList) {
		LinkedHashSet<String> outputAsSet = new LinkedHashSet<>(inputList);
		return new ArrayList<String>(outputAsSet);
	}
}
