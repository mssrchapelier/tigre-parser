import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class WordGlossPair {
	String surfaceForm;
	String lexicalForm;
	boolean isFinalAnalysis;
	
	public WordGlossPair(String surfaceForm, String lexicalForm, boolean isFinalAnalysis) {
		this.surfaceForm = surfaceForm;
		this.lexicalForm = lexicalForm;
		this.isFinalAnalysis = isFinalAnalysis;
	}
	
	public WordGlossPair() {
		this.surfaceForm = "";
		this.lexicalForm = "";
		this.isFinalAnalysis = false;
	}
	
	public static WordGlossPair newInstance(WordGlossPair wdPair) {
		return new WordGlossPair(wdPair.surfaceForm, wdPair.lexicalForm, wdPair.isFinalAnalysis);
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
