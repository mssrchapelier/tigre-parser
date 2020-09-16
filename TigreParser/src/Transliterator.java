import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.math3.util.CombinatoricsUtils;

public class Transliterator {
	// ə in values of romanizationMap stands for disambiguation of cases like [kə][ka] from [kka] (geminated).
	// The actual [ə] sound may or may not occur in that position; this is determined by phonotactics.
	// The ə symbol MUST be removed from any fields of GeezAnalysisPair objects immediately after generating geminated variants.
	HashMap<Character, String> romanizationMap;
	
	static String punctuationMarks = "[፠፡።፣፤፥፦፧፨\\.!,\\-\\:;\"'/\\\\\\|‒–—―‘’“”\\(\\)\\[\\]<>\\{\\}]";
	
	public Transliterator (String romanizationMapFilePath) throws IOException {
		this.romanizationMap = new HashMap<>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(romanizationMapFilePath), "UTF-8"));
		String currentLine;
		Pattern pPair = Pattern.compile("^(?<geez>.)\\t(?<romanized>.+)$");
		Matcher mPair;
		while ((currentLine = reader.readLine()) != null) {
			mPair = pPair.matcher(currentLine);
			if (mPair.find() && mPair.groupCount() == 2) {
				this.romanizationMap.put(mPair.group("geez").charAt(0), mPair.group("romanized"));
			}
		}
		reader.close();
	}
	
	public void romanizeFileReadWrite (String inputPath, String outputPath, boolean printGeez) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(inputPath));
		PrintWriter writer = new PrintWriter(outputPath);
		
		String inputLine;
		String outputLine;
		while ((inputLine = reader.readLine()) != null) {
			outputLine = this.romanizeLine(inputLine);
			if (printGeez) { writer.println(inputLine); }
			writer.println(outputLine);
			if (printGeez) { writer.println(); }
		}
		reader.close();
		writer.close();
	}
	
	public String romanizeLine (String geezLine) {
		String romanizedLine = "";
		char curChar;
		for (int i = 0; i < geezLine.length(); i++) {
			curChar = geezLine.charAt(i);
			if (this.romanizationMap.containsKey(curChar)) {
				romanizedLine += this.romanizationMap.get(curChar);
			} else {
				romanizedLine += curChar;
			}
		}
		return romanizedLine;
	}
	
	static ArrayList<String> generateGeminatedVariants (String ungemWordWithSchwas) {
		// ungemLine is a _non-geminated_ romanized version of a word written in Ge'ez script.
		// does NOT check whether ungemLine was generated from a genuine Ge'ez word
		// assumes conformity to pattern: (CV)+; V may include schwa
		ArrayList<String> orthoVariants = new ArrayList<>();
		ArrayList<Integer> geminablePositions = new ArrayList<>();
		
		orthoVariants.add(ungemWordWithSchwas.replaceAll("ə", ""));
		
		// Determine positions of geminable consonants.
		// the first letter is always a consonant; the first consonant can never be geminated
		// the second letter is a vowel or a schwa in ungemWordWithSchwas
		for (int i = 2; i < ungemWordWithSchwas.length(); i++) {
			char curChar = ungemWordWithSchwas.charAt(i);
			if (LetterType.isConsonant(curChar)
					&& !(LetterType.isLaryngeal(curChar) || LetterType.isSemivowel(curChar))) {
				geminablePositions.add(i);
			}
		}
		
		// Generate all possible ortho variants with respect to gemination
		for (int k = 1; k <= geminablePositions.size(); k++) {
			Iterator<int[]> combinationsIterator = CombinatoricsUtils.combinationsIterator(geminablePositions.size(), k);
			while (combinationsIterator.hasNext()) {
				// e. g. {2, 4}
				int[] combination = combinationsIterator.next();
				ArrayList<Integer> curGeminatedPositions = new ArrayList<>();
				// e. g. combination == {2, 4} and geminablePositions == {2, 3, 5, 7, 8} => curGeminatedPositions == {5, 8}
				for (int i = 0; i < combination.length; i++) {
					curGeminatedPositions.add(geminablePositions.get(combination[i]));
				}
				orthoVariants.add(generateGeminatedOrtho(ungemWordWithSchwas, curGeminatedPositions));
			}
		}
		return orthoVariants;
	}

	private static String generateGeminatedOrtho (String ungeminatedWordWithSchwas, ArrayList<Integer> geminatedPositions) {
		String geminatedOrtho = "";
		Iterator<Integer> gemPosIterator = geminatedPositions.iterator();
		int curPosToGeminate = gemPosIterator.next();
		for (int i = 0; i < ungeminatedWordWithSchwas.length(); i++) {
			char curChar = ungeminatedWordWithSchwas.charAt(i);
			if (!LetterType.isSchwa(curChar)) {
				geminatedOrtho += curChar;
			}
			if (i == curPosToGeminate) {
				if (!LetterType.isSchwa(curChar)) {
					geminatedOrtho += curChar;
				}
				if (gemPosIterator.hasNext()) {
					curPosToGeminate = gemPosIterator.next();
				}
			}
		}
		return geminatedOrtho;	
	}
	
	// takes a line of text in Ge'ez as input
	public ArrayList<GeezAnalysisPair> buildFromLine (String inputLine) {
		ArrayList<GeezAnalysisPair> list = new ArrayList<>();
		String[] tokens = inputLine.split("[ \\-]");
		for (String token : tokens) {
			token = token.replaceAll(punctuationMarks, "");
			if (!token.isEmpty() && isInGeez(token)) {
				GeezAnalysisPair curGeezAnalysisPair = new GeezAnalysisPair();
				curGeezAnalysisPair.geezWord = token;
				String ungemOrthoWithSchwas = this.romanizeLine(token);
				curGeezAnalysisPair.geminatedOrthos = generateGeminatedVariants(ungemOrthoWithSchwas);
				
				// remove schwa and assign to ungeminatedOrtho. Schwas already not present in geminatedOrthos.
				curGeezAnalysisPair.ungeminatedOrtho = ungemOrthoWithSchwas.replaceAll("ə", "");
				list.add(curGeezAnalysisPair);
			}
		}
		return list;
	}
	
	static boolean isInGeez (String line) {
		for (int i = 0; i < line.length(); i++) {
			if (!Character.UnicodeBlock.of(line.charAt(i)).equals(Character.UnicodeBlock.ETHIOPIC)) {
				return false;
			}
		}
		return true;
	}
}
