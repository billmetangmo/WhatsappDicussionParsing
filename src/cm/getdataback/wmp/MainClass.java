package cm.getdataback.wmp;

import io.krakens.grok.api.Grok;
import io.krakens.grok.api.GrokCompiler;
import io.krakens.grok.api.Match;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;


public class MainClass {

	public static void main(String[] args) throws IOException {

		/* Create a new grokCompiler instance */
		GrokCompiler grokCompiler = GrokCompiler.newInstance();
		grokCompiler.registerDefaultPatterns();

		// This pattern accepts multiple words separate with space as
		// whatsapp username in Contacts can have fisrtname & lastname
		grokCompiler.register("USERNAME_PHONE","[a-zA-Z0-9\\+._-]+\\s*[a-zA-Z0-9._-]*");

		/* Grok pattern to compile, here whatsapp message */
		// This pattern only work for french language
		final Grok grok = grokCompiler.compile("%{DATE_EU:date} à %{HOUR:hour}:%{MINUTE:minute} - %{USERNAME_PHONE:username}: %{GREEDYDATA:message}");

		FileWriter fw = null;
		CSVPrinter csvPrinter = null;

		int notMatch = 0;

		try {

			// replace "" with input file location (whatsapp discussion)
			Path whatsapp_discussion = Paths.get("");

			// replace "" with output file location (csv file)
			fw = new FileWriter("");

			csvPrinter = new CSVPrinter(fw, CSVFormat.DEFAULT
					.withHeader("date", "hour","username", "message"));

			// UTF-8 to support all languages
			Charset charset = Charset.forName("UTF-8");
			try {
				List<String> lines = Files.readAllLines(whatsapp_discussion, charset);

				for (String line : lines) {
					Match gm = grok.match(line);

					if (!gm.isNull()){
						Map<String, Object> map  = gm.capture();
						csvPrinter.printRecord(map.get("date"),map.get("hour")+":"+map.get("minute"),map.get("username"),map.get("message") );
						notMatch++;
					}

				}

				System.out.printf("%d/%d matches found ",notMatch,lines.size());
			} catch (IOException e) {
				System.out.println(e);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			csvPrinter.flush();
			csvPrinter.close();
		}

	}

}
