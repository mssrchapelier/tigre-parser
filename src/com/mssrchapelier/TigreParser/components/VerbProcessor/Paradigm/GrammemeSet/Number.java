package com.mssrchapelier.TigreParser.components.VerbProcessor.Paradigm.GrammemeSet;

import com.mssrchapelier.TigreParser.components.utils.misc.ConfigParseException;

enum Number {
	SINGULAR, PLURAL, UNKNOWN;
	
	static Number parseNumber (String s) throws ConfigParseException {
		switch (s) {
			case "SG": return Number.SINGULAR;
			case "PL": return Number.PLURAL;
			default:
				String message = String.format("Couldn't parse number: %s", s);
				throw new ConfigParseException(message);
		}
	}
}
