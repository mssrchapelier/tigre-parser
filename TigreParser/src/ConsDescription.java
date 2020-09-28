import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ConsDescription {
	// tArm -> {t, true}, {r, false}, {m, false}
	
	public char consonant;
	public boolean isGeminated;
	public boolean followedByLongA;
	
	public ConsDescription(char letter, boolean isGeminated, boolean followedByLongA) throws IllegalArgumentException {
		if (!LetterType.isConsonant(letter)) { throw new IllegalArgumentException("Illegal parameter: char consonant must be a consonant."); }
		this.consonant = letter;
		this.isGeminated = isGeminated;
		this.followedByLongA = followedByLongA;
	}
	
	public static ConsDescription newInstance (ConsDescription cd) {
		return new ConsDescription(cd.consonant, cd.isGeminated, cd.followedByLongA);
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(29, 241).append(consonant).append(isGeminated).append(followedByLongA).toHashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
       if (!(obj instanceof ConsDescription))
            return false;
        if (obj == this)
            return true;

        ConsDescription rhs = (ConsDescription) obj;
        return new EqualsBuilder().
            append(consonant, rhs.consonant).
            append(isGeminated, rhs.isGeminated).
            append(followedByLongA, rhs.followedByLongA).
            isEquals();
    }
}
