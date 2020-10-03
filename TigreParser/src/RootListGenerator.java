import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.apache.commons.math3.util.CombinatoricsUtils;

class RootListGenerator {
	
	RootListGenerator () {}

	ArrayList<Root> getRoots (String word) {
		// mikattaqAla -> myktql; mykt2ql; myktqAl; myk2qAl
		ArrayList<ArrayList<ConsDescription>> consonantCombinations = generateConsonantCombinations(word);
		
		LinkedHashSet<Root> rootSet = new LinkedHashSet<>();
		for (ArrayList<ConsDescription> consSet : consonantCombinations) {
			rootSet.addAll(generatePossibleRoots(consSet));
		}

		// myktql -> myk; myt; myq; myl; ...; mykt; mykq; ...; myktq, myktl, mktql.
		return new ArrayList<Root>(rootSet); 
	}

	private static ArrayList<ArrayList<ConsDescription>> generateConsonantCombinations (final String sourceWord) {
		return traverseDownward(sourceWord,
					new LinkedHashSet<ArrayList<ConsDescription>>(),
					new ArrayList<ConsDescription>(),
					0);
	}
	
	private static ArrayList<ArrayList<ConsDescription>> traverseDownward (
		final String sourceWord,
		LinkedHashSet<ArrayList<ConsDescription>> consonantCombinationSet,
		final ArrayList<ConsDescription> candidateCombination,
		final int nextPos
	) {
		if (sourceWord.isEmpty()) { return new ArrayList<ArrayList<ConsDescription>>(); }

		ArrayList<ArrayList<ConsDescription>> forks = new ArrayList<>();
		
		if (candidateCombination.isEmpty()) {
			candidateCombination.add(new ConsDescription(sourceWord.charAt(0), false, false));
			forks.add(copyOfConsDescriptionList(candidateCombination));
		} else { // has 1 item at least
			char lastLetter = candidateCombination.get(candidateCombination.size() - 1).consonant; // last letter in candidateCombination
			
			if (nextPos == sourceWord.length()) {
				consonantCombinationSet.add(copyOfConsDescriptionList(candidateCombination));
			} else {
				char letterToAdd = sourceWord.charAt(nextPos);
				
				if (LetterType.isVowel(letterToAdd) && !LetterType.isLongA(letterToAdd)) {
					if (letterToAdd == 'o' || letterToAdd == 'u') {
						candidateCombination.add(new ConsDescription('w', false, false));
					} else if (letterToAdd == 'e' || letterToAdd == 'i') {
						candidateCombination.add(new ConsDescription('y', false, false));
					}
					forks.add(copyOfConsDescriptionList(candidateCombination));
				} else if (LetterType.isLongA(letterToAdd)) {
					// Two forks: 1. Verb of type C or D, long A is penultimate vowel belonging to the root. 2. Long A is part of a prefix/suffix.
					
					// Fork 1: update last ConsDescription in candidateCombination: followed by long A
					ArrayList<ConsDescription> fork1 = copyOfConsDescriptionList(candidateCombination);
					// update lastConsDescription in fork1: followed by long A
					fork1.get(fork1.size() - 1).isFollowedByLongA = true;

					// Fork 2: long A is a part of a prefix/suffix; do not add to root description.
					ArrayList<ConsDescription> fork2 = copyOfConsDescriptionList(candidateCombination);

					forks.add(fork1);
					forks.add(fork2);
				} else if (LetterType.isConsonant(letterToAdd)) {
					if (letterToAdd == lastLetter &&
							!candidateCombination.get(candidateCombination.size() - 1).isFollowedByLongA) {
						// update last ConsDescription in candidateCombination: geminated
						ArrayList<ConsDescription> fork1 = copyOfConsDescriptionList(candidateCombination);
						fork1.get(fork1.size() - 1).isGeminated = true;
						forks.add(fork1);
					} else { // lastChar == cons1, charToAdd == cons2, cons1 != cons2
						candidateCombination.add(new ConsDescription(letterToAdd, false, false));
						forks.add(copyOfConsDescriptionList(candidateCombination));
					}
				}
			}
		}
		
		for (ArrayList<ConsDescription> fork : forks) {
			traverseDownward(sourceWord, consonantCombinationSet, fork, nextPos + 1);
		}
		
		return new ArrayList<ArrayList<ConsDescription>>(consonantCombinationSet);
	}
	
	private static ArrayList<Root> generatePossibleRoots (final ArrayList<ConsDescription> consSet) {
		LinkedHashSet<Root> rootSet = new LinkedHashSet<>();
		
		// Roots containing 3 to 5 consonants are considered.
		int n = ( consSet.size() > 5 ) ? 5 : consSet.size();
		for (int k = 3; k <= n; k++) {
			Iterator<int[]> combinationsIterator = CombinatoricsUtils.combinationsIterator(consSet.size(), k);
			while (combinationsIterator.hasNext()) {
				int[] combination = combinationsIterator.next();
				ArrayList<ConsDescription> curRootTemplate = new ArrayList<>();
				for (int consIndex : combination) {
					curRootTemplate.add(consSet.get(consIndex));
				}
				try {
					rootSet.add(new Root(curRootTemplate));
				} catch (IllegalArgumentException e) { e.printStackTrace(); }
			}
		}
		
		return new ArrayList<Root>(rootSet);
	}

	private static ArrayList<ConsDescription> copyOfConsDescriptionList (ArrayList<ConsDescription> oldList) {
		ArrayList<ConsDescription> newList = new ArrayList<>();
		for (ConsDescription cd : oldList) { newList.add(new ConsDescription(cd)); }
		return newList;
	}
}
