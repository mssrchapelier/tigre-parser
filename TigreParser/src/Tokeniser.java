import java.util.ArrayList;

public class Tokeniser {
	private static final String punctuationMarksRegex = "[፠፡።፣፤፥፦፧፨\\.!,\\-\\:;\"'/\\\\\\|‒–—―‘’“”\\(\\)\\[\\]<>\\{\\}]";

	public Tokeniser () {}

	public ArrayList<String> tokenise (String ethiopicLine) {
		ArrayList<String> tokenList = new ArrayList<>();
		String[] tokens = ethiopicLine.split("[ \\-]");
		for (String token : tokens) {
			token = token.replaceAll(punctuationMarksRegex, "");
			if (!token.isEmpty()) { tokenList.add(token); }
		}
		return tokenList;
	}
}
