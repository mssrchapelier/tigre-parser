package com.mssrchapelier.TigreParser.components.VerbProcessor.StemDescription;

import com.mssrchapelier.TigreParser.components.utils.misc.ConfigParseException;

public enum RootType {
	A, B, C, D, UNKNOWN;
	
	public static RootType parseRootType (String s) throws ConfigParseException {
		switch (s) {
			case "A": return RootType.A;
			case "B": return RootType.B;
			case "C": return RootType.C;
			case "D": return RootType.D;
			default:
				String message = String.format("Couldn't parse root type: %s", s);
				throw new ConfigParseException(message);
		}
	}
}
