import java.util.regex.Pattern;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.regex.Matcher;

public class Transliterator {
	final private static Pattern entryPattern = Pattern.compile("^(?<geez>.)\\t(?<romanised>.+)$");
	final private static String punctuationMarksRegex = "[፠፡።፣፤፥፦፧፨\\.!,\\-\\:;\"'/\\\\\\|‒–—―‘’“”\\(\\)\\[\\]<>\\{\\}]";

	HashMap<Character, String> romanisationMap;
	
	public Transliterator (String romanisationMapFilePath) throws IOException, ConfigParseException {
		this.romanisationMap = readMap(romanisationMapFilePath);
	}

	private static HashMap<Character, String> readMap (String romanisationMapFilePath) throws IOException, ConfigParseException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(romanisationMapFilePath), "UTF-8"))) {
			HashMap<Character, String> romanisationMap = new HashMap<>();
			String currentLine;
			int curLineNumber = 0;
			while ((currentLine = reader.readLine()) != null) {
				curLineNumber++;
				Matcher matcher = entryPattern.matcher(currentLine);
				if (matcher.find() && matcher.groupCount() == 2) {
					romanisationMap.put(matcher.group("geez").charAt(0), matcher.group("romanised"));
				} else { throw new ConfigParseException(String.format("Failed to read romanisation map file: error at line %d", curLineNumber)); }
			}
			return romanisationMap;
		}
	}

	public String romanise (String ethiopicString) throws NotEthiopicScriptException {

		// Returns the romanised representation of an Ethiopic word which assumes conformity to pattern: (CV)+, where V includes schwa.
		// NB: All consonants in this representation are ungeminated. Gemination is intended to be performed by Geminator.geminate(romanisedWord).
		// NB: Schwa in this representation is a technical symbol representing the order of the Ethiopic syllabic grapheme rather than the presence of the actual schwa sound. The schwa symbol is needed by Geminator.geminate(romanisedWord) for proper gemination (so that cases like ክኪ /romanised: [kəki]/ and ኪ /romanised: [ki]/ can be disambiguated - both sequences may be realised phonetically as [kki], but the former grapheme sequence cannot represent morphologically or lexically determined gemination, while the latter can). 

		if (!isEthiopic(ethiopicString)) {
			String message = String.format("The string is not in Ethiopic script: %s", ethiopicString); 
			throw new NotEthiopicScriptException(message);
		}

		String romanisedString = "";
		for (int i = 0; i < ethiopicString.length(); i++) {
			char ethiopicChar = ethiopicString.charAt(i);
			if (this.romanisationMap.containsKey(ethiopicChar)) {
				romanisedString += this.romanisationMap.get(ethiopicChar);
			} else {
				romanisedString += ethiopicChar;
			}
		}
		return romanisedString;
	}

	private static boolean isEthiopic (String word) {
		for (int i = 0; i < word.length(); i++) {
			if (!Character.UnicodeBlock.of(word.charAt(i))
				.equals(Character.UnicodeBlock.ETHIOPIC)) { return false; }
		}
		return true;
	}
}
