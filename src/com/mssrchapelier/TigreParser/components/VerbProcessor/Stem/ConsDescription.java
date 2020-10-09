package com.mssrchapelier.TigreParser.components.VerbProcessor.Stem;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.mssrchapelier.TigreParser.components.utils.misc.LetterType;

class ConsDescription {
	// tArm -> {t, true}, {r, false}, {m, false}
	
	char consonant;
	boolean isGeminated;
	boolean isFollowedByLongA;
	
	ConsDescription (char letter, boolean isGeminated, boolean isFollowedByLongA) throws IllegalArgumentException {
		if (!LetterType.isConsonant(letter)) { throw new IllegalArgumentException("Illegal parameter: char consonant must be a consonant."); }
		this.consonant = letter;
		this.isGeminated = isGeminated;
		this.isFollowedByLongA = isFollowedByLongA;
	}

	ConsDescription (ConsDescription cd) {
		this.consonant = cd.consonant;
		this.isGeminated = cd.isGeminated;
		this.isFollowedByLongA = cd.isFollowedByLongA;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(29, 241).append(consonant).append(isGeminated).append(isFollowedByLongA).toHashCode();
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
            append(isFollowedByLongA, rhs.isFollowedByLongA).
            isEquals();
    }
}
