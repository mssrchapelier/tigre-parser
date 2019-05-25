
public enum VerbType {
	A, B, C, D, UNKNOWN;
	
	public static VerbType parseVerbType (String s) throws IllegalArgumentException {
		switch (s) {
			case "A": return VerbType.A;
			case "B": return VerbType.B;
			case "C": return VerbType.C;
			case "D": return VerbType.D;
			default: throw new IllegalArgumentException("Couldn't parse verb type.");
		}
	}
}
