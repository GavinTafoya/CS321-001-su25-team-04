package cs321.search;

/**
 * Holds parsed command line arguments for the SSHSearchDatabase
 */
public class SSHSearchDatabaseArguments {
    private final String databaseFileName;
    private final String bTreeType;
    private final int topFrequencyCount;
    private final boolean useTypeSet;

    /**
     * Constructs arguments object.
     *
     * @param databaseFileName Database file path
     * @param bTreeType "accepted-time"
     * @param topFrequencyCount Top N like 10, 25 or 50
     * @param useTypeSet True if --typeset flag is set
     */
    public SSHSearchDatabaseArguments(String databaseFileName, String bTreeType, int topFrequencyCount, boolean useTypeSet) {
        this.databaseFileName = databaseFileName;
        this.bTreeType = bTreeType;
        this.topFrequencyCount = topFrequencyCount;
        this.useTypeSet = useTypeSet;
    }

    public String getDatabaseFileName() {
        return databaseFileName;
    }

    public String getBTreeType() {
        return bTreeType;
    }

    public int getTopFrequencyCount() {
        return topFrequencyCount;
    }

    public boolean isUseTypeSet() {
        return useTypeSet;
    }

    public static SSHSearchDatabaseArguments parseArguments(String[] args) {
        String database = null;
        String type = null;
        int top = -1;
        boolean useTypeSet = false;

        for (String arg : args) {
            if (arg.startsWith("--database=")) {
                database = arg.substring("--database=".length());
            } else if (arg.startsWith("--type=")) {
                type = arg.substring("--type=".length());
            } else if (arg.startsWith("--top-frequency=")) {
                top = Integer.parseInt(arg.substring("--top-frequency=".length()));
            } else if (arg.equalsIgnoreCase("--typeset")) {
                useTypeSet = true;
            }
        }

        if (database == null || type == null || top == -1) {
            throw new IllegalArgumentException("Missing required arguments.");
        }

        return new SSHSearchDatabaseArguments(database, type, top, useTypeSet);
    }


    @Override
    public String toString() {
        return "SSHSearchDatabaseArguments{" + "databaseFileName='" + databaseFileName + '\'' + ", bTreeType='" + bTreeType + '\'' + ", topFrequencyCount=" + topFrequencyCount + ", useTypeSet=" + useTypeSet + '}';
    }
}