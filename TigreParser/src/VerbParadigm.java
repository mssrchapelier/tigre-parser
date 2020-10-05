import java.io.LineNumberReader;
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

		public VerbParadigmBuilder () {
			this.paradigmAsSets = new LinkedHashMap<>();
		}

		public VerbParadigm build () {
			VerbParadigm paradigmObject = new VerbParadigm();
			paradigmObject.paradigm = this.getParadigmAsLists();
			return paradigmObject;
		}
		
		public VerbParadigmBuilder readFrom (String paradigmFilePath) throws IOException, ConfigParseException {
			try (LineNumberReader reader = new LineNumberReader(new FileReader(paradigmFilePath))) {
				String line;
				VerbStemDescription stemDescription = null;
				LinkedHashSet<VerbParadigmCell> cellSet = null;
				while ( (line = reader.readLine()) != null ) {
					if (lineHasContent(line)) {
						try {
							if (line.charAt(0) == '$') {
								// read next paradigm header
								if (stemDescription != null && cellSet != null) {
									this.putCellsInParadigm(cellSet, stemDescription);
								}
								stemDescription = VerbStemDescription.parse(line);
								cellSet = new LinkedHashSet<>();
							} else {
								// read next cell description
								if (stemDescription == null || cellSet == null) {
									String message = String.format("The paradigm header for this cell description has not been set: %s", line);
									throw new ConfigParseException(message);
								}
								VerbParadigmCell cell = new VerbParadigmCell.VerbParadigmCellBuilder().parseAndBuild(line, stemDescription);
								cellSet.add(cell);
							}
						} catch (ConfigParseException cause) {
							String description = "Failed to parse verb paradigm file";
							throw new ConfigParseException.ConfigParseExceptionBuilder().appendDescription(description)
															.appendFilePath(paradigmFilePath)
															.appendLineNumber(reader.getLineNumber())
															.appendCause(cause)
															.build();
						}
					}
				}
				return this;
			} catch (ConfigParseException e) {
				// reset this.paradigmAsSets
				this.paradigmAsSets = new LinkedHashMap<>();
				throw e;
			}
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
