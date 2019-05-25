
public enum Number {
	SINGULAR, PLURAL, UNKNOWN;
	
	public static Number parseNumber (String s) throws IllegalArgumentException {
		switch (s) {
			case "SG": return Number.SINGULAR;
			case "PL": return Number.PLURAL;
			default: throw new IllegalArgumentException("Couldn't parse number.");
		}
	}
}
