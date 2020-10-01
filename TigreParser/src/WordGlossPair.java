import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

class WordGlossPair {

	private static final String unanalysedPartRegex = ".*\\[(?<unanalysed>.*)\\].*";
	private static final Pattern unanalysedExtractorPattern = Pattern.compile(unanalysedPartRegex);
	
	private static final String emptyAnalysisRegex = "^\\[(?<unanalysed>.*)\\]$";
	private static final Pattern emptyAnalysisPattern = Pattern.compile(emptyAnalysisRegex);

	// Format for surfaceForm, lexicalForm: morpheme1-...-[unanalysedPart]-...-morphemeN

	final String surfaceForm;
	final String lexicalForm;
	final boolean isFinalAnalysis;
	
	WordGlossPair(String surfaceForm, String lexicalForm) {
		this.surfaceForm = surfaceForm;
		this.lexicalForm = lexicalForm;
		this.isFinalAnalysis = isFinalAnalysis(surfaceForm, lexicalForm);
	}

	private static boolean isFinalAnalysis (String surfaceForm, String lexicalForm) {
		if (surfaceForm.contains("[") || lexicalForm.contains("#")) {
			return false;
		} else {
			return true;
		}
	}

	static WordGlossPair createWithEmptyAnalysis (String surfaceForm) {
		return new WordGlossPair ("[" + surfaceForm + "]", "#");
	}

	boolean isEmptyAnalysis () {
		Matcher m = emptyAnalysisPattern.matcher(this.surfaceForm);
		return ( !this.isFinalAnalysis && this.lexicalForm == "#" && m.find() );
	}

	String getUnanalysedPart () {
		Matcher m = unanalysedExtractorPattern.matcher(this.surfaceForm);
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
		
		String surfaceForm = pairToChange.surfaceForm.replaceAll("\\[.*\\]", this.surfaceForm);
		String lexicalForm = pairToChange.lexicalForm.replaceAll("#", this.lexicalForm);
		
		return new WordGlossPair(surfaceForm, lexicalForm);
	}
	
	WordGlossPair (WordGlossPair wgp) {
		this.surfaceForm = wgp.surfaceForm;
		this.lexicalForm = wgp.lexicalForm;
		this.isFinalAnalysis = wgp.isFinalAnalysis;
	}

	String getRawWord () {
		return surfaceForm.replaceAll("\\-", "");
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(109, 113)
				.append(surfaceForm)
				.append(lexicalForm)
				.toHashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof WordGlossPair)) { return false; }
		if (obj == this) { return true; }

		WordGlossPair rhs = (WordGlossPair) obj;
		return new EqualsBuilder().append(surfaceForm, rhs.surfaceForm)
					.append(lexicalForm, rhs.lexicalForm)
					.append(isFinalAnalysis, rhs.isFinalAnalysis)
					.isEquals();
	}
}
