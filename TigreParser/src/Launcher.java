import java.io.IOException;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class Launcher {
	
	@Parameter(names = { "-i", "-input" }, description = "input file path")
	String inputFilePath = "input.txt";
	
	@Parameter(names = { "-o", "-output" }, description = "output file path")
	String outputFilePath = "output.txt";
	
	@Parameter(names = "-numanalyses", description = "number of analyses to show (non-negative integer; 0 to show all analyses; or don't specify this paramenter)")
	int numAnalysesToShow = 0;
	
	public static void main(String[] args) {
		Launcher launcher = new Launcher();
		
		JCommander.newBuilder()
		.addObject(launcher)
		.build()
		.parse(args);
		
		launcher.run();
	}
	
	public void run () {
		
		try {
			if (numAnalysesToShow >= 0) {
				new Builder().processFile(inputFilePath, outputFilePath, numAnalysesToShow);
			} else {
				throw new IllegalArgumentException("-numanalyses must be a non-negative integer (0 to show all analyses)");
			}
			
		} catch (IOException e) { e.printStackTrace(); }
		
	}
}
