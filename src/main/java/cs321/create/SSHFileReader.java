package cs321.create;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Extracts the SSH log file entries.
 *
 * @author 
 */
public class SSHFileReader {

    private final String fileName;
    private final String[] treeType;

    private static boolean isIPv4(String s) {
        return s != null && s.matches("\\d{1,3}(?:\\.\\d{1,3}){3}");
    }

    public SSHFileReader(String sshFileName, String treeType) {
        this.fileName = sshFileName;
        this.treeType = treeType.split("-");
    }

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
                        if (!action.equalsIgnoreCase("reverse")
                                && !action.equalsIgnoreCase("Address")
                                && t.length >= 5
                                && isIPv4(t[4])) {
                            entries.add(t[3] + "-" + t[4]);
                        }
                    } else if ("reverseaddress".equals(kind)) {
                        if (action.equalsIgnoreCase("reverse") || action.equalsIgnoreCase("Address")) {
                            String ip = null;
                            for (int i = 3; i < t.length; i++) {
                                if (isIPv4(t[i])) { ip = t[i]; break; }
                            }
                            if (ip != null) {
                                String prefix = action.equalsIgnoreCase("Address") ? "Address-" : "reverse-";
                                entries.add(prefix + ip);
                            }
                        }
                    } else {
                        if (action.equalsIgnoreCase(kind) && t.length >= 5) {
                            entries.add(action + "-" + t[4]);
                        }
                    }
                } else if ("time".equals(mode)) {
                    if ("reverseaddress".equals(kind)) {
                        if (action.equalsIgnoreCase("reverse") || action.equalsIgnoreCase("Address")) {
                            String prefix = action.equalsIgnoreCase("Address") ? "Address-" : "reverse-";
                            entries.add(prefix + hhmm);
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
