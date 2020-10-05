class RootTemplate {

	// In vowelPattern, '0' is used to indicate the absence of a vowel at the respective slot.
	private final char[] vowelPattern;

	// In geminationPattern, false indicates no gemination, true indicates gemination.
	private final boolean[] geminationPattern;

	private final String rootGrammemeGloss;

	private RootTemplate (char[] vowelPattern, boolean[] geminationPattern, String rootGrammemeGloss) {
		this.vowelPattern = vowelPattern;
		this.geminationPattern = geminationPattern;
		this.rootGrammemeGloss = rootGrammemeGloss;
	}

	MorphemeAnalysis applyTo (VerbStem stem) {
		String morphemeSurface = "";
		String morphemeGloss = String.format("%s:%s",
						stem.rootDictionaryGloss,
						this.rootGrammemeGloss);

		char[] consonants = stem.rootConsonants;
		int numRadicals = consonants.length;
		for (int radIndex = 0; radIndex < numRadicals; radIndex++) {
			// geminate if applicable
			char consToAdd = consonants[radIndex];
			morphemeSurface += consToAdd;
			if (this.geminationPattern[radIndex] == true
				&& LetterType.isSubjectToGemination(consToAdd)) {
				morphemeSurface += consToAdd;
			}

			// append vowel if applicable
			if (radIndex != numRadicals - 1) {
				char charToAppend = this.vowelPattern[radIndex];
				if (charToAppend != '0') {
					morphemeSurface += charToAppend;
				}
			}
		}

		return new MorphemeAnalysis(morphemeSurface, morphemeGloss);
	}

	static RootTemplate parseAndBuild (String surface, String rootGrammemeGloss) throws ConfigParseException {
		String exceptionMessage = String.format("Illegal root format: %s", surface);
		ConfigParseException exception = new ConfigParseException(exceptionMessage);

		if (!surface.matches("^.*_.*$")) {
			throw new ConfigParseException(exceptionMessage);
		}

		String[] surfaceParts = surface.split("_"); // e.g. "aa_121" -> {"aa", "121"}
		String vowelPatternString = surfaceParts[0]; // "aa"
		int numVowelSlots = vowelPatternString.length();
		String gemPatternString = surfaceParts[1]; // "121"
		int numConsSlots = gemPatternString.length();

		if (numVowelSlots < 2 || numVowelSlots > 4
			|| numConsSlots < 3 || numConsSlots > 5
			|| numVowelSlots != numConsSlots - 1) {
			throw exception;
		}

		char[] vowelPattern = new char[numVowelSlots];
		for (int slotNum = 0; slotNum < numVowelSlots; slotNum++) {
			char vowel = vowelPatternString.charAt(slotNum);
			if (LetterType.isVowel(vowel) || vowel == '0') {
				vowelPattern[slotNum] = vowel;
			} else {
				throw exception;
			}
		}

		boolean[] gemPattern = new boolean[numConsSlots];
		for (int slotNum = 0; slotNum < numConsSlots; slotNum++) {
			char gemIndicator = gemPatternString.charAt(slotNum);
			switch (gemIndicator) {
				case '1': gemPattern[slotNum] = false; break;
				case '2': gemPattern[slotNum] = true; break;
				default: throw exception;
			}
		}
		
		return new RootTemplate(vowelPattern, gemPattern, rootGrammemeGloss);
	}
}
