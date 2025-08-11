package cs321.create;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Extracts the SSH log file entries.
 *
 * @author 
 */
public class SSHFileReader {

    private String fileName;

    /**
     * Constructor for SSHFileReader.
     * @param sshFileName The name of the SSH log file to read.
     */
    public SSHFileReader(String sshFileName) {
        // Initialize with the SSH file name
        this.fileName = sshFileName;
    }

    /**
     * Read and return all entries from the SSH log file.
     * @return A list of log entries.
     */
    public List<String> readEntries() {
        List<String> entries = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(fileName))) {
            while (scanner.hasNextLine()) {
                entries.add(scanner.nextLine());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return entries;
    }
}
