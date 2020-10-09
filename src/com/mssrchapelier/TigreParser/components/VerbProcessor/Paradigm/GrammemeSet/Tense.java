package com.mssrchapelier.TigreParser.components.VerbProcessor.Paradigm.GrammemeSet;

import com.mssrchapelier.TigreParser.components.utils.misc.ConfigParseException;

enum Tense {
	PERFECT, IMPERFECT, UNKNOWN;
	
	static Tense parseTense (String s) throws ConfigParseException {
		switch (s) {
			case "IMPF": return Tense.IMPERFECT;
			case "PRF": return Tense.PERFECT;
			case "NA": return Tense.UNKNOWN;
			default:
				String message = String.format("Couldn't parse tense: %s", s);
				throw new ConfigParseException(message);
		}
	}
}
