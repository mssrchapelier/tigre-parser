package com.mssrchapelier.TigreParser.components.VerbProcessor;

import java.io.IOException;

// Processes finite verb forms (passed as Strings).

import java.util.ArrayList;

import com.mssrchapelier.TigreParser.components.VerbProcessor.Paradigm.Conjugator;
import com.mssrchapelier.TigreParser.components.VerbProcessor.Stem.Root;
import com.mssrchapelier.TigreParser.components.VerbProcessor.Stem.RootListGenerator;
import com.mssrchapelier.TigreParser.components.VerbProcessor.Stem.Stem;
import com.mssrchapelier.TigreParser.components.utils.misc.ConfigParseException;
import com.mssrchapelier.TigreParser.components.utils.word.WordAnalysis;

public class VerbProcessor {
	private Conjugator conjugator;
	private RootListGenerator rootListGenerator;
	
	public VerbProcessor (String paradigmFilePath) throws IOException, ConfigParseException {
		this.conjugator = new Conjugator (paradigmFilePath);
		this.rootListGenerator = new RootListGenerator();
	} 

	public ArrayList<WordAnalysis> processWord (String word) {
		ArrayList<WordAnalysis> analysisList = new ArrayList<>();
		
		ArrayList<Root> roots = this.rootListGenerator.getRoots(word);

		for (Root root : roots) {
			ArrayList<Stem> derivedStems = Stem.generateWithPossiblePrefixes(root);
 			for (Stem stem : derivedStems) {
				ArrayList<WordAnalysis> allFormsWithRoot = this.conjugator.conjugate(stem);
				for (WordAnalysis form : allFormsWithRoot) {
					if (form.getRawWord().equals(word)) {
						analysisList.add(form);
					}
				}
			}
		}
		
		return analysisList;
	}
}
