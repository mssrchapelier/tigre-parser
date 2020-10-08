package com.mssrchapelier.TigreParser.components.VerbProcessor.Stem;

import java.util.ArrayList;
import java.util.Arrays;

import com.mssrchapelier.TigreParser.components.VerbProcessor.StemDescription.DerivPrefix;
import com.mssrchapelier.TigreParser.components.VerbProcessor.StemDescription.NumRadicals;
import com.mssrchapelier.TigreParser.components.VerbProcessor.StemDescription.StemDescription;
import com.mssrchapelier.TigreParser.components.utils.misc.ConfigParseException;

public class Stem {
	
	public final StemDescription stemDescription;
	public final char[] rootConsonants;
	public final String rootDictionaryGloss;
	
	private Stem (Stem stem) {
		this.stemDescription = new StemDescription(stem.stemDescription);
		this.rootConsonants = Arrays.copyOf(stem.rootConsonants, stem.rootConsonants.length);
		this.rootDictionaryGloss = stem.rootDictionaryGloss;
	}
	
	private Stem (Root root, DerivPrefix derivationalPrefix) {
		this.rootDictionaryGloss = root.dictionaryGloss;
		this.rootConsonants = Arrays.copyOf(root.consonants, root.consonants.length);

		try {
			this.stemDescription = new StemDescription(NumRadicals.parseNumRadicals(this.rootConsonants.length),
									root.rootType,
									derivationalPrefix);
		} catch (ConfigParseException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static ArrayList<Stem> generateWithPossiblePrefixes (Root root) {
		ArrayList<Stem> stemList = new ArrayList<>();
		for (DerivPrefix prefix : DerivPrefix.values()) {
			if (root.isCompatibleWithPrefix(prefix)) { stemList.add(new Stem(root, prefix)); }
		}
		return stemList;
	}
}
