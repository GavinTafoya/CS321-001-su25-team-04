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
    private String treeType[];

    /**
     * Constructor for SSHFileReader.
     * @param sshFileName The name of the SSH log file to read.
     */
    public SSHFileReader(String sshFileName, String treeType) {
        // Initialize with the SSH file name
        this.fileName = sshFileName;
        this.treeType = treeType.split("-");
    }

    /**
     * Read and return all entries from the SSH log file.
     * @return A list of log entries.
     * @throws Exception 
     */
    public List<String> readEntries() throws Exception {
        List<String> entries = new ArrayList<>();

        File wrangledFile = new File(fileName);

        try (Scanner scanner = new Scanner(wrangledFile)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] t = line.split("\\s+");
                if (t.length < 3) continue;

                String action = t[2];
                String kind   = treeType[0];
                String mode   = treeType[1];

                String hhmm = (t[1].length() >= 5) ? t[1].substring(0, 5) : t[1];

                if ("ip".equals(mode)) {
                    if ("user".equals(kind)) {
                        if (!action.equalsIgnoreCase("reverse") && !action.equalsIgnoreCase("Address") && t.length >= 5) {
                            entries.add(t[3] + "-" + t[4]);
                        }
                    } else if ("reverseaddress".equals(kind)) {
                        // Include both reverse and Address; IP is t[3]
                        if ((action.equalsIgnoreCase("reverse") || action.equalsIgnoreCase("Address")) && t.length >= 4) {
                            entries.add(action + "-" + t[3]); // <- IMPORTANT prefix: actual action
                        }
                    } else {
                        if (action.equalsIgnoreCase(kind) && t.length >= 5) {
                            entries.add(action + "-" + t[4]);
                        }
                    }
                } else if ("time".equals(mode)) {
                    if ("reverseaddress".equals(kind)) {
                        if (action.equalsIgnoreCase("reverse") || action.equalsIgnoreCase("Address")) {
                            entries.add(action + "-" + hhmm);
                        }
                    } else {
                        if (action.equalsIgnoreCase(kind)) {
                            entries.add(action + "-" + hhmm);
                        }
                    }
                }
            }
        }
        return entries;
    }
}
