
public enum Tense {
	PERFECT, IMPERFECT, UNKNOWN;
	
	public static Tense parseTense (String s) throws ConfigParseException {
		switch (s) {
			case "IMPF": return Tense.IMPERFECT;
			case "PRF": return Tense.PERFECT;
			case "NA": return Tense.UNKNOWN;
			default:
				String message = String.format("Couldn't parse tense: %s", s);
				throw new ConfigParseException(message);
		}
	}
}
