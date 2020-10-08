package com.mssrchapelier.TigreParser.components.VerbProcessor.Paradigm;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

import com.mssrchapelier.TigreParser.components.VerbProcessor.StemDescription.StemDescription;
import com.mssrchapelier.TigreParser.components.utils.misc.ConfigParseException;

class Paradigm {
	
	private LinkedHashMap<StemDescription, ArrayList<ParadigmCell>> paradigm;
	
	private Paradigm () {
		this.paradigm = new LinkedHashMap<>();
	}

	ArrayList<ParadigmCell> getSingleParadigm (StemDescription stemDescription) {
		try {
			return this.paradigm.get(stemDescription);
		} catch (NullPointerException e) {
			String message = String.format("No cell was found in paradigm for stem: %s", stemDescription.toString());
			throw new NullPointerException(message);
		}
}

	static class ParadigmBuilder {

		// Same as VerbParadigm.paradigm, but stores cells in Set rather than in a List, for management of duplicates when parsing the paradigm file.
		// Sets of cells must be converted to Lists when creating a VerbParadigm object, for efficient subsequent iteration.
		private LinkedHashMap<StemDescription, LinkedHashSet<ParadigmCell>> paradigmAsSets;

		ParadigmBuilder () {
			this.paradigmAsSets = new LinkedHashMap<>();
		}

		Paradigm build () {
			Paradigm paradigmObject = new Paradigm();
			paradigmObject.paradigm = this.getParadigmAsLists();
			return paradigmObject;
		}
		
		ParadigmBuilder readFrom (String paradigmFilePath) throws IOException, ConfigParseException {
			try (LineNumberReader reader = new LineNumberReader(new FileReader(paradigmFilePath))) {
				String line;
				StemDescription stemDescription = null;
				LinkedHashSet<ParadigmCell> cellSet = null;
				while ( (line = reader.readLine()) != null ) {
					if (lineHasContent(line)) {
						try {
							if (line.charAt(0) == '$') {
								// read next paradigm header
								if (stemDescription != null && cellSet != null) {
									this.putCellsInParadigm(cellSet, stemDescription);
								}
								stemDescription = StemDescription.parse(line);
								cellSet = new LinkedHashSet<>();
							} else {
								// read next cell description
								if (stemDescription == null || cellSet == null) {
									String message = String.format("The paradigm header for this cell description has not been set: %s", line);
									throw new ConfigParseException(message);
								}
								ParadigmCell cell = new ParadigmCell.ParadigmCellBuilder().parseAndBuild(line, stemDescription);
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

		private void putCellsInParadigm (LinkedHashSet<ParadigmCell> cellSet, StemDescription stemDescription) {
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

		private LinkedHashMap<StemDescription, ArrayList<ParadigmCell>> getParadigmAsLists () {
			LinkedHashMap<StemDescription, ArrayList<ParadigmCell>> paradigmAsLists = new LinkedHashMap<>();
			for (Entry<StemDescription, LinkedHashSet<ParadigmCell>> pair : this.paradigmAsSets.entrySet()) {
				StemDescription stemDescription = pair.getKey();
				LinkedHashSet<ParadigmCell> cellSet = pair.getValue();
				ArrayList<ParadigmCell> cellList = new ArrayList<>(cellSet);
				paradigmAsLists.put(stemDescription, cellList);
			}
			return paradigmAsLists;
		}
	}
}
