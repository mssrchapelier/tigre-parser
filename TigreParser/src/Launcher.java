import java.io.IOException;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class Launcher {
	
	@Parameter(names = { "-i", "--input" }, description = "input file path")
	String inputFilePath = "input.txt";
	
	@Parameter(names = { "-o", "--output" }, description = "output file path")
	String outputFilePath = "output.txt";
	
	@Parameter(names = { "-n", "--numanalyses" }, description = "number of analyses to show (non-negative integer; 0 to show all analyses - default)")
	int maxAnalysesToShow = 0;

	Builder builder;
	
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
			if (this.maxAnalysesToShow < 0) {
				throw new IllegalArgumentException("numanalyses must be a non-negative integer (0 to show all analyses - default)");
			}
			this.builder = new Builder(this.maxAnalysesToShow); 
			builder.processFile(inputFilePath, outputFilePath, true);
		} catch (IOException|ConfigParseException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
}
