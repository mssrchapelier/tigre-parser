import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WordGlossPairComparator implements Comparator<WordGlossPair> {
	
	@Override
	public int compare (WordGlossPair wgp_1, WordGlossPair wgp_2) {
		if (wgp_1.isFinalAnalysis && !wgp_2.isFinalAnalysis) {
			return 1;
		} else if (!wgp_1.isFinalAnalysis && wgp_2.isFinalAnalysis) {
			return -1;
		} else {
			int numMorph_1;
			int numMorph_2;
			Pattern p = Pattern.compile("\\-");
			Matcher m = p.matcher(wgp_1.lexicalForm);
			numMorph_1 = (m.find()) ? m.groupCount() + 1 : 1;
			m = p.matcher(wgp_2.lexicalForm);
			numMorph_2 = (m.find()) ? m.groupCount() + 1 : 1;
			
			if (wgp_1.isFinalAnalysis && wgp_2.isFinalAnalysis) {
				// less morphemes => less than
				if (numMorph_1 < numMorph_2) { return 1; }
				else if (numMorph_1 == numMorph_2) { return 0; }
				else { return -1; }
			} else {
				// more morphemes => less than
				if (numMorph_1 < numMorph_2) { return -1; }
				else if (numMorph_1 == numMorph_2) { return 0; }
				else { return 1; }
			}
		}
	}
}
