import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Letter {
	char character;
	
	static char[] consonants = {'h', 'l', 'H', 'm', 'r', 's', 'x', 'q', 'b', 't', 'n', '>', 'k', 'w', '<', 'z', 'y', 'd', 'G', 'g', 'T', 'C', 'S', 'f', 'c', 'Z', 'p', 'P', 'X'};
	private static char[] vowels = {'i', 'e', 'a', 'A', 'u', 'o'};
	
	private static char[] laryngeals = {'<', '>', 'h', 'H', 'X'};
	private static char[] semivowels = {'w', 'y'};
	private static char longA = 'A';
	
	public Letter (char c) {
		this.character = c;
	}
	
	public static Letter newInstance (Letter letter) {
		return new Letter(letter.character);
	}
	
	boolean isConsonant () {
		return ArrayUtils.contains(consonants, this.character) ? true : false;
	}
	
	boolean isVowel () {
		return ArrayUtils.contains(vowels, this.character) ? true : false;
	}
	
	boolean isLaryngeal () {
		return ArrayUtils.contains(laryngeals, this.character) ? true : false;
	}
	
	boolean isSemivowel () {
		return ArrayUtils.contains(semivowels, this.character) ? true : false;
	}
	
	boolean isLongA () {
		return this.character == longA ? true : false;
	}
	
	boolean isSubjectToGemination () {
		// laryngeals and semivowels are never geminated
		return !(this.isLaryngeal() || this.isSemivowel());
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(109, 113).append(character).toHashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
       if (!(obj instanceof Letter))
            return false;
        if (obj == this)
            return true;

        Letter rhs = (Letter) obj;
        return new EqualsBuilder()
            .append(character, rhs.character).isEquals();
    }
}
