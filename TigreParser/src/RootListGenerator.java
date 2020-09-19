import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.apache.commons.math3.util.CombinatoricsUtils;

public class RootListGenerator {
	
	String word;
	LinkedHashSet<Root> consonantCombinations; // mikattaqAla -> myktql; mykt2ql; myktqAl; myk2qAl
	LinkedHashSet<Root> roots; // myktql -> myk; myt; myq; myl; ...; mykt; mykq; ...; myktq, myktl, mktql.
	
	public RootListGenerator (String word) {
		this.word = word;
		this.consonantCombinations = new LinkedHashSet<>();
		this.roots = new LinkedHashSet<>();
	}
	
	public void buildRootList () {
		
		generateConsonantCombinations(new Root(new ArrayList<ConsDescription>()), this.word, 0);
		
		for (Root consSet : this.consonantCombinations) {
			this.roots.addAll(generatePossibleRootsNew(consSet));
		}
		
	}
	
	private boolean generateConsonantCombinations (Root rootCandidate, String sourceWord, int nextPos) {
		ArrayList<Root> forks = new ArrayList<>();
		
		if (sourceWord.isEmpty()) {
			return false;
		} else if (rootCandidate.consTemplate.isEmpty()) {
			rootCandidate.consTemplate.add(new ConsDescription(sourceWord.charAt(0), false, false));
			forks.add(Root.newInstance(rootCandidate));
		} else { // has 1 item at least
			Letter lastLetter = Letter.newInstance(rootCandidate.consTemplate.get(rootCandidate.size() - 1).consonant); // copy of the last letter in rootCandidate
			
			if (nextPos == sourceWord.length()) {
				this.consonantCombinations.add(Root.newInstance(rootCandidate));
			} else {
				Letter letterToAdd = new Letter(sourceWord.charAt(nextPos));
				
				if (letterToAdd.isVowel() && !letterToAdd.isLongA()) {
					if (letterToAdd.character == 'o' || letterToAdd.character == 'u') {
						rootCandidate.consTemplate.add(new ConsDescription('w', false, false));
					} else if (letterToAdd.character == 'e' || letterToAdd.character == 'i') {
						rootCandidate.consTemplate.add(new ConsDescription('y', false, false));
					}
					
					forks.add(Root.newInstance(rootCandidate));
				} else if (letterToAdd.isLongA()) {
					// Two forks: 1. Verb of type C or D, long A is penultimate vowel belonging to the root. 2. Long A is part of a prefix/suffix.
					
					// Fork 1: update last ConsDescription in rootCandidate: followed by long A
					Root fork1 = Root.newInstance(rootCandidate);

					// update lastConsDescription in fork1: followed by long A
					ConsDescription cd_A = ConsDescription.newInstance(fork1.consTemplate.get(fork1.consTemplate.size() - 1));
					cd_A.followedByLongA = true;
					fork1.consTemplate.remove(fork1.consTemplate.size() - 1);
					fork1.consTemplate.add(cd_A);
					
					// Fork 2: long A is a part of a prefix/suffix; do not add to root description.
					Root fork2 = Root.newInstance(rootCandidate);

					forks.add(fork1);
					forks.add(fork2);
				} else if (letterToAdd.isConsonant()) {
					if (letterToAdd.equals(lastLetter) &&
							!rootCandidate.consTemplate.get(rootCandidate.size() - 1).followedByLongA) {
						// update last ConsDescription in rootCandidate: geminated
						Root fork1 = Root.newInstance(rootCandidate);

						fork1.consTemplate.remove(fork1.consTemplate.size() - 1);
						fork1.consTemplate.add(new ConsDescription(lastLetter, true, false));
						forks.add(fork1);
					} else { // lastChar == cons1, charToAdd == cons2, cons1 != cons2
						rootCandidate.consTemplate.add(new ConsDescription(letterToAdd, false, false));
						forks.add(Root.newInstance(rootCandidate));
					}
				}
			}
		}
		
		for (Root fork : forks) {
			generateConsonantCombinations(fork, sourceWord, nextPos + 1);
		}
		
		return true;
	}
	
	private LinkedHashSet<Root> generatePossibleRootsNew (Root consSet) {
		LinkedHashSet<Root> rootList = new LinkedHashSet<>();
		
		// Roots containing 3 to 5 consonants are considered.
		int n = ( consSet.size() > 5 ) ? 5 : consSet.size();
		for (int k = 3; k <= n; k++) {
			Iterator<int[]> combinationsIterator = CombinatoricsUtils.combinationsIterator(consSet.size(), k);
			while (combinationsIterator.hasNext()) {
				int[] combination = combinationsIterator.next();
				Root curRoot = new Root();
				for (int consIndex : combination) {
					curRoot.consTemplate.add(consSet.consTemplate.get(consIndex));
				}
				rootList.add(curRoot);
			}
		}
		
		return rootList;
	}
}
