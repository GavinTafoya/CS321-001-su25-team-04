package cs321.search;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/** Search the SQLite DB built from the BTrees. */
public class SSHSearchDatabase {

    private static final String[][] TEST_DATA = {
            {"Accepted-111.222.107.90", "25"},
            {"Accepted-112.96.173.55", "3"},
            {"Accepted-112.96.33.40", "3"},
            {"Accepted-113.116.236.34", "6"},
            {"Accepted-113.118.187.34", "2"},
            {"Accepted-113.99.127.215", "2"},
            {"Accepted-119.137.60.156", "1"},
            {"Accepted-119.137.62.123", "9"},
            {"Accepted-119.137.62.142", "1"},
            {"Accepted-119.137.63.195", "14"},
            {"Accepted-123.255.103.142", "5"},
            {"Accepted-123.255.103.215", "5"},
            {"Accepted-137.189.204.138", "1"},
            {"Accepted-137.189.204.155", "1"},
            {"Accepted-137.189.204.220", "1"},
            {"Accepted-137.189.204.236", "1"},
            {"Accepted-137.189.204.246", "1"},
            {"Accepted-137.189.204.253", "3"},
            {"Accepted-137.189.205.44", "2"},
            {"Accepted-137.189.206.152", "1"},
            {"Accepted-137.189.206.243", "1"},
            {"Accepted-137.189.207.18", "1"},
            {"Accepted-137.189.207.28", "1"},
            {"Accepted-137.189.240.159", "1"},
            {"Accepted-137.189.241.19", "2"}
    };

    public static void main(String[] args) {
        try {
            SSHSearchDatabaseArguments arguments = SSHSearchDatabaseArguments.parseArguments(args);
            String dbPath = arguments.getDatabaseFileName();
            String treeType = arguments.getBTreeType();
            int topN = arguments.getTopFrequencyCount();

            String url = "jdbc:sqlite:" + dbPath;

            try (Connection conn = DriverManager.getConnection(url)) {
                if (treeType.equalsIgnoreCase("test")) {
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute("DROP TABLE IF EXISTS acceptedip");
                        stmt.execute("CREATE TABLE acceptedip (key TEXT PRIMARY KEY, frequency INTEGER NOT NULL)");
                    }
                    try (PreparedStatement insert = conn.prepareStatement("INSERT INTO acceptedip(key,frequency) VALUES(?,?)")) {
                        for (String[] row : TEST_DATA) {
                            insert.setString(1, row[0]);
                            insert.setInt(2, Integer.parseInt(row[1]));
                            insert.executeUpdate();
                        }
                    }
                    try (PreparedStatement q =
                                 conn.prepareStatement("SELECT key,frequency FROM acceptedip ORDER BY frequency DESC, key ASC LIMIT ?")) {
                        q.setInt(1, topN);
                        try (ResultSet rs = q.executeQuery()) {
                            while (rs.next()) {
                                System.out.println(rs.getString("key") + " " + rs.getInt("frequency"));
                            }
                        }
                    }
                } else {
                    String table = treeType.replace("-", "");
                    try (PreparedStatement q =
                                 conn.prepareStatement("SELECT key,frequency FROM " + table + " ORDER BY frequency DESC, key ASC LIMIT ?")) {
                        q.setInt(1, topN);
                        try (ResultSet rs = q.executeQuery()) {
                            while (rs.next()) {
                                System.out.println(rs.getString("key") + " " + rs.getInt("frequency"));
                            }
                        }
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(2);
        }
    }
}
