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

public class VerbParadigmBuilder {
	// Same as VerbParadigm.paradigm, but stores cells in Set rather than in a List, for duplicate management when parsing the paradigm file.
	// Sets of cells must be converted to Lists when creating a VerbParadigm object, for subsequent efficient iteration.
	LinkedHashMap<
					Integer, LinkedHashMap< // number of radicals
											VerbType, LinkedHashMap< // verb type (A, B, C, D)
																	VerbPreformative, LinkedHashSet<VerbParadigmCell> // derivational prefix (t-, a-, at- ...)
																>
										>
				> paradigm;
	
	private ArrayList<String> lines;
	private ListIterator<String> lineIterator;
	private int currentLineNum;
	
	public VerbParadigmBuilder () {
		this.paradigm = new LinkedHashMap<>();
		this.lines = new ArrayList<>();
		this.lineIterator = lines.listIterator();
		this.currentLineNum = 0;
	}
	
	public VerbParadigm build (String paradigmFilePath) throws IOException, ParseException {
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
		
		VerbParadigm paradigmObject = new VerbParadigm();
		
		for (Entry<Integer, LinkedHashMap<VerbType, LinkedHashMap<VerbPreformative, LinkedHashSet<VerbParadigmCell>>>> typeMap : this.paradigm.entrySet()) {
			int curNumRadicals = typeMap.getKey();
			paradigmObject.paradigm.put(curNumRadicals, new LinkedHashMap<VerbType, LinkedHashMap<VerbPreformative, ArrayList<VerbParadigmCell>>>());
			for (Entry<VerbType, LinkedHashMap<VerbPreformative, LinkedHashSet<VerbParadigmCell>>> derivPrefixMap : typeMap.getValue().entrySet()) {
				VerbType curType = derivPrefixMap.getKey();
				paradigmObject.paradigm.get(curNumRadicals).put(curType, new LinkedHashMap<VerbPreformative, ArrayList<VerbParadigmCell>>());
				for (Entry<VerbPreformative, LinkedHashSet<VerbParadigmCell>> cellSet : derivPrefixMap.getValue().entrySet()) {
					VerbPreformative curDerivPrefix = cellSet.getKey();
					paradigmObject.paradigm.get(curNumRadicals).get(curType).put(curDerivPrefix, new ArrayList<VerbParadigmCell>(cellSet.getValue()));
				}
			}
		}
		
		return paradigmObject;
	}
	
	private void readSingleParadigm () throws ParseException { // return true when done
		this.currentLineNum++;
		String[] paradigmHeaderTriple = this.readParadigmHeader(this.lineIterator.next());
		
		int curNumRadicals;
		VerbType curType;
		VerbPreformative curDerivPrefix;
		
		try {
			curNumRadicals = Integer.parseInt(paradigmHeaderTriple[0]);
			curType = VerbType.parseVerbType(paradigmHeaderTriple[1]);
			curDerivPrefix = VerbPreformative.parseVerbPreformative(paradigmHeaderTriple[2]);
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
			
			curParadigmLines.add(curParadigmLine); // including empty lines and comments
		}
		
		if (this.lineIterator.hasNext()) {
			this.lineIterator.previous();
			this.currentLineNum--;
		}

		
		// build cell list
		
		LinkedHashSet<VerbParadigmCell> cellsAsSet = new LinkedHashSet<>();
		for (String line : curParadigmLines) {
			this.currentLineNum++;
			if (lineHasContent(line)) {
				try {
					cellsAsSet.add(this.buildCellFromLine(line, curNumRadicals, curType, curDerivPrefix));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}
		ArrayList<VerbParadigmCell> cellsAsList = new ArrayList<>(cellsAsSet);
		
		// check whether paradigm already has cells for this num radicals + verb type + derivational prefix.
		// If it does, add cells to the corresponding set.
		// If it doesn't, create a new set.
		
		LinkedHashSet<VerbParadigmCell> curParadigm;
		if (this.paradigm.containsKey(curNumRadicals) &&
			this.paradigm.get(curNumRadicals).containsKey(curType) &&
			this.paradigm.get(curNumRadicals).get(curType).containsKey(curDerivPrefix)) {
			curParadigm = this.paradigm.get(curNumRadicals).get(curType).get(curDerivPrefix); // get existing paradigm
			curParadigm.addAll(cellsAsList); // add non-duplicate entries
			this.paradigm.get(curNumRadicals).get(curType).put(curDerivPrefix, curParadigm); // put the updated paradigm back
		} else if (this.paradigm.containsKey(curNumRadicals) &&
					this.paradigm.get(curNumRadicals).containsKey(curType) &&
					!this.paradigm.get(curNumRadicals).get(curType).containsKey(curDerivPrefix)) {
			paradigm.get(curNumRadicals).get(curType).put(curDerivPrefix, cellsAsSet);
		} else if (this.paradigm.containsKey(curNumRadicals) &&
					!this.paradigm.get(curNumRadicals).containsKey(curType)) {
			LinkedHashMap<VerbPreformative, LinkedHashSet<VerbParadigmCell>> derivPrefixMap = new LinkedHashMap<>();
			derivPrefixMap.put(curDerivPrefix, cellsAsSet);
			this.paradigm.get(curNumRadicals).put(curType, derivPrefixMap);
		} else { // if !paradigm.containsKey(curNumRadicals)
			LinkedHashMap<VerbPreformative, LinkedHashSet<VerbParadigmCell>> derivPrefixMap = new LinkedHashMap<>();
			derivPrefixMap.put(curDerivPrefix, cellsAsSet);
			LinkedHashMap<VerbType, LinkedHashMap<VerbPreformative, LinkedHashSet<VerbParadigmCell>>> typeMap = new LinkedHashMap<>();
			typeMap.put(curType, derivPrefixMap);
			this.paradigm.put(curNumRadicals, typeMap);
		}
	}
	
	private String[] readParadigmHeader(String line) throws ParseException {
		String[] headerTriple = new String[3];
		
		Pattern p = Pattern.compile("^\\$radicals\\:(?<rad>[345]),type\\:(?<type>[ABCD]),prefix\\:(?<prefix>0|A|T|AT|ATTA|AN|AS|ATTAN|ATTAS|ASTA)$");
		Matcher m = p.matcher(line);
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
	// Manipulate the cell accordingly in the calling method before adding this cell to paradigm.
	
	private VerbParadigmCell buildCellFromLine (String line, int curNumRadicals, VerbType curType, VerbPreformative curDerivPrefix) throws ParseException {
		VerbParadigmCell cell = new VerbParadigmCell();
		cell.acceptedNumRadicals = curNumRadicals;
		cell.acceptedVerbType = curType;
		cell.acceptedDerivPrefix = curDerivPrefix;
		
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
			// prefixes: in linear order
			
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
				
				// comply with the inverse ordering of prefixes in MorphemeDescriptionPair.prefixes
				if (!curMorphemeSurface.matches("0")) {
					cell.prefixes.add(0, new MorphemeDescriptionPair(curMorphemeSurface, curMorphemeLex));
				}
			}
			
			// read root
			
			curMorphemeSurface = surfaceIterator.next();
			curMorphemeLex = lexIterator.next();
			String[] rootSurfaceParts = curMorphemeSurface.split("_"); // i. e. "aa_121" -> {"aa", "121"}
			int[] gemPattern = new int[rootSurfaceParts[1].length()];
			for (int i = 0; i < rootSurfaceParts[1].length(); i++) {
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

}
