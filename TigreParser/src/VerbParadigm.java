import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.ListIterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
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

	ArrayList<VerbParadigmCell> getSingleVerbParadigm (int numRadicals, VerbType verbType, VerbPreformative derivationalPrefix) {
		try {
			return this.paradigm.get(numRadicals)
				.get(verbType)
				.get(derivationalPrefix);
		} catch (NullPointerException e) {
			String message = String.format("No cell was found in paradigm for root: %d-radical, type %s, prefix %s", numRadicals, verbType.toString(), derivationalPrefix.toString());
			throw new NullPointerException(message);
		}
}

	public static class VerbParadigmBuilder {
		private static final Pattern paradigmHeaderPattern = Pattern.compile("^\\$radicals\\:(?<rad>[345]),type\\:(?<type>[ABCD]),prefix\\:(?<prefix>0|A|T|AT|ATTA|AN|AS|ATTAN|ATTAS|ASTA)$");

		// Same as VerbParadigm.paradigm, but stores cells in Set rather than in a List, for management of duplicates when parsing the paradigm file.
		// Sets of cells must be converted to Lists when creating a VerbParadigm object, for efficient subsequent iteration.
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

		private int curNumRadicals;
		private VerbType curVerbType;
		private VerbPreformative curDerivPrefix;

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
		
		public VerbParadigmBuilder readFrom (String paradigmFilePath) throws IOException, ConfigParseException {
			try (BufferedReader reader = new BufferedReader(new FileReader(paradigmFilePath))) {
				String currentLine;
				while ((currentLine = reader.readLine()) != null) {
					this.lines.add(currentLine);
				}
			}
			
			this.lineIterator = lines.listIterator();
			this.currentLineNum = 0;

			while (this.lineIterator.hasNext()) {
				String currentLine = this.lineIterator.next();
				currentLineNum++;
				if (lineHasContent(currentLine)) { // skip comments and empty lines
					if (currentLine.charAt(0) == '$') {
						this.lineIterator.previous();
						currentLineNum--;
						// this.lineIterator, this.currentLineNum: before paradigm header
						this.readSingleParadigm();
						// this.lineIterator, this.currentLineNum: before next paradigm header, or at last line
					} else {
						throw new ConfigParseException("Failed to read paradigm file header: paradigm file not read");
					}
				}
			}
			return this;
		}
		
		private void readSingleParadigm () throws ConfigParseException {
			// this.lineIterator, this.currentLineNum: before paradigm header
			String paradigmHeaderLine = this.lineIterator.next();
			this.currentLineNum++;

			// e. g. {"3", "A", "ASTA"} ->
			// this.curNumRadicals = 3
			// this.curVerbType = VerbType.A
			// this.curDerivPrefix = VerbPreformative.ASTA
			this.updateParadigmHeader(paradigmHeaderLine);
			
			// read everything before the next paradigm header (or the end of this file if there are no more headers)
			ArrayList<String> paradigmLines = this.readSingleParadigmAsLines();
			// this.lineIterator, this.currentLineNum: before next paradigm header, or at last line
	
			// build cell set
			LinkedHashSet<VerbParadigmCell> cellSet = this.buildCellSet(paradigmLines);
			this.putCellsInParadigm(cellSet);
		}

		private LinkedHashSet<VerbParadigmCell> buildCellSet (ArrayList<String> cellsAsLines) throws ConfigParseException {
			LinkedHashSet<VerbParadigmCell> cellSet = new LinkedHashSet<>();
			for (String line : cellsAsLines) {
				try {
					VerbParadigmCell cell = VerbParadigmCell.VerbParadigmCellBuilder.parseAndBuild(line, this.curNumRadicals, this.curVerbType, this.curDerivPrefix);
					cellSet.add(cell);
				} catch (ConfigParseException e) {
					throw new ConfigParseException("Failed to read paradigm line");
				}
			}
			return cellSet;
		}

		private void putCellsInParadigm (LinkedHashSet<VerbParadigmCell> cellSet) {
			// check whether paradigmAsSets already has cells for this num radicals + verb type + derivational prefix.
			// If it does, add cells to the corresponding set.
			// If it doesn't, create a new set.
			
			LinkedHashSet<VerbParadigmCell> curSingleVerbParadigm;
			if (this.paradigmAsSets.containsKey(this.curNumRadicals) &&
				this.paradigmAsSets.get(this.curNumRadicals).containsKey(this.curVerbType) &&
				this.paradigmAsSets.get(this.curNumRadicals).get(this.curVerbType).containsKey(this.curDerivPrefix)) {
				curSingleVerbParadigm = this.paradigmAsSets.get(this.curNumRadicals).get(this.curVerbType).get(this.curDerivPrefix); // get existing paradigm
				curSingleVerbParadigm.addAll(cellSet); // add entries
				this.paradigmAsSets.get(this.curNumRadicals).get(this.curVerbType).put(this.curDerivPrefix, curSingleVerbParadigm); // put the updated paradigm back
			} else if (this.paradigmAsSets.containsKey(this.curNumRadicals) &&
						this.paradigmAsSets.get(this.curNumRadicals).containsKey(this.curVerbType) &&
						!this.paradigmAsSets.get(this.curNumRadicals).get(this.curVerbType).containsKey(this.curDerivPrefix)) {
				this.paradigmAsSets.get(this.curNumRadicals).get(this.curVerbType).put(this.curDerivPrefix, cellSet);
			} else if (this.paradigmAsSets.containsKey(this.curNumRadicals) &&
						!this.paradigmAsSets.get(this.curNumRadicals).containsKey(this.curVerbType)) {
				LinkedHashMap<VerbPreformative, LinkedHashSet<VerbParadigmCell>> derivPrefixMap = new LinkedHashMap<>();
				derivPrefixMap.put(this.curDerivPrefix, cellSet);
				this.paradigmAsSets.get(this.curNumRadicals).put(this.curVerbType, derivPrefixMap);
			} else { // if !this.paradigmAsSets.containsKey(this.curNumRadicals)
				LinkedHashMap<VerbPreformative, LinkedHashSet<VerbParadigmCell>> derivPrefixMap = new LinkedHashMap<>();
				derivPrefixMap.put(this.curDerivPrefix, cellSet);
				LinkedHashMap<VerbType, LinkedHashMap<VerbPreformative, LinkedHashSet<VerbParadigmCell>>> typeMap = new LinkedHashMap<>();
				typeMap.put(this.curVerbType, derivPrefixMap);
				this.paradigmAsSets.put(this.curNumRadicals, typeMap);
			}
		}

		private ArrayList<String> readSingleParadigmAsLines () {
			// this.lineIterator, this.currentLineNum: at line after paradigm header
			ArrayList<String> paradigmLines = new ArrayList<>();
			String curLine;
			while (this.lineIterator.hasNext()) {
				curLine = this.lineIterator.next();
				this.currentLineNum++;

				if (!curLine.isEmpty() &&
					curLine.charAt(0) == '$') {
					// new paradigm header
					break;
				}
				if (lineHasContent(curLine)) { paradigmLines.add(curLine); }	
			}
			
			if (this.lineIterator.hasNext()) {
				// not at the end of file
				this.lineIterator.previous();
				this.currentLineNum--;
				// this.lineIterator, this.currentLineNum: before next paradigm header
			}

			// this.lineIterator, this.currentLineNum: before next paradigm header, or at last line
			return paradigmLines;
		}
		
		private void updateParadigmHeader (String line) throws ConfigParseException {
			Matcher matcher = paradigmHeaderPattern.matcher(line);
			try {
				matcher.find();
				this.curNumRadicals = Integer.parseInt(matcher.group("rad"));
				this.curVerbType = VerbType.parseVerbType(matcher.group("type"));
				this.curDerivPrefix = VerbPreformative.parseVerbPreformative(matcher.group("prefix"));
			} catch (IllegalStateException|IllegalArgumentException e) {
				throw new ConfigParseException(String.format("Failed to read paradigm header at line %d", this.currentLineNum));
			}
		}
		
		private static boolean lineHasContent (String line) {
			if (line.isEmpty() ||
				line.charAt(0) == '#' || // comment
				line.matches("^[ \\t]+$")) { // empty line
				return false;
			}
			return true;
		}

		// Converts LinkedHashMap<... ... LinkedHashSet<VerbParadigmCell>> to LinkedHashMap<... ... ArrayList<VerbParadigmCell>> for efficient subsequent iteration over single verb paradigms (i. e. collections of VerbParadigmCell objects).

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
