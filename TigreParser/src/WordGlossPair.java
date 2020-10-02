import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

class WordGlossPair {

	private static final String unanalysedPartRegex = ".*\\[(?<unanalysed>.*)\\].*";
	private static final Pattern unanalysedExtractorPattern = Pattern.compile(unanalysedPartRegex);
	
	private static final String emptyAnalysisRegex = "^\\[(?<unanalysed>.*)\\]$";
	private static final Pattern emptyAnalysisPattern = Pattern.compile(emptyAnalysisRegex);

	// Format for surface, gloss: morpheme1-...-[unanalysedPart]-...-morphemeN

	final String surface;
	final String gloss;
	final boolean isFinalAnalysis;
	
	WordGlossPair(String surface, String gloss) {
		this.surface = surface;
		this.gloss = gloss;
		this.isFinalAnalysis = isFinalAnalysis(surface, gloss);
	}

	private static boolean isFinalAnalysis (String surface, String gloss) {
		if (surface.contains("[") || gloss.contains("#")) {
			return false;
		} else {
			return true;
		}
	}

	static WordGlossPair createWithEmptyAnalysis (String surface) {
		return new WordGlossPair ("[" + surface + "]", "#");
	}

	boolean isEmptyAnalysis () {
		Matcher m = emptyAnalysisPattern.matcher(this.surface);
		return ( !this.isFinalAnalysis && this.gloss == "#" && m.find() );
	}

	String getUnanalysedPart () {
		Matcher m = unanalysedExtractorPattern.matcher(this.surface);
		if (m.find()) {
			return m.group("unanalysed")
				.replaceAll("[\\[\\]]", "");
		} else { return ""; }
	}

	// Replaces the unanalysed part of this WordGlossPair with the analysis specified in replacement (possibly non-final). Returns a WordGlossPair with the new analysis included.
	// Format for replacement: surface1:lex1-..:..-[unanalysedPart]-..:..-surfaceN:lexN

	WordGlossPair insertReplacement (String replacement) {
		String[] morphemes = replacement.split("\\-");

		String analysisSurface = "";
		String analysisLex = "";
		boolean isFinalAnalysis = true;

		for (int i = 0; i < morphemes.length; i++) {
			String morpheme = morphemes[i];
			String[] morphemeParts = morpheme.split("\\:");
			if (morphemeParts.length != 2) {
				throw new IllegalArgumentException("replacement is not formatted properly");
			}
			String morphemeSurface = morphemeParts[0];
			String morphemeLex = morphemeParts[1];

			if (morphemeSurface.charAt(0) == '['
				&& morphemeLex.charAt(0) == '#') {
				// unanalysed part -> the returned WGPair is not a final analysis
				isFinalAnalysis = false;
			}

			analysisSurface += morphemeSurface;
			analysisLex += morphemeLex;
			
			if (i < morphemes.length - 1) {
				analysisSurface += "-";
				analysisLex += "-";
			}
		}
		
		WordGlossPair innerPartAnalysis = new WordGlossPair(analysisSurface, analysisLex);
		WordGlossPair newAnalysis = innerPartAnalysis.insertInto(this);
		return newAnalysis; 
	}

	WordGlossPair insertInto (WordGlossPair pairToChange) {
		
		// Inserts this pair into the unanalysed part of pairToChange.
		
		String surface = pairToChange.surface.replaceAll("\\[.*\\]", this.surface);
		String gloss = pairToChange.gloss.replaceAll("#", this.gloss);
		
		return new WordGlossPair(surface, gloss);
	}
	
	WordGlossPair (WordGlossPair wgp) {
		this.surface = wgp.surface;
		this.gloss = wgp.gloss;
		this.isFinalAnalysis = wgp.isFinalAnalysis;
	}

	String getRawWord () {
		return surface.replaceAll("\\-", "");
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(109, 113)
				.append(surface)
				.append(gloss)
				.toHashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof WordGlossPair)) { return false; }
		if (obj == this) { return true; }

		WordGlossPair rhs = (WordGlossPair) obj;
		return new EqualsBuilder().append(surface, rhs.surface)
					.append(gloss, rhs.gloss)
					.append(isFinalAnalysis, rhs.isFinalAnalysis)
					.isEquals();
	}
}
