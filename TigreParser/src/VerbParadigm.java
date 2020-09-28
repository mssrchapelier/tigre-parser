import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map.Entry;

public class VerbParadigm {
	
	/*
	 * Structure of paradigm:
	 * paradigm -- number_of_radicals : typeParadigm
	 * typeParadigm -- verb_type : derivativesParadigm
	 * derivativesParadigm -- derivational_prefix : paradigm_cells
	 * 
	 */
	private LinkedHashMap<
					Integer, LinkedHashMap< // number of radicals
											VerbType, LinkedHashMap< // verb type (A, B, C, D)
																	VerbPreformative, ArrayList<VerbParadigmCell> // derivational prefix (t-, a-, at- ...)
																	>
											>
					> paradigm;
	
	private VerbParadigm () {
		this.paradigm = new LinkedHashMap<>();
	}

	// builds all forms for a given verb stem

	public ArrayList<WordGlossPair> buildAllForms (VerbStem stem) throws NullPointerException {
		
		ArrayList<WordGlossPair> formList = new ArrayList<>();
		ArrayList<VerbParadigmCell> singleVerbParadigm = new ArrayList<>();
		try {
			singleVerbParadigm = this.paradigm.get(stem.numRadicals)
					.get(stem.verbType)
					.get(stem.derivationalPrefix);
			for (VerbParadigmCell cell : singleVerbParadigm) {
				formList.add(new VerbFormBuilder(cell, stem).build());
			}
			return formList;
		} catch (NullPointerException e) {
			String message = String.format("No cell was found in paradigm for root: %d-radical, type %s, prefix %s", stem.numRadicals, stem.verbType.toString(), stem.derivationalPrefix.toString());
			throw new NullPointerException(message);
		}
	}

	public static class VerbParadigmBuilder {
	// Same as VerbParadigm.paradigm, but stores cells in Set rather than in a List, for management of duplicates when parsing the paradigm file.
	// Sets of cells must be converted to Lists when creating a VerbParadigm object, for efficient subsequent iteration.
		final private static Pattern paradigmHeaderPattern = Pattern.compile("^\\$radicals\\:(?<rad>[345]),type\\:(?<type>[ABCD]),prefix\\:(?<prefix>0|A|T|AT|ATTA|AN|AS|ATTAN|ATTAS|ASTA)$");

		private LinkedHashMap<
						Integer, LinkedHashMap< // number of radicals
												VerbType, LinkedHashMap< // verb type (A, B, C, D)
																		VerbPreformative, LinkedHashSet<VerbParadigmCell> // derivational prefix (t-, a-, at- ...)
																	>
											>
					> paradigmAsSets;
		
		private ArrayList<String> lines;
		private ListIterator<String> lineIterator;
		private int currentLineNum;

		public VerbParadigmBuilder () {
			this.lines = new ArrayList<>();
			this.lineIterator = lines.listIterator();
			this.currentLineNum = 0;

			this.paradigmAsSets = new LinkedHashMap<>();
		}

		public VerbParadigm build () {
			VerbParadigm paradigmObject = new VerbParadigm();
			paradigmObject.paradigm = this.getParadigmAsLists();
			return paradigmObject;
		}
		
		public VerbParadigmBuilder readFrom (String paradigmFilePath) throws IOException, ParseException {
			BufferedReader reader = new BufferedReader(new FileReader(paradigmFilePath));
			String currentLine;
			
			while ((currentLine = reader.readLine()) != null) {
				this.lines.add(currentLine);
			}
			reader.close();
			
			this.lineIterator = lines.listIterator();
			while (this.lineIterator.hasNext()) {
				currentLine = this.lineIterator.next();
				currentLineNum++;
				if (lineHasContent(currentLine)) { // skip comments and empty lines
					if (currentLine.charAt(0) == '$') {
						this.lineIterator.previous();
						currentLineNum--;
						try {
							this.readSingleParadigm();
						} catch (ParseException e) { e.printStackTrace(); } // ParseException handled here; parsing will continue with the next paradigm in the file.
					} else {
						throw new ParseException(String.format("Line %d: Each paradigm must start with $ (dollar sign).", this.currentLineNum), this.currentLineNum);
					}
				}
			}
			return this;
		}
		
