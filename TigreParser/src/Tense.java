
public enum Tense {
	PERFECT, IMPERFECT, UNKNOWN;
	
	public static Tense parseTense (String s) throws IllegalArgumentException {
		switch (s) {
			case "IMPF": return Tense.IMPERFECT;
			case "PRF": return Tense.PERFECT;
			case "NA": return Tense.UNKNOWN;
			default: throw new IllegalArgumentException("Couldn't parse tense.");
		}
	}
}
