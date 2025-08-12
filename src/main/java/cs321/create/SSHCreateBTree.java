package cs321.create;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cs321.btree.BTree;
import cs321.btree.TreeObject;
import cs321.common.ParseArgumentException;



/**
 * The driver class for building a BTree representation of an SSH Log file.
 *
 * @author 
 */
public class SSHCreateBTree {
    /**
     * Main driver of program.
     * @param args
     */
    public static void main(String[] args) throws Exception 
	{
        SSHCreateBTreeArguments myArgs = parseArguments(args);
        
        if (myArgs == null) {
            printUsageAndExit("Error: Invalid arguments provided.");
        }

        // Create the BTree
        
        String btreeFileName = "SSH_log.txt.ssh.btree." + myArgs.getTreeType() + "." + myArgs.getDegree();
        BTree bTree = new BTree(myArgs.getDegree(), btreeFileName);

        // Read the SSH log file
        SSHFileReader fileReader = new SSHFileReader(myArgs.getSSHFileName(), myArgs.getTreeType());
        List<String> logEntries = fileReader.readEntries();

        // Insert log entries into the BTree
        for (String entry : logEntries) {
            TreeObject treeObject = new TreeObject(entry);
            bTree.insert(treeObject);
        }

        // Dump file if debug is enabled
        if (myArgs.getDebugLevel() == 1) {
            try (PrintWriter printWriter = new PrintWriter(new File("BTreeDump.txt"))) {
                bTree.dumpToFile(printWriter);
            }
        }

        bTree.dumpToDatabase("SSHLogDB.db", myArgs.getTreeType() + "." + myArgs.getDegree());
	}


    /**
     * Process command line arguments.
     * @param args  The command line arguments passed to the main method.
     */
    public static SSHCreateBTreeArguments parseArguments(String[] args) throws ParseArgumentException
    {
        Map<String, String> argMap = new HashMap<>();
        for (String arg : args) {
            String[] keyValue = arg.split("=");
            if (keyValue.length == 2) {
                argMap.put(keyValue[0], keyValue[1]);
            }
        }

        // Validate --cache argument
        if (!argMap.containsKey("--cache") 
        || !(argMap.get("--cache").equals("0") || argMap.get("--cache").equals("1"))) {
            printUsageAndExit("Error: --cache argument is required.");
        }

        // Validate --degree argument
        if (!argMap.containsKey("--degree")) {
            printUsageAndExit("Error: --degree argument is required.");
        }

        int degreeValue;
        try {
            degreeValue = Integer.parseInt(argMap.get("--degree"));
        } 
        catch (NumberFormatException e) {
            printUsageAndExit("Error: --degree must be a valid integer.");
            return null; // unreachable, but keeps compiler happy
        }
        if (degreeValue != 0 && degreeValue < 2) {
            printUsageAndExit("Error: --degree must be 0 (default) or greater than 1.");
        }

        // Validate --sshFile argument
        if (!argMap.containsKey("--sshFile") 
        || argMap.get("--sshFile").isEmpty()) {
            printUsageAndExit("Error: --sshFile argument is required.");
        }

        // Validate --type argument
        if(!argMap.containsKey("--type")
        || argMap.get("--type").isEmpty()) {
            printUsageAndExit("Error: --type argument is required.");
        }

        // Validate --cache-size argument
       if (argMap.get("--cache").equals("1")) {
            if (!argMap.containsKey("--cache-size")) {
                printUsageAndExit("Error: --cache-size required when cache is enabled.");
            }
            try {
                if (Integer.parseInt(argMap.get("--cache-size")) <= 0) {
                    printUsageAndExit("Error: --cache-size must be > 0.");
                }
            } catch (NumberFormatException e) {
                printUsageAndExit("Error: --cache-size must be an integer.");
            }
        } 
        else {
            argMap.put("--cache-size", "0");
        }

        // Validate --database argument
        if (!argMap.containsKey("--database") 
        || argMap.get("--database").isEmpty()
        || !(argMap.get("--database").equals("yes") || argMap.get("--database").equals("no"))) {
            printUsageAndExit("Error: --database argument is required.");
        }

        // Validate --debug argument
        if (argMap.containsKey("--debug")) {
            if (!argMap.get("--debug").equals("0") && !argMap.get("--debug").equals("1")) {
                printUsageAndExit("Error: --debug must be 0 or 1.");
            }
        } 
        else {
            argMap.put("--debug", "0");
        }

        // Get the degree
        int degree = (!argMap.get("--degree").equals("0")) ? Integer.parseInt(argMap.get("--degree")) : 25;

        // Create the SSHCreateBTreeArguments object
        SSHCreateBTreeArguments arguments = new SSHCreateBTreeArguments(
            argMap.get("--cache").equals("1"), 
            degree,
            argMap.get("--sshFile"), 
            argMap.get("--type"),
            Integer.parseInt(argMap.get("--cache-size")),
            Integer.parseInt(argMap.get("--debug"))
        );

        return arguments;
    }


	/** 
	 * Print usage message and exit.
	 * @param errorMessage the error message for proper usage
	 */
	private static void printUsageAndExit(String errorMessage)
    {
        System.out.println(errorMessage);
        System.out.println("Usage: java -jar build/libs/SSHCreateBTree.jar --cache=<0/1> --degree=<btree-degree> \\\n"
        + "--sshFile=<ssh-File> --type=<tree-type> [--cache-size=<n>] \\\n"
        +  "--database=<yes/no> [--debug=<0|1>]);");
        System.exit(1);
	}

}