		private void readSingleParadigm () throws ParseException { // return true when done
			this.currentLineNum++;

			// e. g. {"3", "A", "ASTA"} -> 3-radical, type A, prefix ASTA
			String[] paradigmHeaderTriple = this.readParadigmHeader(this.lineIterator.next());
			
			int numRadicals;
			VerbType verbType;
			VerbPreformative derivPrefix;
			
			try {
				numRadicals = Integer.parseInt(paradigmHeaderTriple[0]);
				verbType = VerbType.parseVerbType(paradigmHeaderTriple[1]);
				derivPrefix = VerbPreformative.parseVerbPreformative(paradigmHeaderTriple[2]);
			} catch (IllegalArgumentException e) {
				throw new ParseException(String.format(e.getMessage() + "Error in paradigm header at line %s. Paradigm not read.", this.currentLineNum), this.currentLineNum);
			}
			
			// read everything before the next paradigm header (or the end of this file if there are no more headers)
			ArrayList<String> curParadigmLines = new ArrayList<>();
			String curParadigmLine = "";
			while (this.lineIterator.hasNext()) {
				curParadigmLine = this.lineIterator.next();
				this.currentLineNum++;
				
				if (!curParadigmLine.isEmpty() &&
					curParadigmLine.charAt(0) == '$') {
					break;
				}
			
				if (lineHasContent(curParadigmLine)) { curParadigmLines.add(curParadigmLine); }	
			}
			
			if (this.lineIterator.hasNext()) {
				this.lineIterator.previous();
				this.currentLineNum--;
			}

			
			// build cell list
			
			LinkedHashSet<VerbParadigmCell> cellsAsSet = new LinkedHashSet<>();
			for (String line : curParadigmLines) {
				this.currentLineNum++;
				try {
					cellsAsSet.add(this.buildCellFromLine(line, numRadicals, verbType, derivPrefix));
				} catch (ParseException e) { e.printStackTrace(); }
			}
			ArrayList<VerbParadigmCell> cellsAsList = new ArrayList<>(cellsAsSet);
			
			// check whether paradigmAsSets already has cells for this num radicals + verb type + derivational prefix.
			// If it does, add cells to the corresponding set.
			// If it doesn't, create a new set.
			
			LinkedHashSet<VerbParadigmCell> curSingleVerbParadigm;
			if (this.paradigmAsSets.containsKey(numRadicals) &&
				this.paradigmAsSets.get(numRadicals).containsKey(verbType) &&
				this.paradigmAsSets.get(numRadicals).get(verbType).containsKey(derivPrefix)) {
				curSingleVerbParadigm = this.paradigmAsSets.get(numRadicals).get(verbType).get(derivPrefix); // get existing paradigm
				curSingleVerbParadigm.addAll(cellsAsList); // add non-duplicate entries
				this.paradigmAsSets.get(numRadicals).get(verbType).put(derivPrefix, curSingleVerbParadigm); // put the updated paradigm back
			} else if (this.paradigmAsSets.containsKey(numRadicals) &&
						this.paradigmAsSets.get(numRadicals).containsKey(verbType) &&
						!this.paradigmAsSets.get(numRadicals).get(verbType).containsKey(derivPrefix)) {
				this.paradigmAsSets.get(numRadicals).get(verbType).put(derivPrefix, cellsAsSet);
			} else if (this.paradigmAsSets.containsKey(numRadicals) &&
						!this.paradigmAsSets.get(numRadicals).containsKey(verbType)) {
				LinkedHashMap<VerbPreformative, LinkedHashSet<VerbParadigmCell>> derivPrefixMap = new LinkedHashMap<>();
				derivPrefixMap.put(derivPrefix, cellsAsSet);
				this.paradigmAsSets.get(numRadicals).put(verbType, derivPrefixMap);
			} else { // if !this.paradigmAsSets.containsKey(numRadicals)
				LinkedHashMap<VerbPreformative, LinkedHashSet<VerbParadigmCell>> derivPrefixMap = new LinkedHashMap<>();
				derivPrefixMap.put(derivPrefix, cellsAsSet);
				LinkedHashMap<VerbType, LinkedHashMap<VerbPreformative, LinkedHashSet<VerbParadigmCell>>> typeMap = new LinkedHashMap<>();
				typeMap.put(verbType, derivPrefixMap);
				this.paradigmAsSets.put(numRadicals, typeMap);
			}
		}
		
