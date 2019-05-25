
public enum Gender {
	FEMININE, MASCULINE, COMMON, UNKNOWN;
	
	public static Gender parseGender (String s) throws IllegalArgumentException {
		switch (s) {
			case "M": return Gender.MASCULINE;
			case "F": return Gender.FEMININE;
			case "C": return Gender.COMMON;
			default: throw new IllegalArgumentException("Couldn't parse gender.");
		}
	}
}
