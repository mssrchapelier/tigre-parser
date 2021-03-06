package com.mssrchapelier.TigreParser.components.VerbProcessor.Paradigm.GrammemeSet;

import com.mssrchapelier.TigreParser.components.utils.misc.ConfigParseException;

enum Mood {
	INDICATIVE, JUSSIVE, IMPERATIVE, UNKNOWN;
	
	static Mood parseMood (String s) throws ConfigParseException {
		switch (s) {
			case "INDIC": return Mood.INDICATIVE;
			case "JUSS": return Mood.JUSSIVE;
			case "IMP": return Mood.IMPERATIVE;
			default:
				String message = String.format("Couldn't parse mood: %s", s);
				throw new ConfigParseException(message);
		}
	}
}
