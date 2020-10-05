
public enum VerbType {
	A, B, C, D, UNKNOWN;
	
	public static VerbType parseVerbType (String s) throws ConfigParseException {
		switch (s) {
			case "A": return VerbType.A;
			case "B": return VerbType.B;
			case "C": return VerbType.C;
			case "D": return VerbType.D;
			default:
				String message = String.format("Couldn't parse verb type: %s", s);
				throw new ConfigParseException(message);
		}
	}
}
