package callback;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import config.Config;

public class CSVLogger {

    private String logPath;
    private String delimiter;
    private File logFile;
    private FileWriter writer;

    public CSVLogger(String logPath, String delimiter) {
        Date date = new Date();
        this.logPath = String.format("%s-%s.csv", logPath, date.getTime());
        this.delimiter = delimiter;
        this.logFile = new File("log", this.logPath);
    }

    public void initialize() throws IOException {
        if (!logFile.exists()) {
            logFile.getParentFile().mkdirs();
            boolean createResult = logFile.createNewFile();
            if (!createResult && Config.logging == Config.Logging.VERBOSE) {
                System.out.println(String.format("Log file %s already exists, skipping creation.", logPath));
            }
        }

        writer = new FileWriter(logFile);
    }

    public void logNext(String[] data) throws IOException {
        String tokenizedString = convertStringArrayToString(data);
        writer.write(tokenizedString);
    }

    public void close() throws IOException {
        writer.flush();
        writer.close();
    }

    private String convertStringArrayToString(String[] data) {
        StringBuilder sb = new StringBuilder();

        for (String d : data) {
            sb.append(d);
            sb.append(delimiter);
        }

        sb.append("\n");

        return sb.toString();
    }
}