		private String[] readParadigmHeader(String line) throws ParseException {
			// $radicals:3,type:A,prefix:ASTA => {"3", "A", "ASTA"}

			String[] headerTriple = new String[3];
			
			Matcher m = paradigmHeaderPattern.matcher(line);
			if (m.find() && m.groupCount() == 3) {
				headerTriple[0] = m.group("rad");
				headerTriple[1] = m.group("type");
				headerTriple[2] = m.group("prefix");
			} else {
				throw new ParseException(String.format("Error in paradigm header at line %s. Paradigm not read.", this.currentLineNum), this.currentLineNum);
			}
			return headerTriple;
		}
		
		// NB: The cell being returned has the following fields empty: acceptedNumRadicals; acceptedVerbType; acceptedDerivPrefix.
		// Manipulate the cell accordingly in the calling method before adding this cell to paradigmAsSets.
		
		private VerbParadigmCell buildCellFromLine (String line, int numRadicals, VerbType verbType, VerbPreformative derivPrefix) throws ParseException {
			VerbParadigmCell cell = new VerbParadigmCell();
			cell.acceptedNumRadicals = numRadicals;
			cell.acceptedVerbType = verbType;
			cell.acceptedDerivPrefix = derivPrefix;
			
			try {
				Pattern p = Pattern.compile("^(?<grammemeset>.+)\\t(?<surfacepattern>.+)\\t(?<lexpattern>.+)$");
				Matcher m = p.matcher(line);
				
				String[] lineParts = new String[3];
				
				m.find();
				lineParts[0] = m.group("grammemeset");
				lineParts[1] = m.group("surfacepattern");
				lineParts[2] = m.group("lexpattern");
				
				// *** read grammemeset ***
				p = Pattern.compile("(?<mood>INDIC|JUSS|IMP)" +
									"\\+(?<tense>IMPF|PRF|NA)" +
									"\\+(?<person>[123])" +
									"\\+(?<gender>[MFC])" +
									"\\+(?<number>SG|PL)");
				m = p.matcher(lineParts[0]);
				
				m.find();
				cell.grammemeSet.mood = Mood.parseMood(m.group("mood"));
				cell.grammemeSet.tense = Tense.parseTense(m.group("tense"));
				cell.grammemeSet.person = Person.parsePerson(m.group("person"));
				cell.grammemeSet.gender = Gender.parseGender(m.group("gender"));
				cell.grammemeSet.number = Number.parseNumber(m.group("number"));
				
				// *** read surfacepattern and lexpattern ***
				ArrayList<String> surfacePatternParts = new ArrayList<>(Arrays.asList(lineParts[1].split("\\+")));
				ArrayList<String> lexPatternParts = new ArrayList<>(Arrays.asList(lineParts[2].split("\\+")));
				
				ListIterator<String> surfaceIterator = surfacePatternParts.listIterator();
				ListIterator<String> lexIterator = lexPatternParts.listIterator();
				
				String curMorphemeSurface;
				String curMorphemeLex;
				
				// read prefixes
				while (surfaceIterator.hasNext() && lexIterator.hasNext()) {
					curMorphemeSurface = surfaceIterator.next();
					curMorphemeLex = lexIterator.next();
					
					if (curMorphemeSurface.matches("^.*_.*$")) { // contains underscore, i. e. is a root morpheme
						surfaceIterator.previous();
						lexIterator.previous();
						break;
					}
					
					if (!curMorphemeSurface.matches("0")) {
						cell.prefixes.add(new MorphemeDescriptionPair(curMorphemeSurface, curMorphemeLex));
					}
				}
				
				// read root
				
				curMorphemeSurface = surfaceIterator.next();
				curMorphemeLex = lexIterator.next();
				String[] rootSurfaceParts = curMorphemeSurface.split("_"); // i. e. "aa_121" -> {"aa", "121"}
				int[] gemPattern = new int[rootSurfaceParts[1].length()];
				for (int i = 0; i < gemPattern.length; i++) {
					gemPattern[i] = Integer.parseInt(rootSurfaceParts[1].substring(i, i+1));
				}
				cell.vowelPattern = new MorphemeDescriptionPair(rootSurfaceParts[0], curMorphemeLex);
				cell.geminationPattern = gemPattern;
				
				// read suffixes
				
				while (surfaceIterator.hasNext() && lexIterator.hasNext()) {
					curMorphemeSurface = surfaceIterator.next();
					curMorphemeLex = lexIterator.next();
					if (!curMorphemeSurface.matches("0")) {
						cell.suffixes.add(new MorphemeDescriptionPair(curMorphemeSurface, curMorphemeLex));
					}
				}
				
				return cell;
				
			} catch (IndexOutOfBoundsException|NoSuchElementException|IllegalArgumentException e) {
				throw new ParseException(String.format(e.getMessage() + "%nError in form description at line %s. Line not read.", this.currentLineNum), this.currentLineNum);
			}
		}
		
