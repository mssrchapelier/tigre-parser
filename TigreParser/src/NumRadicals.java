enum NumRadicals {
	RAD3 (3),
	RAD4 (4),
	RAD5 (5);

	private final int numValue;

	NumRadicals (int numValue) { this.numValue = numValue; }

	static NumRadicals parseNumRadicals (String numRadicals) throws IllegalArgumentException {
		switch (numRadicals) {
			case "3": return NumRadicals.RAD3;
			case "4": return NumRadicals.RAD4;
			case "5": return NumRadicals.RAD5;
			default: throw new IllegalArgumentException("Couldn't parse number of radicals");
		}
	}

	@Override
	public String toString () {
		return Integer.toString(this.numValue);
	}

	
}
