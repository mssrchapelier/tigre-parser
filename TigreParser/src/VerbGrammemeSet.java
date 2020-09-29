
public class VerbGrammemeSet {
	Mood mood;
	Tense tense;
	Person person;
	Gender gender;
	Number number;
	
	public VerbGrammemeSet () {
		this.mood = Mood.UNKNOWN;
		this.tense = Tense.UNKNOWN;
		this.person = Person.UNKNOWN;
		this.gender = Gender.UNKNOWN;
		this.number = Number.UNKNOWN;
	}

	public VerbGrammemeSet(Mood mood, Tense tense, Person person, Gender gender, Number number) throws IllegalArgumentException {
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
	
	
}
