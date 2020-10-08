package com.mssrchapelier.TigreParser.components.VerbProcessor.Paradigm.VerbGrammemeSet;

import com.mssrchapelier.TigreParser.components.utils.misc.ConfigParseException;

enum Person {
	P1, P2, P3, UNKNOWN;
	
	static Person parsePerson (String s) throws ConfigParseException {
		switch (s) {
			case "1": return Person.P1;
			case "2": return Person.P2;
			case "3": return Person.P3;
			default:
				String message = String.format("Couldn't parse person: %s", s);
				throw new ConfigParseException(message);
		}
	}
}
