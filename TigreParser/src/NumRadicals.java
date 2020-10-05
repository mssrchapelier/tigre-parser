enum NumRadicals {
	RAD3 (3),
	RAD4 (4),
	RAD5 (5);

	private final int numValue;

	NumRadicals (int numValue) { this.numValue = numValue; }

	static NumRadicals parseNumRadicals (String numRadicalsAsString) throws ConfigParseException {
		return parseNumRadicals(Integer.parseInt(numRadicalsAsString));
	}

	static NumRadicals parseNumRadicals (int numRadicals) throws ConfigParseException {
		switch (numRadicals) {
			case 3: return NumRadicals.RAD3;
			case 4: return NumRadicals.RAD4;
			case 5: return NumRadicals.RAD5;
			default:
				String message = String.format("Couldn't parse number of radicals: %d", numRadicals);
				throw new ConfigParseException(message);
		}
	}

	@Override
	public String toString () {
		return Integer.toString(this.numValue);
	}

	
}
