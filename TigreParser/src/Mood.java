
public enum Mood {
	INDICATIVE, JUSSIVE, IMPERATIVE, UNKNOWN;
	
	public static Mood parseMood (String s) throws IllegalArgumentException {
		switch (s) {
			case "INDIC": return Mood.INDICATIVE;
			case "JUSS": return Mood.JUSSIVE;
			case "IMP": return Mood.IMPERATIVE;
			default: throw new IllegalArgumentException("Couldn't parse mood.");
		}
	}
}
