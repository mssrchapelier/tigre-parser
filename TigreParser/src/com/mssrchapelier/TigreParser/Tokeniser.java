package com.mssrchapelier.TigreParser;

import java.util.ArrayList;

class Tokeniser {
	private static final String punctuationMarksRegex = "[፠፡።፣፤፥፦፧፨\\.!,\\-\\:;\"'/\\\\\\|‒–—―‘’“”\\(\\)\\[\\]<>\\{\\}]";

	Tokeniser () {}

	ArrayList<String> tokenise (String ethiopicLine) {
		ArrayList<String> tokenList = new ArrayList<>();
		String[] tokens = ethiopicLine.split("[ \\-]");
		for (String token : tokens) {
			token = token.replaceAll(punctuationMarksRegex, "");
			if (!token.isEmpty()) { tokenList.add(token); }
		}
		return tokenList;
	}
}
