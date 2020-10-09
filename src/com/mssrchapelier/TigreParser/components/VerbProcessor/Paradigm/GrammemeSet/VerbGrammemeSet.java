package com.mssrchapelier.TigreParser.components.VerbProcessor.Paradigm.GrammemeSet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.mssrchapelier.TigreParser.components.utils.misc.ConfigParseException;

public class VerbGrammemeSet {
	private static final Pattern descriptionPattern = Pattern.compile(
									"(?<mood>INDIC|JUSS|IMP)" +
									"\\+(?<tense>IMPF|PRF|NA)" +
									"\\+(?<person>[123])" +
									"\\+(?<gender>[MFC])" +
									"\\+(?<number>SG|PL)");
	private final Mood mood;
	private final Tense tense;
	private final Person person;
	private final Gender gender;
	private final Number number;
	
	private VerbGrammemeSet(Mood mood, Tense tense, Person person, Gender gender, Number number) throws IllegalArgumentException {
		if (person == Person.UNKNOWN ||
			gender == Gender.UNKNOWN ||
			number == Number.UNKNOWN ||
			(mood == Mood.INDICATIVE && tense == Tense.UNKNOWN) ||
			((mood == Mood.JUSSIVE || mood == Mood.IMPERATIVE) && tense != Tense.UNKNOWN) ||
			(mood == Mood.IMPERATIVE && person != Person.P2) ||
			(person == Person.P1 && gender != Gender.COMMON)) {
			throw new IllegalArgumentException ("Invalid grammeme set passed as argument.");
		}
		
		this.mood = mood;
		this.tense = tense;
		this.person = person;
		this.gender = gender;
		this.number = number;
	}

	public static VerbGrammemeSet parse (String inputString) throws ConfigParseException {
		try {
			Matcher matcher = descriptionPattern.matcher(inputString);
			matcher.find();

			Mood mood = Mood.parseMood(matcher.group("mood"));
			Tense tense = Tense.parseTense(matcher.group("tense"));
			Person person = Person.parsePerson(matcher.group("person"));
			Gender gender = Gender.parseGender(matcher.group("gender"));
			Number number = Number.parseNumber(matcher.group("number"));

			return new VerbGrammemeSet(mood, tense, person, gender, number);
		} catch (IllegalStateException | ConfigParseException e) {
			String message = String.format("Failed to parse grammeme set: %s", inputString);
			throw new ConfigParseException(message, e);
		}
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(109, 113)
				.append(mood)	
				.append(tense)
				.append(person)
				.append(gender)
				.append(number)
				.toHashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof VerbGrammemeSet)) { return false; }
		if (obj == this) { return true; }

		VerbGrammemeSet rhs = (VerbGrammemeSet) obj;
		return new EqualsBuilder()
				.append(mood, rhs.mood)
				.append(tense, rhs.tense)
				.append(person, rhs.person)
				.append(gender, rhs.gender)
				.append(number, rhs.number)
				.isEquals();
	}
}
