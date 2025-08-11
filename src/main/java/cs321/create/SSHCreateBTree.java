package cs321.create;

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
        BTree bTree = new BTree(myArgs.getDegree(), myArgs.getSSHFileName());

        // Read the SSH log file
        SSHFileReader fileReader = new SSHFileReader(myArgs.getSSHFileName());
        List<String> logEntries = fileReader.readEntries();

        // Insert log entries into the BTree
        for (String entry : logEntries) {
            TreeObject treeObject = new TreeObject(entry);
            bTree.insert(treeObject);
        }
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
        || argMap.get("--cache").equals("0") 
        || argMap.get("--cache").equals("false")) {
            printUsageAndExit("Error: --cache argument is required.");
        }

        // Validate --degree argument
        try {
            if (!argMap.containsKey("--degree") 
            || Integer.parseInt(argMap.get("--degree")) != 0 
            || Integer.parseInt(argMap.get("--degree")) < 2) {
                printUsageAndExit("Error: --degree argument is required.");
            }
        } catch (NumberFormatException e) {
            printUsageAndExit("Error: --degree must be a valid integer.");
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
        try {
            if (argMap.containsKey("--cache-size") 
            && (argMap.get("--cache-size").isEmpty() || Integer.parseInt(argMap.get("--cache-size")) <= 0)) {
                printUsageAndExit("Error: --cache-size argument must be specified.");
            }
        } catch (NumberFormatException e) {
            printUsageAndExit("Error: --cache-size must be a valid integer.");
        }   

        // Validate --database argument
        if (!argMap.containsKey("--database") 
        || argMap.get("--database").isEmpty()
        || !(argMap.get("--database").equals("yes") || argMap.get("--database").equals("no"))) {
            printUsageAndExit("Error: --database argument is required.");
        }

        // Validate --debug argument
        try {
            if (argMap.containsKey("--debug") 
            && (argMap.get("--debug").isEmpty() || Integer.parseInt(argMap.get("--debug")) < 0)) {
                printUsageAndExit("Error: --debug argument must be specified.");
            }
            if (!argMap.containsKey("--debug")) {
                argMap.put("--debug", "0"); // Default debug level
            }
        } catch (NumberFormatException e) {
            printUsageAndExit("Error: --debug must be a valid integer.");
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
