import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ConsDescription {
	// tArm -> {t, true}, {r, false}, {m, false}
	
	Letter consonant;
	boolean isGeminated;
	boolean followedByLongA;
	
	public ConsDescription(char consonant, boolean isGeminated, boolean followedByLongA) throws IllegalArgumentException {
		
		Letter letter = new Letter(consonant);
		
		this.consonant = letter;
		this.isGeminated = isGeminated;
		this.followedByLongA = followedByLongA;
	}
	
	public ConsDescription(Letter consonantLetter, boolean isGeminated, boolean followedByLongA) throws IllegalArgumentException {
		this.consonant = consonantLetter;
		this.isGeminated = isGeminated;
		this.followedByLongA = followedByLongA;
	}
	
	public static ConsDescription newInstance (ConsDescription cd) {
		return new ConsDescription(Letter.newInstance(cd.consonant), cd.isGeminated, cd.followedByLongA);
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
