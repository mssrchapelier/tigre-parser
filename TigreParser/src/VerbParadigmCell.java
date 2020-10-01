import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.NoSuchElementException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class VerbParadigmCell {
	
	int acceptedNumRadicals;
	VerbType acceptedVerbType;
	VerbPreformative acceptedDerivPrefix;
	
	VerbGrammemeSet grammemeSet;
	
	// prefixes
	ArrayList<MorphemeDescriptionPair> prefixes;

	// root
	MorphemeDescriptionPair vowelPattern;
	int[] geminationPattern;

	// suffixes
	ArrayList<MorphemeDescriptionPair> suffixes;

	// --- FOR TESTING ---
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

	// --- FOR TESTING ---
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
		for (MorphemeDescriptionPair morpheme : this.prefixes) {
			prefixesSurface += morpheme.surfaceForm + "-";
			prefixesLex += morpheme.lexicalForm + "-";
		}
		message += String.format("prefixes: %s; %s", prefixesSurface, prefixesLex) + "\n";

		message += String.format("vowel pattern: %s; %s", this.vowelPattern.surfaceForm, this.vowelPattern.lexicalForm) + "\n";

		String gemPattern = "";
		for (int i = 0; i < this.geminationPattern.length; i++) { gemPattern += Integer.toString(this.geminationPattern[i]); }
		message += String.format("gemination pattern: %s", gemPattern) + "\n";

		String suffixesSurface = "", suffixesLex = "";
		for (MorphemeDescriptionPair morpheme : this.suffixes) {
			suffixesSurface += morpheme.surfaceForm + "-";
			suffixesLex += morpheme.lexicalForm + "-";
		}
		message += String.format("suffixes: %s; %s", suffixesSurface, suffixesLex);

		return message;
	}
	
	public VerbParadigmCell () {
		this.acceptedNumRadicals = 0;
		this.acceptedVerbType = VerbType.UNKNOWN;
		this.acceptedDerivPrefix = VerbPreformative.UNKNOWN;
		this.prefixes = new ArrayList<>();
		this.vowelPattern = new MorphemeDescriptionPair("", "");
		this.geminationPattern = new int[0];
		this.suffixes = new ArrayList<>();
		this.grammemeSet = new VerbGrammemeSet();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(109, 113)
				.append(acceptedNumRadicals)
				.append(acceptedVerbType)
				.append(acceptedDerivPrefix)	
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
        		.append(acceptedNumRadicals, rhs.acceptedNumRadicals)
        		.append(acceptedVerbType, rhs.acceptedVerbType)
        		.append(acceptedDerivPrefix, rhs.acceptedDerivPrefix)	
        		.append(prefixes, rhs.prefixes)
        		.append(vowelPattern, rhs.vowelPattern)
        		.append(geminationPattern, rhs.geminationPattern)
        		.append(suffixes, rhs.suffixes)
        		.append(grammemeSet, rhs.grammemeSet)
        		.isEquals();
	}

	static class VerbParadigmCellBuilder {
		
		private static final Pattern cellDescriptionPattern = Pattern.compile("^(?<grammemeset>.+)\\t(?<surfacepattern>.+)\\t(?<lexpattern>.+)$");
		
		VerbParadigmCellBuilder () {}

		static VerbParadigmCell parseAndBuild (String line, int numRadicals, VerbType verbType, VerbPreformative derivPrefix) throws ConfigParseException {
			VerbParadigmCell cell = new VerbParadigmCell();
			cell.acceptedNumRadicals = numRadicals;
			cell.acceptedVerbType = verbType;
			cell.acceptedDerivPrefix = derivPrefix;
			
			try {
				Matcher cellDescriptionMatcher = cellDescriptionPattern.matcher(line);
				cellDescriptionMatcher.find();
				cell.grammemeSet = VerbGrammemeSet.parse(cellDescriptionMatcher.group("grammemeset"));
				cell = readMorphemes(cellDescriptionMatcher.group("surfacepattern"),
							cellDescriptionMatcher.group("lexpattern"),
							cell);
				
				return cell;
				
			} catch (IndexOutOfBoundsException|NoSuchElementException|IllegalArgumentException e) {
				throw new ConfigParseException("Failed to read paradigm line");
			}
		}
	
		private static VerbParadigmCell readMorphemes (String surfaceString, String lexString, VerbParadigmCell cell) {
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
					readRoot(curSurface, curLex, cell);
					rootHasBeenRead = true;
				} else {
					if (rootHasBeenRead) { readSuffix(curSurface, curLex, cell); }
					else { readPrefix(curSurface, curLex, cell); }
				}
			}
	
			return cell;
		}
	
		private static VerbParadigmCell readRoot (String surfaceString, String lexString, VerbParadigmCell cell) {
			String[] surfaceParts = surfaceString.split("_"); // i. e. "aa_121" -> {"aa", "121"}
			String vowelPatternString = surfaceParts[0];
			String gemPatternString = surfaceParts[1];
	
			int[] gemPattern = new int[gemPatternString.length()];
			for (int i = 0; i < gemPattern.length; i++) {
				gemPattern[i] = Integer.parseInt(gemPatternString.substring(i, i+1));
			}
			
			cell.vowelPattern = new MorphemeDescriptionPair(vowelPatternString, lexString);
			cell.geminationPattern = gemPattern;
	
			return cell;
		}
		
		private static VerbParadigmCell readSuffix (String surfaceString, String lexString, VerbParadigmCell cell) {
			if (!surfaceString.matches("0")) {
				cell.suffixes.add(new MorphemeDescriptionPair(surfaceString, lexString));
			}
			return cell;
		}
	
		private static VerbParadigmCell readPrefix (String surfaceString, String lexString, VerbParadigmCell cell) {
			if (!surfaceString.matches("0")) {
				cell.prefixes.add(new MorphemeDescriptionPair(surfaceString, lexString));
			}
			return cell;
		}
	}

	
	
}
