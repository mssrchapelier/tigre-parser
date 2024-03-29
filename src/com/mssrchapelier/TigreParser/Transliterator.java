package com.mssrchapelier.TigreParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mssrchapelier.TigreParser.components.utils.misc.ConfigParseException;

class Transliterator {
	final private static Pattern mapEntryPattern = Pattern.compile("^(?<geez>.)\\t(?<romanised>.*)$");
	
	HashMap<Character, String> romanisationMap;
	
	Transliterator (InputStream inputStream) throws IOException, ConfigParseException {
		this.romanisationMap = readMap(inputStream);
	}

	private static HashMap<Character, String> readMap (InputStream inputStream) throws IOException, ConfigParseException {
		try (
				InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
				LineNumberReader reader = new LineNumberReader(inputStreamReader)
			) {
			HashMap<Character, String> romanisationMap = new HashMap<>();
			String line;
			while ((line = reader.readLine()) != null) {
				Matcher matcher = mapEntryPattern.matcher(line);
				matcher.find();
				try {
					if (matcher.groupCount() != 2) {
						String message = String.format("Failed to match the entry pattern: %s", line);
						throw new ConfigParseException(message);
					}
					
					String ethiopicPart = matcher.group("geez");
					String romanisedPart = matcher.group("romanised");
					if ( !isEthiopic(ethiopicPart) ) {
						String message = String.format("Not in charset Ethiopic: %s", ethiopicPart);
						throw new ConfigParseException(message);
					}
					romanisationMap.put(ethiopicPart.charAt(0), romanisedPart);
				} catch (ConfigParseException cause) {
					String description = String.format("Failed to read line in romanisation map: %s", line);
					throw new ConfigParseException.ConfigParseExceptionBuilder().appendDescription(description)
													.appendLineNumber(reader.getLineNumber())
													.build();
				}
			}
			return romanisationMap;
		}
	}

	String romanise (String inputString) throws NotEthiopicScriptException {

		// Returns the romanised representation of an Ethiopic word which assumes conformity to pattern: (CV)+, where V includes schwa.
		// NB: All consonants in this representation are ungeminated. Gemination is intended to be performed by Geminator.geminate(romanisedWord).
		// NB: Schwa in this representation is a technical symbol representing the order of the Ethiopic syllabic grapheme rather than the presence of the actual schwa sound. The schwa symbol is needed by Geminator.geminate(romanisedWord) for proper gemination (so that cases like ክኪ /romanised: [kəki]/ and ኪ /romanised: [ki]/ can be disambiguated - both sequences may be realised phonetically as [kki], but the former grapheme sequence cannot represent morphologically or lexically determined gemination, while the latter can). 

		if (!isEthiopic(inputString)) {
			throw new NotEthiopicScriptException("The word is not in Ethiopic script");
		}

		String romanisedString = "";
		for (int i = 0; i < inputString.length(); i++) {
			char ethiopicChar = inputString.charAt(i);
			if (this.romanisationMap.containsKey(ethiopicChar)) {
				romanisedString += this.romanisationMap.get(ethiopicChar);
			} else {
				romanisedString += ethiopicChar;
			}
		}
		return romanisedString;
	}

	private static boolean isEthiopic (String inputString) {
		for (int i = 0; i < inputString.length(); i++) {
			if (!Character.UnicodeBlock.of(inputString.charAt(i))
				.equals(Character.UnicodeBlock.ETHIOPIC)) { return false; }
		}
		return true;
	}
}
