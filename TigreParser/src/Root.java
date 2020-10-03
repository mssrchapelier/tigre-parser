import java.util.Arrays;
import java.util.ArrayList;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Root {
	final char[] consonants;
	final VerbType verbType;
	final String dictionaryGloss;
	
	Root (Root root) {
		this.consonants = Arrays.copyOf(root.consonants, root.consonants.length);
		this.verbType = root.verbType;
		this.dictionaryGloss = root.dictionaryGloss;
	}
	
	Root (ArrayList<ConsDescription> template) {
		int numConsonants = template.size();
		if (numConsonants < 3 || numConsonants > 5) {
			throw new IllegalArgumentException("template not a valid root: length must be between 3 and 5 consonants");
		}
		
		int longACount = 0;
		for (ConsDescription cd : template) {
			if (cd.isFollowedByLongA) { longACount++; }
			if (longACount > 1) { throw new IllegalArgumentException("template not a valid root: cannot contain more than two consonants followed by [a:] (long A)"); }
		}

		char[] consonants = new char[numConsonants];
		for (int i = 0; i < numConsonants; i++) {
			consonants[i] = template.get(i).consonant;
		}

		this.consonants = consonants;
		this.verbType = determineVerbType(template);
		this.dictionaryGloss = constructGloss(template);
	}
	
	private static VerbType determineVerbType (ArrayList<ConsDescription> template) {
		if (template.size() == 3 && template.get(1).isGeminated) {
			return VerbType.B;
		} else if (template.size() == 4
				&& template.get(1).isFollowedByLongA
				&& template.get(1).consonant == template.get(2).consonant) {
			return VerbType.D;
		} else if ((template.size() == 3 && template.get(0).isFollowedByLongA)
				|| (template.size() == 4 && template.get(1).isFollowedByLongA)
				|| (template.size() == 5 && template.get(2).isFollowedByLongA)) {
			return VerbType.C;
		} else {
			return VerbType.A;
		}
	}

	boolean isCompatibleWithPrefix (VerbPreformative prefix) {
		switch (prefix) {
			case NO_PREFORMATIVE:
				return true;
			case T:
				return true;
			case A:
				if (this.verbType == VerbType.D || LetterType.isLaryngeal(this.consonants[0])) { return false; }
				else { return true; }
			case ATTA:
				if (LetterType.isLaryngeal(this.consonants[0])) { return false; }
				else { return true;	}
			case AT:
				if (verbType == VerbType.A) {
					// Check if any of the consonants is a laryngeal. If yes, assigning type A is possible; otherwise it isn't.
					for (int i = 0; i < this.consonants.length; i++) {
						if (LetterType.isLaryngeal(this.consonants[i])) { return true; }
					}
					return false;
				} else { return true; }
			default: // case UNKNOWN
				return false;
		}
	}

	private static String constructGloss (ArrayList<ConsDescription> template) {
		String dictionaryGloss = "";
		for (ConsDescription radical : template) {
			String toAppend = "";
			toAppend += radical.consonant;
			if (radical.isGeminated) { toAppend += "(2)"; }
			if (radical.isFollowedByLongA) { toAppend += "(A)"; }
			dictionaryGloss += toAppend;
		}
		return dictionaryGloss;
	}
	
	@Override
	public String toString () {
		String output = "";
		output += String.format("%s, type: %s", this.dictionaryGloss, this.verbType.toString());
		return output;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(109, 113)
			.append(consonants)
			.append(verbType)
			.append(dictionaryGloss)
			.toHashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
       if (!(obj instanceof Root))
            return false;
        if (obj == this)
            return true;

        Root rhs = (Root) obj;
        return new EqualsBuilder()
		.append(consonants, rhs.consonants)
		.append(verbType, rhs.verbType)
		.append(dictionaryGloss, rhs.dictionaryGloss)
		.isEquals();
    }
}
