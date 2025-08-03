package cs321.search;

import org.junit.After;
import org.junit.Test;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.Assert.*;

public class SSHSearchDatabaseTest
{

    private final String testDb = "test.db";

    @After
    public void cleanup() {
        new File(testDb).delete();
    }

    @Test
    public void testFakeDataInsertion() throws Exception {
        // Run test mode given in the instructions
        // $ java -jar build/libs/SSHSearchDatabase.jar --type=test --database=test.db \
        //            --top-frequency=<10/25/50>
        // result I got
        /**
         * ahmedrao@Ahmeds-MacBook-Pro CS321-001-su25-team-04 % java -jar build/libs/SSHSearchDatabase.jar --type=test --database=test.db --top-frequency=25
         *
         * WARNING: A restricted method in java.lang.System has been called
         * WARNING: java.lang.System::load has been called by org.sqlite.SQLiteJDBCLoader in an unnamed module (file:/Users/ahmedrao/Desktop/cs321/p4/CS321-001-su25-team-04/build/libs/SSHSearchDatabase.jar)
         * WARNING: Use --enable-native-access=ALL-UNNAMED to avoid a warning for callers in this module
         * WARNING: Restricted methods will be blocked in a future release unless native access is enabled
         *
         * Accepted-111.222.107.90 25
         * Accepted-119.137.63.195 14
         * Accepted-119.137.62.123 9
         * Accepted-113.116.236.34 6
         * Accepted-123.255.103.142 5
         * Accepted-123.255.103.215 5
         * Accepted-112.96.173.55 3
         * Accepted-112.96.33.40 3
         * Accepted-137.189.204.253 3
         * Accepted-113.118.187.34 2
         * Accepted-113.99.127.215 2
         * Accepted-137.189.205.44 2
         * Accepted-137.189.241.19 2
         * Accepted-119.137.60.156 1
         * Accepted-119.137.62.142 1
         * Accepted-137.189.204.138 1
         * Accepted-137.189.204.155 1
         * Accepted-137.189.204.220 1
         * Accepted-137.189.204.236 1
         * Accepted-137.189.204.246 1
         * Accepted-137.189.206.152 1
         * Accepted-137.189.206.243 1
         * Accepted-137.189.207.18 1
         * Accepted-137.189.207.28 1
         * Accepted-137.189.240.159 1
         *
         * */
        String[] args = {"--type=test", "--database=" + testDb, "--top-frequency=25"};
        SSHSearchDatabase.main(args);

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + testDb);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM acceptedip")) {
            assertTrue(rs.next());
            assertEquals(25, rs.getInt(1));  // Confirm that the 25 rows are inserted
        }
    }

    @Test
    public void testTopFrequencyOrdering() throws Exception {
        // Creates the test data
        String[] args = {"--type=test", "--database=" + testDb, "--top-frequency=10"};
        SSHSearchDatabase.main(args);

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + testDb);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT key, frequency FROM acceptedip ORDER BY frequency DESC, key ASC LIMIT 1")) {
            assertTrue(rs.next());
            assertEquals("Accepted-111.222.107.90", rs.getString("key"));
            assertEquals(25, rs.getInt("frequency"));
        }
    }
}
