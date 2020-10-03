import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.ListIterator;
import java.util.Map.Entry;

public class VerbParadigm {
	
	private LinkedHashMap<VerbStemDescription, ArrayList<VerbParadigmCell>> paradigm;
	
	private VerbParadigm () {
		this.paradigm = new LinkedHashMap<>();
	}

	ArrayList<VerbParadigmCell> getSingleVerbParadigm (VerbStemDescription stemDescription) {
		try {
			return this.paradigm.get(stemDescription);
		} catch (NullPointerException e) {
			String message = String.format("No cell was found in paradigm for stem: %s", stemDescription.toString());
			throw new NullPointerException(message);
		}
}

	public static class VerbParadigmBuilder {

		// Same as VerbParadigm.paradigm, but stores cells in Set rather than in a List, for management of duplicates when parsing the paradigm file.
		// Sets of cells must be converted to Lists when creating a VerbParadigm object, for efficient subsequent iteration.
		private LinkedHashMap<VerbStemDescription, LinkedHashSet<VerbParadigmCell>> paradigmAsSets;
		
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
						this.readAndBuildSingleParadigm();
						// this.lineIterator, this.currentLineNum: before next paradigm header, or at last line
					} else {
						throw new ConfigParseException("Failed to read paradigm file header: paradigm file not read");
					}
				}
			}
			return this;
		}
		
		private void readAndBuildSingleParadigm () throws ConfigParseException {
			// this.lineIterator, this.currentLineNum: before paradigm header
			String paradigmHeaderLine = this.lineIterator.next();
			this.currentLineNum++;

			VerbStemDescription stemDescription = VerbStemDescription.parse(paradigmHeaderLine);
			
			// read everything before the next paradigm header (or the end of this file if there are no more headers)
			ArrayList<String> paradigmLines = this.readSingleParadigmAsLines();
			// this.lineIterator, this.currentLineNum: before next paradigm header, or at last line
	
			// build cell set
			LinkedHashSet<VerbParadigmCell> cellSet = buildCellSet(paradigmLines, stemDescription);
			this.putCellsInParadigm(cellSet, stemDescription);
		}

		private static LinkedHashSet<VerbParadigmCell> buildCellSet (ArrayList<String> cellsAsLines, VerbStemDescription stemDescription) throws ConfigParseException {
			LinkedHashSet<VerbParadigmCell> cellSet = new LinkedHashSet<>();
			for (String line : cellsAsLines) {
				try {
					VerbParadigmCell cell = new VerbParadigmCell.VerbParadigmCellBuilder().parseAndBuild(line, stemDescription);
					cellSet.add(cell);
				} catch (ConfigParseException e) {
					throw new ConfigParseException("Failed to read paradigm line", e);
				}
			}
			return cellSet;
		}

		private void putCellsInParadigm (LinkedHashSet<VerbParadigmCell> cellSet, VerbStemDescription stemDescription) {
			if (this.paradigmAsSets.containsKey(stemDescription)) {
				// get the existing paradigm and add new entries
				this.paradigmAsSets.get(stemDescription)
							.addAll(cellSet);
			} else {
				// create a new paradigm with these entries
				this.paradigmAsSets.put(stemDescription, cellSet);
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
		
		private static boolean lineHasContent (String line) {
			if (line.isEmpty() ||
				line.charAt(0) == '#' || // comment
				line.matches("^[ \\t]+$")) { // empty line
				return false;
			}
			return true;
		}

		// Converts LinkedHashMap<... ... LinkedHashSet<VerbParadigmCell>> to LinkedHashMap<... ... ArrayList<VerbParadigmCell>> for efficient subsequent iteration over single verb paradigms (i. e. collections of VerbParadigmCell objects).

		private LinkedHashMap<VerbStemDescription, ArrayList<VerbParadigmCell>> getParadigmAsLists () {
			LinkedHashMap<VerbStemDescription, ArrayList<VerbParadigmCell>> paradigmAsLists = new LinkedHashMap<>();
			for (Entry<VerbStemDescription, LinkedHashSet<VerbParadigmCell>> pair : this.paradigmAsSets.entrySet()) {
				VerbStemDescription stemDescription = pair.getKey();
				LinkedHashSet<VerbParadigmCell> cellSet = pair.getValue();
				ArrayList<VerbParadigmCell> cellList = new ArrayList<>(cellSet);
				paradigmAsLists.put(stemDescription, cellList);
			}
			return paradigmAsLists;
		}
	}
}
