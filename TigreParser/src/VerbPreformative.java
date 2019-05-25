
public enum VerbPreformative {
	T, A, AT, ATTA, NO_PREFORMATIVE, UNKNOWN;
	
	public static VerbPreformative parseVerbPreformative (String s) throws IllegalArgumentException {
		switch (s) {
			case "T": return VerbPreformative.T;
			case "A": return VerbPreformative.A;
			case "AT": return VerbPreformative.AT;
			case "ATTA": return VerbPreformative.ATTA;
			case "0": return VerbPreformative.NO_PREFORMATIVE;
			default: throw new IllegalArgumentException("Couldn't parse derivational prefix.");
		}
	}
}
