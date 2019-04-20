package callback;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import config.Config;

public class ResultOutput {

    private String outputPath;
    private File outputFile;
    private FileWriter writer;

    public ResultOutput(String outputPath) {
        Date date = new Date();
        this.outputPath = String.format("%s-%s.txt", outputPath, date.getTime());
        this.outputFile = new File("result", this.outputPath);
    }

    public void initialize() throws IOException {
        if (!outputFile.exists()) {
            outputFile.getParentFile().mkdirs();
            boolean createResult = outputFile.createNewFile();
            if (!createResult && Config.logging == Config.Logging.VERBOSE) {
                System.out.println(String.format("Output file %s already exists, skipping creation.", outputPath));
            }
        }

        writer = new FileWriter(outputFile);
    }

    public void write(String data) throws IOException {
        writer.write(data);
    }

    public void close() throws IOException {
        writer.flush();
        writer.close();
    }
}
