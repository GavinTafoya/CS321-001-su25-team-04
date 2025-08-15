package cs321.search;

import java.util.HashMap;
import java.util.Map;

public class SSHSearchBTreeArguments {
    private final boolean useCache;
    private final int degree;
    private final int cacheSize;
    private final int debug;
    private final String btreeFile;
    private final String queryFile;
    private final int topFrequencyCount;


    public SSHSearchBTreeArguments(boolean useCache, int degree, int cacheSize, int debug, String btreeFile, String queryFile, int topFrequencyCount) {
        this.useCache = useCache;
        this.degree = degree;
        this.cacheSize = cacheSize;
        this.debug = debug;
        this.btreeFile = btreeFile;
        this.queryFile = queryFile;
        this.topFrequencyCount = topFrequencyCount;
    }

    public boolean isUseCache() { return useCache; }
    public int getDegree() { return degree; }
    public int getCacheSize() { return cacheSize; }
    public int getDebug() { return debug; }
    public String getBtreeFile() { return btreeFile; }
    public String getQueryFile() { return queryFile; }
    public int getTopFrequencyCount() { return topFrequencyCount; }

    public static SSHSearchBTreeArguments parse(String[] args) {
        Map<String, String> m = new HashMap<>();
        for (String a : args) {
            if (!a.startsWith("--")) continue;
            int eq = a.indexOf('=');
            if (eq > 0) {
                m.put(a.substring(0, eq).trim(), a.substring(eq + 1).trim());
            } else {
                m.put(a.trim(), "1");
            }
        }

        String btree = m.get("--btree-file");
        String query = m.get("--query-file");
        String degreeStr = m.get("--degree");
        if (btree == null || btree.isEmpty()) throw new IllegalArgumentException("Missing --btree-file=<path>");
        if (query == null || query.isEmpty()) throw new IllegalArgumentException("Missing --query-file=<path>");
        if (degreeStr == null) throw new IllegalArgumentException("Missing --degree=<int> (0 for default)");

        int degree;
        try {
            degree = Integer.parseInt(degreeStr);
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("--degree must be an integer");
        }
        if (degree != 0 && degree < 2) throw new IllegalArgumentException("--degree must be 0 or >= 2");

        boolean useCache = "1".equals(m.get("--cache"));
        int cacheSize = 0;
        if (m.containsKey("--cache-size")) {
            try { cacheSize = Integer.parseInt(m.get("--cache-size")); }
            catch (NumberFormatException e) { throw new IllegalArgumentException("--cache-size must be an integer"); }
        }

        int debug = 0;
        if (m.containsKey("--debug")) {
            String d = m.get("--debug");
            if (!"0".equals(d) && !"1".equals(d)) throw new IllegalArgumentException("--debug must be 0 or 1");
            debug = Integer.parseInt(d);
        }

        int topN = -1;
        if (m.containsKey("--top-frequency")) {
            try { topN = Integer.parseInt(m.get("--top-frequency")); }
            catch (NumberFormatException e) { throw new IllegalArgumentException("--top-frequency must be an integer"); }
            if (topN < 1) throw new IllegalArgumentException("--top-frequency must be >= 1");
        }

        return new SSHSearchBTreeArguments(useCache, degree, cacheSize, debug, btree, query, topN);
    }

    @Override
    public String toString() {
        return "SSHSearchBTreeArguments{" +
                "useCache=" + useCache +
                ", degree=" + degree +
                ", cacheSize=" + cacheSize +
                ", debug=" + debug +
                ", btreeFile='" + btreeFile + '\'' +
                ", queryFile='" + queryFile + '\'' +
                ", topFrequencyCount=" + topFrequencyCount +
                '}';
    }
}
