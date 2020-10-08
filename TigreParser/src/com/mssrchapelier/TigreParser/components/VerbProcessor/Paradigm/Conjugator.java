package com.mssrchapelier.TigreParser.components.VerbProcessor.Paradigm;

import java.io.IOException;
import java.util.ArrayList;

import com.mssrchapelier.TigreParser.components.VerbProcessor.Stem.Stem;
import com.mssrchapelier.TigreParser.components.utils.misc.ConfigParseException;
import com.mssrchapelier.TigreParser.components.utils.word.WordAnalysis;

public class Conjugator {

	Paradigm paradigm;

	public Conjugator (String paradigmFilePath) throws IOException, ConfigParseException {
		this.paradigm = new Paradigm.ParadigmBuilder().readFrom(paradigmFilePath)
																.build();
	}

	public ArrayList<WordAnalysis> conjugate (Stem stem) {
		ArrayList<ParadigmCell> singleVerbParadigm = this.paradigm.getSingleParadigm(stem.stemDescription);
		ArrayList<WordAnalysis> formList = new ArrayList<>();
		for (ParadigmCell cell : singleVerbParadigm) {
			formList.add(cell.applyTo(stem));
		}
		return formList;
	}
}
