import java.util.ArrayList;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Root {
	ArrayList<ConsDescription> consTemplate;
	
	Root (ArrayList<ConsDescription> template) throws IllegalArgumentException {
		if (template.size() < 3 || template.size() > 5) {
			throw new IllegalArgumentException("template not a valid root: length must be between 3 and 5 consonants.");
		}
		
		int longACount = 0;
		for (ConsDescription cd : template) {
			if (cd.followedByLongA) { longACount++; }
			if (longACount > 1) { throw new IllegalArgumentException("template not a valid root: cannot contain more than two consonants followed by [a:] (long A)."); }
		}

		ArrayList<ConsDescription> templateCopy = new ArrayList<>();
		for (ConsDescription cd : template) { templateCopy.add(ConsDescription.newInstance(cd)); }
		this.consTemplate = templateCopy;
	}
	
	VerbType determineVerbType () {
		if (this.size() == 3 && this.consTemplate.get(1).isGeminated) {
			return VerbType.B;
		} else if (this.size() == 4
				&& this.consTemplate.get(1).followedByLongA
				&& this.consTemplate.get(1).consonant == this.consTemplate.get(2).consonant) {
			return VerbType.D;
		} else if ((this.size() == 3 && this.consTemplate.get(0).followedByLongA)
				|| (this.size() == 4 && this.consTemplate.get(1).followedByLongA)
				|| (this.size() == 5 && this.consTemplate.get(2).followedByLongA)) {
			return VerbType.C;
		} else {
			return VerbType.A;
		}
	}

	boolean combinesWithPrefix (VerbPreformative prefix) {
		VerbType verbType = this.determineVerbType();
		switch (prefix) {
			case NO_PREFORMATIVE:
			case T:
				return true;
			case A:
				if (verbType == VerbType.D || LetterType.isLaryngeal(this.consTemplate.get(0).consonant)) { return false; }
				else { return true; }
			case ATTA:
				if (LetterType.isLaryngeal(this.consTemplate.get(0).consonant)) { return false; }
				else { return true;	}
			case AT:
				if (verbType == VerbType.A) {
					// Check if any of the consonants is a laryngeal. If yes, assigning type A is possible; otherwise it isn't.
					for (ConsDescription cd : this.consTemplate) {
						if (LetterType.isLaryngeal(cd.consonant)) { return true; }
					}
						return false;
				} else { return true; }
			default: // case UNKNOWN
				return false;
		}
	}
	
	static Root newInstance (Root root) throws IllegalArgumentException {
		ArrayList<ConsDescription> newConsTemplate = new ArrayList<>();
		for (ConsDescription cd : root.consTemplate) {
			newConsTemplate.add(ConsDescription.newInstance(cd));
		}
		return new Root(newConsTemplate);
	}
	
	int size () {
		return this.consTemplate.size();
	}
	
	@Override
	public String toString () {
		String output = "";
		for (ConsDescription radical : this.consTemplate) {
			String toAppend = "";
			toAppend += radical.consonant;
			if (radical.isGeminated) { toAppend += "(2)"; }
			if (radical.followedByLongA) { toAppend += "(A)"; }
			output += toAppend;
		}
		return output;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(109, 113).append(consTemplate).toHashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
       if (!(obj instanceof Root))
            return false;
        if (obj == this)
            return true;

        Root rhs = (Root) obj;
        return new EqualsBuilder()
            .append(consTemplate, rhs.consTemplate)
            .isEquals();
    }
}
