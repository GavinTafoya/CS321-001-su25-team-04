package cs321.search;

import cs321.btree.BTree;
import cs321.common.ParseArgumentException;
import cs321.common.ParseArgumentUtils;
import java.sql.*;

/**
 * The driver class for searching a Database of a B-Tree.
 */
public class SSHSearchDatabase
{

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
            String tableName = treeType.replace("-", "");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            Statement stmt = connection.createStatement();

            if (treeType.equalsIgnoreCase("test")) {
                stmt.execute("CREATE TABLE IF NOT EXISTS acceptedip (key TEXT PRIMARY KEY, frequency INTEGER)");
                PreparedStatement insert = connection.prepareStatement("INSERT OR REPLACE INTO acceptedip (key, frequency) VALUES (?, ?)");
                for (String[] row : TEST_DATA) {
                    insert.setString(1, row[0]);
                    insert.setInt(2, Integer.parseInt(row[1]));
                    insert.executeUpdate();
                }
                insert.close();

                PreparedStatement query = connection.prepareStatement(
                        "SELECT key, frequency FROM acceptedip ORDER BY frequency DESC, key ASC LIMIT ?");
                query.setInt(1, topN);
                ResultSet rs = query.executeQuery();
                while (rs.next()) {
                    System.out.println(rs.getString("key") + " " + rs.getInt("frequency"));
                }
                rs.close();
                query.close();
            } else {
                PreparedStatement query = connection.prepareStatement("SELECT key, frequency FROM " + tableName + " ORDER BY frequency DESC, key ASC LIMIT ?");
                query.setInt(1, topN);
                ResultSet rs = query.executeQuery();
                while (rs.next()) {
                    System.out.println(rs.getString("key") + " " + rs.getInt("frequency"));
                }
                rs.close();
                query.close();
            }
            stmt.close();
            connection.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
