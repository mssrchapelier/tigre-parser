import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VerbGrammemeSet {
	private static final Pattern descriptionPattern = Pattern.compile(
									"(?<mood>INDIC|JUSS|IMP)" +
									"\\+(?<tense>IMPF|PRF|NA)" +
									"\\+(?<person>[123])" +
									"\\+(?<gender>[MFC])" +
									"\\+(?<number>SG|PL)");
	Mood mood;
	Tense tense;
	Person person;
	Gender gender;
	Number number;

	VerbGrammemeSet() {
		this.mood = Mood.UNKNOWN;
		this.tense = Tense.UNKNOWN;
		this.person = Person.UNKNOWN;
		this.gender = Gender.UNKNOWN;
		this.number = Number.UNKNOWN;
	}
	
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

	static VerbGrammemeSet parse (String inputString) {
		Matcher matcher = descriptionPattern.matcher(inputString);
		matcher.find();

		Mood mood = Mood.parseMood(matcher.group("mood"));
		Tense tense = Tense.parseTense(matcher.group("tense"));
		Person person = Person.parsePerson(matcher.group("person"));
		Gender gender = Gender.parseGender(matcher.group("gender"));
		Number number = Number.parseNumber(matcher.group("number"));

		return new VerbGrammemeSet(mood, tense, person, gender, number);
	}
	
	
}
