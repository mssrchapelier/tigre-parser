
public enum Person {
	P1, P2, P3, UNKNOWN;
	
	public static Person parsePerson (String s) throws IllegalArgumentException {
		switch (s) {
			case "1": return Person.P1;
			case "2": return Person.P2;
			case "3": return Person.P3;
			default: throw new IllegalArgumentException("Couldn't parse person.");
		}
	}
}
