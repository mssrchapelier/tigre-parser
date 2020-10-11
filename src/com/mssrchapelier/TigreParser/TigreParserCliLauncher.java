package com.mssrchapelier.TigreParser;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.validators.PositiveInteger;

/**
 * 
 * <p>The command-line interface for {@link com.mssrchapelier.TigreParser.TigreParser TigreParser}.</p>
 * 
 * <p>
 * The only public member of this class is the {@code main} method, whose {@code String} arguments are parsed by an instance
 * of {@link com.beust.jcommander.JCommander.Builder JCommander.Builder}. The accepted arguments are as follows:
 * <ul>
 * <li><em>(no flag)</em>: input file path <strong>(required)</strong>;</li>
 * <li>{@code -o}, {@code --output}: output file path (optional; is set to {@code output_}<em>input_file_path</em> if omitted);</li>
 * <li>{@code -c}, {@code --config}: {@code .json} configuration file path (optional; if omitted, the default value will be specified
 * by {@code TigreParser} and equals {@code /res/config.json});</li>
 * <li>{@code -n}, {@code --numanalyses}: the maximum number of analyses for each (word) token to print to the output file (optional;
 * must be a non-negative integer - checked by {@link com.beust.jcommander.validators.PositiveInteger jcommander.validators.PositiveInteger};
 * if omitted, all non-empty analyses will be printed, as specified by {@code TigreParser}).</li>
 * </ul>
 * </p>
 * 
 * <p>
 * The instance method {@code run}, called by {@code main}, accesses {@code TigreParser}'s its builder ({@link com.mssrchapelier.TigreParser.TigreParser.TigreParserBuilder
 * TigreParserBuilder}) through its {@code builder} method, passes the {@code TigreParserCliLauncher} instance's non-null arguments (the methods {@code setConfigFilePath} and {@code setMaxAnalyses} of {@code TigreParserBuilder}),
 * calls the builder's {@code build} method to construct a {@code TigreParser} object and calls the latter's {@code processFile} method, passing the input and output file names as the arguments. If an {@code IOException}
 * or a {@link com.mssrchapelier.TigreParser.components.utils.misc.ConfigParseException ConfigParseException} are thrown by {@code build} or {@code processFile},
 * a message indicating the failure to configure the parser or to process the file respectively is printed to standard output along with the exception's stack trace.
 * </p>
 *
 */

public class TigreParserCliLauncher {
	
	@Parameter(description = "input file path", required = true)
	private String inputFilePath;
	
	@Parameter(names = { "-o", "--output" }, description = "output file path")
	private String outputFilePath;
	
	@Parameter(names = { "-c", "--config" }, description = "configuration file path")
	private String configFilePath;
	
	@Parameter(names = { "-n", "--numanalyses" },
			description = "maximum number of analyses to show (non-negative integer; 0 to show all analyses - default)",
			validateWith = PositiveInteger.class)
	private int maxAnalyses;
	
	private TigreParserCliLauncher () {}
	
	public static void main(String[] args) {
		
		TigreParserCliLauncher launcher = new TigreParserCliLauncher();
		
		JCommander.newBuilder()
			.addObject(launcher)
			.build()
			.parse(args);
		if (launcher.outputFilePath == null) {
			File inputFile = new File(launcher.inputFilePath);
			launcher.outputFilePath = String.format("output_%s",
								inputFile.getName());
		}

		launcher.run();
		
	}
	
	private void run () {
		try {
			TigreParser.TigreParserBuilder builder = TigreParser.builder();
			if (this.configFilePath != null) { builder.setConfigFilePath(this.configFilePath); }
			if (this.maxAnalyses != 0) { builder.setMaxAnalyses(this.maxAnalyses); }
			TigreParser parser = builder.build();
			
			try {
				parser.processFile(this.inputFilePath, this.outputFilePath, true);
			} catch (Exception e) {
				System.out.println("Failed to process file");
				e.printStackTrace();
			}
		} catch (IOException | ParseException e) {
			System.out.println("Failed to configure the parser");
			e.printStackTrace();
		}
		
	}
}
