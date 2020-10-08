package com.mssrchapelier.TigreParser.components.utils.misc;

import org.apache.commons.lang3.ArrayUtils;

public class LetterType {
	
	private static final char[] consonants = {'h', 'l', 'H', 'm', 'r', 's', 'x', 'q', 'b', 't', 'n', '>', 'k', 'w', '<', 'z', 'y', 'd', 'G', 'g', 'T', 'C', 'S', 'f', 'c', 'Z', 'p', 'P', 'X'};
	private static final char[] vowels = {'i', 'e', 'a', 'A', 'u', 'o'};
	
	private static final char[] laryngeals = {'<', '>', 'h', 'H', 'X'};
	private static final char[] semivowels = {'w', 'y'};
	private static final char longA = 'A';
	
	// schwa symbol is used during preprocessing (i. e. generating geminated variants in Transliterator) to differentiate between two syllables which have identical consonants, and a geminated consonant (e. g. between [kə][ka] and kka)
	// schwa is NOT treated as a (vowel) phoneme
	private static char schwa = 'ə';
	
	public static boolean isConsonant (char c) {
		return ArrayUtils.contains(consonants, c) ? true : false;
	}
	
	public static boolean isVowel (char c) {
		return ArrayUtils.contains(vowels, c) ? true : false;
	}
	
	public static boolean isLongA (char c) {
		return c == longA ? true : false;
	}
	
	public static boolean isLaryngeal (char c) {
		return ArrayUtils.contains(laryngeals, c) ? true : false;
	}
	
	public static boolean isSemivowel (char c) {
		return ArrayUtils.contains(semivowels, c) ? true : false;
	}
	
	public static boolean isSchwa (char c) {
		return c == schwa ? true : false;
	}

	public static boolean isSubjectToGemination (char c) {
		if (isConsonant(c)) {
			// laryngeals and semivowels are never geminated
			if (isLaryngeal(c) || isSemivowel(c)) { return false; }
			else { return true; }
		} else { return false; }
	}
}
