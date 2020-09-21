import java.util.ArrayList;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class VerbParadigmCell {
	
	int acceptedNumRadicals;
	VerbType acceptedVerbType;
	VerbPreformative acceptedDerivPrefix;
	
	ArrayList<MorphemeDescriptionPair> prefixes;
	MorphemeDescriptionPair vowelPattern;
	int[] geminationPattern;
	ArrayList<MorphemeDescriptionPair> suffixes;
	VerbGrammemeSet grammemeSet;
	
	public VerbParadigmCell () {
		this.acceptedNumRadicals = 0;
		this.acceptedVerbType = VerbType.UNKNOWN;
		this.acceptedDerivPrefix = VerbPreformative.UNKNOWN;
		this.prefixes = new ArrayList<>();
		this.vowelPattern = new MorphemeDescriptionPair("", "");
		this.geminationPattern = new int[0];
		this.suffixes = new ArrayList<>();
		this.grammemeSet = new VerbGrammemeSet();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(109, 113)
				.append(acceptedNumRadicals)
				.append(acceptedVerbType)
				.append(acceptedDerivPrefix)	
				.append(prefixes)
				.append(vowelPattern)
				.append(geminationPattern)
				.append(suffixes)
				.append(grammemeSet)
				.toHashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
       if (!(obj instanceof VerbParadigmCell))
            return false;
        if (obj == this)
            return true;

        VerbParadigmCell rhs = (VerbParadigmCell) obj;
        return new EqualsBuilder()
        		.append(acceptedNumRadicals, rhs.acceptedNumRadicals)
        		.append(acceptedVerbType, rhs.acceptedVerbType)
        		.append(acceptedDerivPrefix, rhs.acceptedDerivPrefix)	
        		.append(prefixes, rhs.prefixes)
        		.append(vowelPattern, rhs.vowelPattern)
        		.append(geminationPattern, rhs.geminationPattern)
        		.append(suffixes, rhs.suffixes)
        		.append(grammemeSet, rhs.grammemeSet)
        		.isEquals();
    }
	
}
