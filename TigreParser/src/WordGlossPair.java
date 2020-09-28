import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class WordGlossPair {

	private static String unprocessedPartRegex = ".*\\[(?<unprocessed>.*)\\].*";
	private static Pattern unprocessedExtractorPattern = Pattern.compile(unprocessedPartRegex);
	
	private static String emptyAnalysisRegex = "^\\[(?<unprocessed>.*)\\]$";
	private static Pattern emptyAnalysisPattern = Pattern.compile(emptyAnalysisRegex);

	public String surfaceForm;
	public String lexicalForm;
	public boolean isFinalAnalysis;
	
	public WordGlossPair(String surfaceForm, String lexicalForm, boolean isFinalAnalysis) {
		this.surfaceForm = surfaceForm;
		this.lexicalForm = lexicalForm;
		this.isFinalAnalysis = isFinalAnalysis;
	}

	public static WordGlossPair createWithEmptyAnalysis (String surfaceForm) {
		return new WordGlossPair ("[" + surfaceForm + "]", "#",	false);
	}

	public boolean isEmptyAnalysis () {
		Matcher m = emptyAnalysisPattern.matcher(this.surfaceForm);
		return ( !this.isFinalAnalysis && this.lexicalForm == "#" && m.find() );
	}

	public String getUnanalysedPart () {
		Matcher m = unprocessedExtractorPattern.matcher(this.surfaceForm);
		if (m.find()) {
			return m.group("unprocessed")
				.replaceAll("[\\[\\]]", "");
		} else { return ""; }
	}

	public WordGlossPair insertInto (WordGlossPair pairToChange) {
		
		// Inserts this pair into the unanalysed part of pairToChange.
		WordGlossPair outputPair = new WordGlossPair();
		
		outputPair.surfaceForm = pairToChange.surfaceForm.replaceAll("\\[.*\\]", this.surfaceForm);
		outputPair.lexicalForm = pairToChange.lexicalForm.replaceAll("#", this.lexicalForm);
		outputPair.isFinalAnalysis = true;
		
		return outputPair;
	}
	
	public WordGlossPair() {
		this.surfaceForm = "";
		this.lexicalForm = "";
		this.isFinalAnalysis = false;
	}
	
	public static WordGlossPair newInstance(WordGlossPair wgPair) {
		return new WordGlossPair(wgPair.surfaceForm, wgPair.lexicalForm, wgPair.isFinalAnalysis);
	}
	
	public String getRawWord () {
		return surfaceForm.replaceAll("\\-", "");
	}
	
	public int hashCode() {
		return new HashCodeBuilder(109, 113)
				.append(surfaceForm)
				.append(lexicalForm)
				.toHashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
       if (!(obj instanceof WordGlossPair))
            return false;
        if (obj == this)
            return true;

        WordGlossPair rhs = (WordGlossPair) obj;
        return new EqualsBuilder().
            // if deriving: appendSuper(super.equals(obj)).
            append(surfaceForm, rhs.surfaceForm)
            .append(lexicalForm, rhs.lexicalForm)
            .append(isFinalAnalysis, rhs.isFinalAnalysis)
            .isEquals();
    }
}
