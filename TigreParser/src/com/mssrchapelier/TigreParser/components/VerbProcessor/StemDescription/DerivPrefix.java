package com.mssrchapelier.TigreParser.components.VerbProcessor.StemDescription;

import com.mssrchapelier.TigreParser.components.utils.misc.ConfigParseException;

public enum DerivPrefix {
	T, A, AT, ATTA, NO_PREFORMATIVE, UNKNOWN;
	
	public static DerivPrefix parseDerivPrefix (String s) throws ConfigParseException {
		switch (s) {
			case "T": return DerivPrefix.T;
			case "A": return DerivPrefix.A;
			case "AT": return DerivPrefix.AT;
			case "ATTA": return DerivPrefix.ATTA;
			case "0": return DerivPrefix.NO_PREFORMATIVE;
			default:
				String message = String.format("Couldn't parse derivational prefix: %s", s);
				throw new ConfigParseException(message);
		}
	}
}
