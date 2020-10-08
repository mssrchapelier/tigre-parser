package com.mssrchapelier.TigreParser;

import java.io.IOException;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.validators.PositiveInteger;
import com.mssrchapelier.TigreParser.components.utils.misc.ConfigParseException;

public class TigreParserCliLauncher {
	
	@Parameter(description = "input file path", required = true)
	String inputFilePath;
	
	@Parameter(names = { "-o", "--output" }, description = "output file path")
	String outputFilePath = String.format("output_%s.txt", inputFilePath);
	
	@Parameter(names = { "-n", "--numanalyses" }, description = "maximum number of analyses to show (non-negative integer; 0 to show all analyses - default)", validateWith = PositiveInteger.class)
	int maxAnalysesToShow = 0;

	TigreParser tigreParser;
	
	public static void main(String[] args) {
		
		TigreParserCliLauncher launcher = new TigreParserCliLauncher();
		
		JCommander.newBuilder()
			.addObject(launcher)
			.build()
			.parse(args);
		if (launcher.outputFilePath == null) {
			launcher.outputFilePath = String.format("output_%s.txt",
								launcher.inputFilePath);
		}

		launcher.run();
		
	}
	
	public void run () {
		try {
			this.tigreParser = new TigreParser(this.maxAnalysesToShow); 
			tigreParser.processFile(this.inputFilePath, this.outputFilePath, true);
		} catch (IOException | ConfigParseException e) {
			e.printStackTrace();
		}
	}
}
