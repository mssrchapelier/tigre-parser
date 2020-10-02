import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WordGlossPairComparator implements Comparator<WordGlossPair> {
	
	final static Pattern morphemeSeparatorPattern = Pattern.compile("\\-");

	@Override
	public int compare (WordGlossPair wgp_1, WordGlossPair wgp_2) {
		// final_analysis > non-final_analysis
		if (wgp_1.isFinalAnalysis && !wgp_2.isFinalAnalysis) { return 1; }
		else if (!wgp_1.isFinalAnalysis && wgp_2.isFinalAnalysis) { return -1; }
		else {
			// both final or both non-final
			int morphemeCount_1;
			int morphemeCount_2;
			Matcher m = morphemeSeparatorPattern.matcher(wgp_1.gloss);
			morphemeCount_1 = (m.find()) ? m.groupCount() + 1 : 1;
			m = morphemeSeparatorPattern.matcher(wgp_2.gloss);
			morphemeCount_2 = (m.find()) ? m.groupCount() + 1 : 1;
			
			if (wgp_1.isFinalAnalysis && wgp_2.isFinalAnalysis) {
				// both final: fewer_morphemes > more_morphemes
				if (morphemeCount_1 < morphemeCount_2) { return 1; }
				else if (morphemeCount_1 == morphemeCount_2) { return 0; }
				else { return -1; }
			} else {
				// both non-final: fewer_morphemes < more_morphemes
				if (morphemeCount_1 < morphemeCount_2) { return -1; }
				else if (morphemeCount_1 == morphemeCount_2) { return 0; }
				else { return 1; }
			}
		}
	}
}
