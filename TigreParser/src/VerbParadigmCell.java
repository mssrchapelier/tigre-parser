import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.NoSuchElementException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class VerbParadigmCell {
	
	VerbStemDescription stemDescription;
	
	VerbGrammemeSet grammemeSet;
	
	// prefixes
	ArrayList<MorphemeAnalysis> prefixes;

	// root
	MorphemeAnalysis vowelPattern;
	int[] geminationPattern;

	// suffixes
	ArrayList<MorphemeAnalysis> suffixes;

	private VerbParadigmCell (VerbStemDescription stemDescription,
					VerbGrammemeSet grammemeSet,
					ArrayList<MorphemeAnalysis> prefixes,
					MorphemeAnalysis vowelPattern,
					int[] geminationPattern,
					ArrayList<MorphemeAnalysis> suffixes) {
		this.stemDescription = stemDescription;
		this.grammemeSet = grammemeSet;
		this.prefixes = prefixes;
		this.vowelPattern = vowelPattern;
		this.geminationPattern = geminationPattern;
		this.suffixes = suffixes;
	}

	WordGlossPair applyTo (VerbStem stem) {
		String surface = "";
		String gloss = "";

		ListIterator<MorphemeAnalysis> it;
		MorphemeAnalysis curMorpheme;
		
		// appending prefixes
		
		it = this.prefixes.listIterator();
		
		while (it.hasNext()) {
			curMorpheme = it.next();
			surface += curMorpheme.surface;
			gloss += curMorpheme.gloss;
			if (it.hasNext()) {
				surface += "-";
				gloss += "-";
			}
		}
		
		if (!this.prefixes.isEmpty()) {
			surface += "-";
			gloss += "-";
		}
		
		// appending root
		
		int numRadicals = stem.rootAsLetters.size();
		for (int radIndex = 0; radIndex < numRadicals; radIndex++) {
			// geminate if applicable
			char consToAdd = stem.rootAsLetters.get(radIndex);
			
			if (this.geminationPattern[radIndex] > 0) {
				surface += consToAdd;
			}
			
			if (this.geminationPattern[radIndex] == 2 && LetterType.isSubjectToGemination(stem.rootAsLetters.get(radIndex))) {
				surface += consToAdd;
			}
			// append vowel if applicable
			if (radIndex != numRadicals - 1) {
				char charToAppend = this.vowelPattern.surface.charAt(radIndex);
				if (charToAppend != '0') {
					surface += charToAppend;
				}
			}
		}
		gloss += stem.rootAsString + ":";
		gloss += this.vowelPattern.gloss;
		
		// appending suffixes
		
		if (!this.suffixes.isEmpty()) {
			surface += "-";
			gloss += "-";
		}
		
		it = this.suffixes.listIterator();
		
		while (it.hasNext()) {
			curMorpheme = it.next();
			surface += curMorpheme.surface;
			gloss += curMorpheme.gloss;
			if (it.hasNext()) {
				surface += "-";
				gloss += "-";
			}
		}
		
		return new WordGlossPair(surface, gloss);
	}

	// --- FOR TESTING ---
	/*
	public void testPrintA () {
		if (this.acceptedNumRadicals == 3
			&& this.acceptedVerbType == VerbType.A
			&& this.acceptedDerivPrefix == VerbPreformative.A
			&& this.grammemeSet.mood == Mood.INDICATIVE
			&& this.grammemeSet.tense == Tense.IMPERFECT
			&& this.grammemeSet.person == Person.P1
			&& this.grammemeSet.gender == Gender.COMMON
			&& this.grammemeSet.number == Number.SINGULAR) { System.out.println(this.toString()); }
	}
	*/

	// --- FOR TESTING ---
	/*
	public String toString () {
		String message = "";
		message += "--- cell ---" + "\n";
		message += String.format("%d-rad, type %s, prefix %s", this.acceptedNumRadicals, this.acceptedVerbType.toString(), this.acceptedDerivPrefix.toString()) + "\n";
		message += String.format("grammemes: mood %s, tense %s, person %s, gender %s, number %s",
					this.grammemeSet.mood.toString(),
					this.grammemeSet.tense.toString(),
					this.grammemeSet.person.toString(),
					this.grammemeSet.gender.toString(),
					this.grammemeSet.number.toString()) + "\n";
		
		String prefixesSurface = "", prefixesLex = "";
		for (MorphemeAnalysis morpheme : this.prefixes) {
			prefixesSurface += morpheme.surface + "-";
			prefixesLex += morpheme.gloss + "-";
		}
		message += String.format("prefixes: %s; %s", prefixesSurface, prefixesLex) + "\n";

		message += String.format("vowel pattern: %s; %s", this.vowelPattern.surface, this.vowelPattern.gloss) + "\n";

		String gemPattern = "";
		for (int i = 0; i < this.geminationPattern.length; i++) { gemPattern += Integer.toString(this.geminationPattern[i]); }
		message += String.format("gemination pattern: %s", gemPattern) + "\n";

		String suffixesSurface = "", suffixesLex = "";
		for (MorphemeAnalysis morpheme : this.suffixes) {
			suffixesSurface += morpheme.surface + "-";
			suffixesLex += morpheme.gloss + "-";
		}
		message += String.format("suffixes: %s; %s", suffixesSurface, suffixesLex);

		return message;
	}
	*/

	@Override
	public int hashCode() {
		return new HashCodeBuilder(109, 113)
				.append(stemDescription)	
				.append(prefixes)
				.append(vowelPattern)
				.append(geminationPattern)
				.append(suffixes)
				.append(grammemeSet)
				.toHashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof VerbParadigmCell)) { return false; }
        	if (obj == this) { return true; }

		VerbParadigmCell rhs = (VerbParadigmCell) obj;
		return new EqualsBuilder()
        		.append(stemDescription, rhs.stemDescription)	
        		.append(prefixes, rhs.prefixes)
        		.append(vowelPattern, rhs.vowelPattern)
        		.append(geminationPattern, rhs.geminationPattern)
        		.append(suffixes, rhs.suffixes)
        		.append(grammemeSet, rhs.grammemeSet)
        		.isEquals();
	}

	static class VerbParadigmCellBuilder {
		
		private static final Pattern cellDescriptionPattern = Pattern.compile("^(?<grammemeset>.+)\\t(?<surfacepattern>.+)\\t(?<lexpattern>.+)$");
		
		private VerbStemDescription stemDescription;
		private VerbGrammemeSet grammemeSet;
		private ArrayList<MorphemeAnalysis> prefixes;
		private MorphemeAnalysis vowelPattern;
		private int[] geminationPattern;
		private ArrayList<MorphemeAnalysis> suffixes;

		VerbParadigmCellBuilder () {}

		VerbParadigmCell parseAndBuild (String line, VerbStemDescription stemDescription) throws ConfigParseException {
			try {
				this.stemDescription = stemDescription;
				Matcher cellDescriptionMatcher = cellDescriptionPattern.matcher(line);
				cellDescriptionMatcher.find();
				this.stemDescription = stemDescription;
				this.grammemeSet = VerbGrammemeSet.parse(cellDescriptionMatcher.group("grammemeset"));
				this.prefixes = new ArrayList<>();
				this.suffixes = new ArrayList<>();
				// this.vowelPattern, this.geminationPattern are not initialised yet; initialised in this.readRoot
				readMorphemes(cellDescriptionMatcher.group("surfacepattern"),
							cellDescriptionMatcher.group("lexpattern"));
				return new VerbParadigmCell(this.stemDescription,
										this.grammemeSet,
										this.prefixes,
										this.vowelPattern,
										this.geminationPattern,
										this.suffixes);
			} catch (IndexOutOfBoundsException|NoSuchElementException|IllegalArgumentException e) {
				throw new ConfigParseException("Failed to read paradigm line");
			}
		}
	
		private void readMorphemes (String surfaceString, String lexString) {
			ArrayList<String> surfacePatternParts = new ArrayList<>(Arrays.asList(surfaceString.split("\\+")));
			ArrayList<String> lexPatternParts = new ArrayList<>(Arrays.asList(lexString.split("\\+")));
	
			ListIterator<String> surfaceIterator = surfacePatternParts.listIterator();
			ListIterator<String> lexIterator = lexPatternParts.listIterator();
			
			String curSurface;
			String curLex;
			boolean rootHasBeenRead = false;
	
			while (surfaceIterator.hasNext() && lexIterator.hasNext()) {
				curSurface = surfaceIterator.next();
				curLex = lexIterator.next();
	
				if (curSurface.matches("^.*_.*$")) {
					// is root
					readRoot(curSurface, curLex);
					rootHasBeenRead = true;
				} else {
					if (rootHasBeenRead) { readSuffix(curSurface, curLex); }
					else { readPrefix(curSurface, curLex); }
				}
			}
		}
	
		private void readRoot (String surfaceString, String lexString) {
			String[] surfaceParts = surfaceString.split("_"); // i. e. "aa_121" -> {"aa", "121"}
			String vowelPatternString = surfaceParts[0];
			String gemPatternString = surfaceParts[1];
	
			int[] gemPattern = new int[gemPatternString.length()];
			for (int i = 0; i < gemPattern.length; i++) {
				gemPattern[i] = Integer.parseInt(gemPatternString.substring(i, i+1));
			}
			
			this.vowelPattern = new MorphemeAnalysis(vowelPatternString, lexString);
			this.geminationPattern = gemPattern;
		}
		
		private void readSuffix (String surfaceString, String lexString) {
			if (!surfaceString.matches("0")) {
				this.suffixes.add(new MorphemeAnalysis(surfaceString, lexString));
			}
		}
	
		private void readPrefix (String surfaceString, String lexString) {
			if (!surfaceString.matches("0")) {
				this.prefixes.add(new MorphemeAnalysis(surfaceString, lexString));
			}
		}
	}

	
	
}
