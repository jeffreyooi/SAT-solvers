package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * File utility to get file contents in String.
 */
public class FileUtil {
    /**
     * Reads the file content from path and return the contents as String.
     * @param path path of the file.
     * @return contents of the file in String.
     */
    public static String getFileString(String path) {
        File file = new File(path);

        // If file does not exist or it is a directory, return null.
        if (!file.exists() || file.isDirectory()) {
            return null;
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            return sb.toString();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }
}
