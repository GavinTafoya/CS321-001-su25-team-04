package cs321.create;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

import cs321.common.ParseArgumentException;


/**
 * The driver class for wrangling a raw SSH log file into a useful form.
 *
 * @author 
 */
public class SSHDataWrangler {

	private static String rawSSHFile = null;
	private static String SSHFile = null;

    private static final String KEYWORD_REGEX = "(?i)---|\\bpm\\b|\\bSSHD\\b|\\bpassword\\b|\\bfrom and for\\b|\\binvalid user\\b|\\buser\\b|\\bfailed - POSSIBLE BREAK-IN ATTEMPT!\\b|\\bmapping\\b|\\bmaps to\\b|\\bchecking getaddrinfo\\b|-";
    private static final String LAB_ID_REGEX = "(?i)Lab-id:[\\[a-j]*]";
    private static final String FAILED_ID_REGEX = "(?i)Failed id.*";

    /**
     * Main driver of program.
     * @param args
     */
    public static void main(String[] args) throws Exception 
	{
        parseArguments(args);

        File rawSSH = new File(rawSSHFile);
        File ssh = new File(SSHFile);

        if (!ssh.exists()) {
            ssh.createNewFile();
        }

        Scanner scanner = new Scanner(rawSSH);
        FileWriter writer = new FileWriter(ssh);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            line = line.replaceAll(KEYWORD_REGEX, "");
            line = line.replaceAll(LAB_ID_REGEX, "");
            line = line.replaceAll(FAILED_ID_REGEX, "").trim();

            writer.write(line + "\n");
        }
        scanner.close();
        writer.close();
	}


    /**
     * Process command line arguments.
     * @param args  The command line arguments passed to the main method.
     */
    public static void parseArguments(String[] args) throws ParseArgumentException
    {
        if(args.length != 2) {
            printUsageAndExit("Error: Invalid number of arguments.");
        }

        for (String arg : args) {
            if (arg.startsWith("rawSSHFile=")) {
                rawSSHFile = arg.substring("rawSSHFile=".length());
            } else if (arg.startsWith("sshFile=")) {
                SSHFile = arg.substring("sshFile=".length());
            } else {
                printUsageAndExit("Error: Unknown argument: " + arg);
            }
        }

        if (rawSSHFile == null || SSHFile == null) {
            printUsageAndExit("Error: Missing required arguments.");
        }
    }


	/** 
	 * Print usage message and exit.
	 * @param errorMessage the error message for proper usage
	 */
	private static void printUsageAndExit(String errorMessage)
    {
        System.err.println(errorMessage);
        System.err.println("Usage: java SSHDataWrangler rawSSHFile=<rawSSHFile> sshFile=<wrangledSSHFile>");
        System.exit(1);
	}

}
