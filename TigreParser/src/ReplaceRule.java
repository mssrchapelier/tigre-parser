import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ReplaceRule {
	final String regex;
	final String replacement;
	
	public ReplaceRule (String regex, String replacement) throws ConfigParseException {
		if (!isValidPattern(regex)) {
			throw new ConfigParseException(String.format("Not a valid pattern: %s", regex));
		}

		if (!isValidReplacement(replacement)) {
			throw new ConfigParseException(String.format("Not a valid replacement: %s", replacement));
		}
		this.regex = regex;
		this.replacement = replacement;
	}

	private static boolean isValidPattern (String regex) {
		if (regex.isEmpty()) { return false; }
		try {
			Pattern.compile(regex);
			return true;
		} catch (PatternSyntaxException e) { return false; }
	}

	private static boolean isValidReplacement (String replacement) {
		if (replacement.isEmpty()) { return false; }	
		// remove escape slashes
		String[] segments = replacement.split("-");
		for (int i = 0; i < segments.length; i++) {
			String segment = segments[i];
			String[] segmentParts = segment.split(":");
			if (segmentParts.length != 2) { return false; }
			String surface = segmentParts[0];
			String gloss = segmentParts[1];
			if (surface.length() == 0 || gloss.length() == 0) { return false; }
			if (surface.charAt(0) == '['
				&& surface.charAt(surface.length() - 1) == ']') {
				// surface is enclosed in square brackets => unanalysed segment
				if (gloss != "#") {
					// => gloss must be equal to #
					return false;
				}
			} else {
				// surface is not enclosed in square brackets => morpheme analysis
				if (gloss == "#") {
					// => gloss must not be equal to #
					return false;
				}
			}
		}
		return true;
	}
}
