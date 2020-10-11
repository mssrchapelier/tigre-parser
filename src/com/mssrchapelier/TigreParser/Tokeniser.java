package com.mssrchapelier.TigreParser;

import java.util.ArrayList;

class Tokeniser {
	private static final String punctuationMarksRegex = "[፠፡።፣፤፥፦፧፨\\.!,\\-\\:;\"'/\\\\\\|‒–—―‘’“”\\(\\)\\[\\]<>\\{\\}]";
	private static final String whitespaceRegex = "\\s+";
	private static final String delimiterRegex = punctuationMarksRegex + "|" + whitespaceRegex;
	
	Tokeniser () {}

	ArrayList<String> tokenise (String ethiopicLine) {
		ArrayList<String> tokenList = new ArrayList<>();
		String[] tokens = ethiopicLine.split("[ \\-]");
		for (String token : tokens) {
			token = token.replaceAll(delimiterRegex, "");
			if (!token.isEmpty()) { tokenList.add(token); }
		}
		return tokenList;
	}
}
