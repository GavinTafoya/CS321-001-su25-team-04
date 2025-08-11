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
     */
    public List<String> readEntries() {
        List<String> entries = new ArrayList<>();

        // Read the SSH log file
        try (Scanner scanner = new Scanner(new File(fileName))) {
            String keyStart = treeType[0] + "-";

            // Determine the type of information to extract
            switch(treeType[1]){
                case "ip":
                    //Find ips at desired results
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        // Extract the IP address
                        if(!treeType[0].equals("user")){
                            if(line.contains(treeType[0])){
                                entries.add(keyStart + line.split(" ")[0]);
                            }
                        } else {
                            entries.add(line.split(" ")[3] + "-" + line.split(" ")[4]);
                        }
                    }
                    break;
                case "time":
                    // Extract the times at desired results
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        // Extract the time
                        if(line.contains(treeType[0])){
                            entries.add(keyStart + line.split(" ")[1]);
                        }
                    }
                    break;
                default:
                    break;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return entries;
    }
}
