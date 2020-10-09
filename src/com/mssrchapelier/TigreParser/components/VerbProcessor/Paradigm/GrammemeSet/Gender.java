package com.mssrchapelier.TigreParser.components.VerbProcessor.Paradigm.GrammemeSet;

import com.mssrchapelier.TigreParser.components.utils.misc.ConfigParseException;

enum Gender {
	FEMININE, MASCULINE, COMMON, UNKNOWN;
	
	static Gender parseGender (String s) throws ConfigParseException {
		switch (s) {
			case "M": return Gender.MASCULINE;
			case "F": return Gender.FEMININE;
			case "C": return Gender.COMMON;
			default:
				String message = String.format("Couldn't parse gender: %s", s);
				throw new ConfigParseException(message);
		}
	}
}
