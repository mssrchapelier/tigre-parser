import java.io.IOException;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.validators.PositiveInteger;

public class StandaloneLauncher {
	
	@Parameter(description = "input file path", required = true)
	String inputFilePath;
	
	@Parameter(names = { "-o", "--output" }, description = "output file path")
	String outputFilePath = String.format("output_%s.txt", inputFilePath);
	
	@Parameter(names = { "-n", "--numanalyses" }, description = "maximum number of analyses to show (non-negative integer; 0 to show all analyses - default)", validateWith = PositiveInteger.class)
	int maxAnalysesToShow = 0;

	Builder builder;
	
	public static void main(String[] args) {
		
		StandaloneLauncher launcher = new StandaloneLauncher();
		
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
			this.builder = new Builder(this.maxAnalysesToShow); 
			builder.processFile(this.inputFilePath, this.outputFilePath, true);
		} catch (IOException | ConfigParseException e) {
			e.printStackTrace();
		}
	}
}
