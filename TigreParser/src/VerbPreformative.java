
public enum VerbPreformative {
	T, A, AT, ATTA, NO_PREFORMATIVE, UNKNOWN;
	
	public static VerbPreformative parseVerbPreformative (String s) throws ConfigParseException {
		switch (s) {
			case "T": return VerbPreformative.T;
			case "A": return VerbPreformative.A;
			case "AT": return VerbPreformative.AT;
			case "ATTA": return VerbPreformative.ATTA;
			case "0": return VerbPreformative.NO_PREFORMATIVE;
			default:
				String message = String.format("Couldn't parse derivational prefix: %s", s);
				throw new ConfigParseException(message);
		}
	}
}
