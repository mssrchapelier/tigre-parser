
public enum Gender {
	FEMININE, MASCULINE, COMMON, UNKNOWN;
	
	public static Gender parseGender (String s) throws ConfigParseException {
		switch (s) {
			case "M": return Gender.MASCULINE;
			case "F": return Gender.FEMININE;
			case "C": return Gender.COMMON;
			default:
				String message = String.format("Couldn't parse gender: %s", s);
				throw new ConfigParseException(message);
		}
	}
}