		private static boolean lineHasContent (String line) {
			if (line.isEmpty() ||
				line.charAt(0) == '#' ||
				line.matches("^[ \\t]+$")) { // skip comments and empty lines
				return false;
			}
			return true;
		}

		private LinkedHashMap<Integer, LinkedHashMap<VerbType, LinkedHashMap<VerbPreformative,ArrayList<VerbParadigmCell>>>> getParadigmAsLists () {
			
			LinkedHashMap<Integer, LinkedHashMap<VerbType, LinkedHashMap<VerbPreformative,ArrayList<VerbParadigmCell>>>> paradigmAsLists = new LinkedHashMap<>();

			for (Entry<Integer, LinkedHashMap<VerbType, LinkedHashMap<VerbPreformative, LinkedHashSet<VerbParadigmCell>>>> typeMap : this.paradigmAsSets.entrySet()) {
				int curNumRadicals = typeMap.getKey();
				paradigmAsLists.put(curNumRadicals, new LinkedHashMap<VerbType, LinkedHashMap<VerbPreformative, ArrayList<VerbParadigmCell>>>());
				for (Entry<VerbType, LinkedHashMap<VerbPreformative, LinkedHashSet<VerbParadigmCell>>> derivPrefixMap : typeMap.getValue().entrySet()) {
					VerbType curType = derivPrefixMap.getKey();
					paradigmAsLists.get(curNumRadicals).put(curType, new LinkedHashMap<VerbPreformative, ArrayList<VerbParadigmCell>>());
					for (Entry<VerbPreformative, LinkedHashSet<VerbParadigmCell>> cellSet : derivPrefixMap.getValue().entrySet()) {
						VerbPreformative curDerivPrefix = cellSet.getKey();
						paradigmAsLists.get(curNumRadicals).get(curType).put(curDerivPrefix, new ArrayList<VerbParadigmCell>(cellSet.getValue()));
					}
				}
			}

			return paradigmAsLists;
		}

	}
}
