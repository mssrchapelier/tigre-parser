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
	RootTemplate rootTemplate;

	// suffixes
	ArrayList<MorphemeAnalysis> suffixes;

	private VerbParadigmCell (VerbStemDescription stemDescription,
					VerbGrammemeSet grammemeSet,
					ArrayList<MorphemeAnalysis> prefixes,
					RootTemplate rootTemplate,
					ArrayList<MorphemeAnalysis> suffixes) {
		this.stemDescription = stemDescription;
		this.grammemeSet = grammemeSet;
		this.prefixes = prefixes;
		this.rootTemplate = rootTemplate;
		this.suffixes = suffixes;
	}

	WordAnalysis applyTo (VerbStem stem) {
		ArrayList<AnalysisSegment> segments = new ArrayList<>();
		// add prefixes
		segments.addAll(AnalysisSegmentUtils.cloneList(this.prefixes));
		// construct and add root
		segments.add(this.rootTemplate.applyTo(stem));
		// add suffixes
		segments.addAll(AnalysisSegmentUtils.cloneList(this.suffixes));
		return new WordAnalysis(segments);
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(109, 113)
				.append(stemDescription)	
				.append(prefixes)
				.append(rootTemplate)
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
        		.append(rootTemplate, rhs.rootTemplate)
        		.append(suffixes, rhs.suffixes)
        		.append(grammemeSet, rhs.grammemeSet)
        		.isEquals();
	}

	static class VerbParadigmCellBuilder {
		
		private static final Pattern cellDescriptionPattern = Pattern.compile("^(?<grammemeset>.+)\\t(?<surfacepattern>.+)\\t(?<lexpattern>.+)$");
		
		private VerbStemDescription stemDescription;
		private VerbGrammemeSet grammemeSet;
		private ArrayList<MorphemeAnalysis> prefixes;
		private RootTemplate rootTemplate;
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
				// this.rootTemplate is not initialised yet; initialised in this.readMorphemes
				readMorphemes(cellDescriptionMatcher.group("surfacepattern"),
							cellDescriptionMatcher.group("lexpattern"));
				return new VerbParadigmCell(this.stemDescription,
										this.grammemeSet,
										this.prefixes,
										this.rootTemplate,
										this.suffixes);
			} catch (IllegalStateException | ConfigParseException e) {
				String message = String.format("Failed to read paradigm line: %s", line);
				throw new ConfigParseException(message, e);
			}
		}
	
		private void readMorphemes (String surfaceString, String lexString) throws ConfigParseException {
			ArrayList<String> surfacePatternParts = new ArrayList<>(Arrays.asList(surfaceString.split("\\+")));
			ArrayList<String> lexPatternParts = new ArrayList<>(Arrays.asList(lexString.split("\\+")));
	
			ListIterator<String> surfaceIterator = surfacePatternParts.listIterator();
			ListIterator<String> lexIterator = lexPatternParts.listIterator();
			
			String curSurface;
			String curGloss;
			boolean rootHasBeenRead = false;
	
			while (surfaceIterator.hasNext() && lexIterator.hasNext()) {
				curSurface = surfaceIterator.next();
				curGloss = lexIterator.next();
	
				if (curSurface.matches("^.*_.*$")) {
					// is root
					this.rootTemplate = RootTemplate.parseAndBuild(curSurface, curGloss);
					rootHasBeenRead = true;
				} else {
					if (rootHasBeenRead) { readSuffix(curSurface, curGloss); }
					else { readPrefix(curSurface, curGloss); }
				}
			}

			if (!rootHasBeenRead) {
				throw new ConfigParseException("Failed to locate root in description");
			}
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
