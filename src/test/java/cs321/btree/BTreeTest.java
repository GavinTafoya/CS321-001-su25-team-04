package cs321.btree;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Unit testing for BTree constructors, Insert, Search and
 * some TreeObject interactions in the BTree (such as counting duplicates)
 *
 * Note some tests use Alphabetic letters as keys to follow the examples
 * given in the textbook.
 *
 * @author CS321 instructors
 */
public class BTreeTest {

    /**
     * Use the same filename for each time a BTree is created.
     */
    private static String testFilename = "Test_BTree.tmp";

    /**
     * Avoid some test errors if the test file failed to clean up
     * in a previous run.
     */
    @BeforeClass
    public static void beforeAll() {

        deleteTestFile(testFilename);
    }

    /**
     * After each test case, remove the test file.
     */
    @After
    public void cleanUpTests() {

        deleteTestFile(testFilename);
    }

    // HINT:
    //  instead of checking all intermediate states of constructing a tree
    //  you can check the final state of the tree and
    //  assert that the constructed tree has the expected number of nodes and
    //  assert that some (or all) of the nodes have the expected values
    @Test
    public void btreeDegree4Test()
    {
//        //TODO instantiate and populate a bTree object
//        int expectedNumberOfNodes = TBD;
//
//        // it is expected that these nodes values will appear in the tree when
//        // using a level traversal (i.e., root, then level 1 from left to right, then
//        // level 2 from left to right, etc.)
//        String[] expectedNodesContent = new String[]{
//                "TBD, TBD",      //root content
//                "TBD",           //first child of root content
//                "TBD, TBD, TBD", //second child of root content
//        };
//
//        assertEquals(expectedNumberOfNodes, bTree.getNumberOfNodes());
//        for (int indexNode = 0; indexNode < expectedNumberOfNodes; indexNode++)
//        {
//            // root has indexNode=0,
//            // first child of root has indexNode=1,
//            // second child of root has indexNode=2, and so on.
//            assertEquals(expectedNodesContent[indexNode], bTree.getArrayOfNodeContentsForNodeIndex(indexNode).toString());
//        }
    }

    /**
     * Test simple creation of an empty BTree.
     * An empty BTree has 1 node with no keys and height of 0.
     *
     * @throws BTreeException Exception thrown when BTree encounters an unexpected problem
     */
    @Test
    public void testCreate() throws BTreeException{


        BTree b = new BTree(testFilename);

        //height should be 0
        assertEquals(0, b.getHeight());

        //size should be 0
        assertEquals(0, b.getSize());

        //will have only 1 node, the root
        assertEquals(1, b.getNumberOfNodes());

    }

    /**
     * Test constructing a BTree with custom degree.
     *
     * @throws BTreeException Exception thrown when BTree encounters an unexpected problem
     */
    @Test
    public void testCreateDegree () throws BTreeException {

        BTree b = new BTree(3, testFilename);

        assertEquals(3, b.getDegree());

    }

    /**
     * Test inserting a single key into an empty BTree.
     * BTree size now reflects the single key.
     * BTree structure is not validated in this test, as it would depend
     * on searching the tree or examining private members of BTree.
     *
     * @throws BTreeException Exception thrown when BTree encounters an unexpected problem
     * @throws IOException Exception thrown when testing fails due to IO errors
     */
    @Test
    public void testInsertOneKey() throws BTreeException, IOException {

        BTree b = new BTree(2, testFilename);

        b.insert(new TreeObject("1"));

        assertEquals(1, b.getSize());
        assertEquals(0, b.getHeight());

        assertTrue(validateInserts(b, new String[]{"1"}));
    }

    /**
     * Ten Keys (0 -> 9) added to a tree of degree 2, ensuring full nodes will be split.
     *
     * @throws BTreeException Exception thrown when BTree encounters an unexpected problem
     * @throws IOException Exception thrown when testing fails due to IO errors
     */
    @Test
    public void testInsertTenKeys() throws BTreeException, IOException{

        BTree b = new BTree(2, testFilename);

        String[] input = new String[10];

        for (int i = 0; i < 10; i++) {
            input[i] = i + "";
            b.insert(new TreeObject(i + ""));
        }

        assertEquals(10, b.getSize());
        assertEquals(2, b.getHeight());

        assertTrue(validateInserts(b, input));
    }


    /**
     * Ten keys (10 -> 1) inserted into a BTree of degree 2.
     *
     * @throws BTreeException Exception thrown when BTree encounters an unexpected problem
     * @throws IOException Exception thrown when testing fails due to IO errors
     */
    @Test
    public void testInsertTenKeysReverseOrder() throws BTreeException, IOException {

        BTree b = new BTree(2, testFilename);

        String[] input = new String[10];

        for (int i = 10; i > 0; i--) {
            input[10 - i] = i + "";
            b.insert(new TreeObject(i + ""));
        }

        assertEquals(10, b.getSize());
        assertEquals(2, b.getHeight());

        assertTrue(validateInserts(b, input));
    }


    /**
     * Tests that adding duplicate key values to the tree doesn't create
     * duplicates within the tree.
     *
     * @throws BTreeException Exception thrown when BTree encounters an unexpected problem
     * @throws IOException Exception thrown when testing fails due to IO errors
     */
    @Test
    public void testInsertTenDuplicates() throws BTreeException, IOException {

        BTree b = new BTree(2, testFilename);

        for (int i = 0; i < 10; i++) {
            b.insert(new TreeObject(1 + ""));
        }

        assertEquals(1, b.getSize());
        assertEquals(0, b.getHeight());

        assertTrue(validateInserts(b, new String[]{"1", "1", "1", "1", "1", "1", "1", "1", "1", "1"}));
    }


    /**
     * Simply tests inserting many objects into the BTree (no duplicates).
     *
     * @throws BTreeException Exception thrown when BTree encounters an unexpected problem
     * @throws IOException Exception thrown when testing fails due to IO errors
     */
    @Test
    public void testInsertTenThousandObjects() throws BTreeException, IOException {

        BTree b = new BTree(2, testFilename);

        String[] input = new String[10000];

        for (int i = 0; i < 10000; i++) {
            input[i] = i + "";
            b.insert(new TreeObject(i + ""));
        }

        assertEquals(10000, b.getSize());

        assertTrue(validateInserts(b, input));
    }

    /**
     * Test inserting into a tree using the example in Figure 18.6 in CLRS.
     * Note that Letters have been transposed to numbers corresponding to
     * position in the English Alphabet (e.g. A = 1, B = 2)
     *
     * @throws BTreeException Exception thrown when BTree encounters an unexpected problem
     * @throws IOException Exception thrown when testing fails due to IO errors
     */
    @Test
    public void testCLRSExample18_6() throws BTreeException, IOException {

        BTree b = new BTree(4, testFilename);

        String[] input = new String[]{"A", "D", "F", "H", "L", "N", "P", "B"};

        for (int i = 0; i < input.length - 1; i++) {
            b.insert(new TreeObject(input[i]));
        }

        assertEquals(7, b.getSize());
        assertEquals(0, b.getHeight());
        assertEquals(1, b.getNumberOfNodes());

        b.insert(new TreeObject(input[7])); //Insert 'B'

        assertEquals(8, b.getSize());
        assertEquals(1, b.getHeight());
        assertEquals(3, b.getNumberOfNodes());

        assertTrue(validateInserts(b, input));
    }

    /**
     * Search test that queries an empty tree.
     *
     * @throws BTreeException Exception thrown when BTree encounters an unexpected problem
     * @throws IOException Exception thrown when testing fails due to IO errors
     */
    @Test
    public void testSearchEmptyTree() throws BTreeException, IOException {

        BTree b = new BTree(2, testFilename);

        TreeObject t = b.search("1");

        assertNull(t);

    }

    /**
     * Search test that adds a TreeObject and then searches for it.
     * Assumes that BTree.insert() works properly and TreeObject.compareTo()
     * has been implemented.
     *
     * @throws BTreeException Exception thrown when BTree encounters an unexpected problem
     * @throws IOException Exception thrown when testing fails due to IO errors
     */
    @Test
    public void testSearchOneKey() throws BTreeException, IOException {

        String key = "1";
        TreeObject t = new TreeObject(key);

        BTree b = new BTree(2, testFilename);

        b.insert(new TreeObject(key));

        TreeObject obj = b.search(key);

        assertEquals(0, t.compareTo(obj));

    }


    /**
     * More complex search test for searching recursively.
     * Test inserting a duplicate into a node that is not a leaf and has
     * a full child.
     * Assertion is that TreeObject with key 'A' has been found
     *
     * @throws BTreeException
     * @throws IOException
     */
    @Test
    public void testSearchToNotLeaf() throws BTreeException, IOException {

        BTree b = new BTree(2, testFilename); //Different degree than CLRS 18.6!

        b.insert(new TreeObject("A"));
        b.insert(new TreeObject("D"));
        b.insert(new TreeObject("F"));
        b.insert(new TreeObject("H"));
        b.insert(new TreeObject("L"));

        TreeObject obj = b.search("A");

        assertEquals("A", obj.getKey());
    }

    /**
     * TreeObject test that inserts 1 TreeObject and checks that its count is correct.
     *
     * @throws BTreeException Exception thrown when BTree encounters an unexpected problem
     * @throws IOException Exception thrown when testing fails due to IO errors
     */
    @Test
    public void testTreeObjectCount() throws BTreeException, IOException {

        String key = "A";

        BTree b = new BTree(2, testFilename);

        b.insert(new TreeObject(key));

        TreeObject obj = b.search(key);

        assertEquals(1, obj.getCount());
    }

    /**
     * TreeObject test that validates duplicates are counted properly.
     *
     * @throws BTreeException Exception thrown when BTree encounters an unexpected problem
     * @throws IOException Exception thrown when testing fails due to IO errors
     */
    @Test
    public void testCountingTreeObjectDuplicates() throws BTreeException, IOException {

        String duplicateKey = "A";

        BTree b = new BTree(2, testFilename);

        for (int i = 0; i < 10; i++) {
            b.insert(new TreeObject(duplicateKey));
        }

        TreeObject obj = b.search(duplicateKey);

        assertEquals(10, obj.getCount());
    }

    /**
     * TreeObject test of additional constructor.
     */
    @Test
    public void testSettingTreeObjectCount() {
        String key = "A";
        long count = 12;

        TreeObject t = new TreeObject(key, count);

        assertEquals(count, t.getCount());

    }


    /**
     * More complex insert test requiring working Search and duplicate counting.
     * Run a similar test to example 18.6 in the book, except
     * that a duplicate key ('H') is inserted again to the root when
     * it is no longer a leaf node.
     * Assertion is that TreeObject with key 'H' has count = 2.
     *
     * @throws BTreeException Exception thrown when BTree encounters an unexpected problem
     * @throws IOException Exception thrown when testing fails due to IO errors
     */
    @Test
    public void testInsertToNotLeaf() throws BTreeException, IOException {

        BTree b = new BTree(4, testFilename);

        String[] input = new String[]{"A", "D", "F", "H", "L", "N", "P", "B", "H"};

        for (int i = 0; i < input.length - 1; i++) {
            b.insert(new TreeObject(input[i]));
        }

        //by inserting a duplicate into a non leaf node, another branch is tested.
        b.insert(new TreeObject(input[8])); //H

        TreeObject obj = b.search("H");

        assertEquals(2, obj.getCount());

        assertTrue(validateInserts(b, input));
    }


    /**
     * More complex insert test requiring working Search and duplicate counting.
     * Test inserting a duplicate into a node that is not a leaf and has
     * a full child.
     * Assertion is that key 'H' (8) has been counted twice in a search
     *
     * @throws BTreeException Exception thrown when BTree encounters an unexpected problem
     * @throws IOException Exception thrown when testing fails due to IO errors
     */
    @Test
    public void testInsertToNotLeafFullChild() throws BTreeException, IOException {

        BTree b = new BTree(2, testFilename); //Different degree than CLRS 18.6!

        String[] input = new String[]{"A", "D", "F", "H", "L", "H"};

        for (String l : input) {
            b.insert(new TreeObject(l));
        }

        TreeObject obj = b.search("H");

        assertEquals(2, obj.getCount());

        assertTrue(validateInserts(b, input));
    }

    /**
     * Test that verifies the BTree is actually saved to disk as a file.
     * Creates a BTree, inserts some keys, and then verifies:
     * 1. The file exists on disk
     * 2. The file has content (non-zero size)
     * 3. A new BTree can be created from the existing file and contains the same data
     *
     * @throws BTreeException Exception thrown when BTree encounters an unexpected problem
     * @throws IOException Exception thrown when testing fails due to IO errors
     */
    @Test
    public void testBTreePersistsToFile() throws BTreeException, IOException {
        String persistenceTestFile = "persistence_test.btree";
        
        // Clean up any existing test file
        deleteTestFile(persistenceTestFile);
        
        try {
            // Create a BTree and insert some test data
            BTree originalTree = new BTree(3, persistenceTestFile);
            String[] testKeys = {"apple", "banana", "cherry", "date", "elderberry"};
            
            for (String key : testKeys) {
                originalTree.insert(new TreeObject(key));
            }
            
            // Verify the file exists and has content
            File btreeFile = new File(persistenceTestFile);
            assertTrue("BTree file should exist on disk", btreeFile.exists());
            assertTrue("BTree file should not be empty", btreeFile.length() > 0);
            
            // Create a new BTree from the existing file to verify persistence
            BTree loadedTree = new BTree(3, persistenceTestFile);
            
            // Verify the loaded tree has the same properties
            assertEquals("Loaded tree should have same size", originalTree.getSize(), loadedTree.getSize());
            assertEquals("Loaded tree should have same degree", originalTree.getDegree(), loadedTree.getDegree());
            assertEquals("Loaded tree should have same height", originalTree.getHeight(), loadedTree.getHeight());
            
            // Verify all keys can be found in the loaded tree
            for (String key : testKeys) {
                TreeObject foundObj = loadedTree.search(key);
                assertNotNull("Key '" + key + "' should be found in loaded tree", foundObj);
                assertEquals("Key should match", key, foundObj.getKey());
                assertEquals("Count should be 1", 1, foundObj.getCount());
            }
            
            // Verify the keys are in sorted order
            assertTrue("Keys should be properly sorted in loaded tree", validateInserts(loadedTree, testKeys));
            
        } finally {
            // Clean up the test file
            deleteTestFile(persistenceTestFile);
        }
    }

    /**
     * Test that verifies the dumpToFile method properly writes BTree contents to a file.
     * Creates a BTree, inserts various keys (including duplicates), dumps to file,
     * and verifies the output format and content correctness.
     *
     * @throws BTreeException Exception thrown when BTree encounters an unexpected problem
     * @throws IOException Exception thrown when testing fails due to IO errors
     */
    @Test
    public void testDumpToFile() throws BTreeException, IOException {
        String dumpTestFile = "dump_test.txt";
        String btreeTestFile = "dump_btree_test.btree";
        
        // Clean up any existing test files
        deleteTestFile(dumpTestFile);
        deleteTestFile(btreeTestFile);
        
        try {
            // Create a BTree and insert test data with some duplicates
            BTree tree = new BTree(3, btreeTestFile);
            
            // Insert keys in non-alphabetical order to test sorting
            tree.insert(new TreeObject("delta"));
            tree.insert(new TreeObject("alpha"));
            tree.insert(new TreeObject("charlie"));
            tree.insert(new TreeObject("beta"));
            tree.insert(new TreeObject("echo"));
            
            // Insert some duplicates to test counting
            tree.insert(new TreeObject("alpha")); // Should have count 2
            tree.insert(new TreeObject("charlie")); // Should have count 2
            tree.insert(new TreeObject("alpha")); // Should have count 3
            
            // Create PrintWriter and dump to file
            PrintWriter writer = new PrintWriter(dumpTestFile);
            tree.dumpToFile(writer);
            writer.close();
            
            // Verify the dump file was created and has content
            File dumpFile = new File(dumpTestFile);
            assertTrue("Dump file should exist", dumpFile.exists());
            assertTrue("Dump file should not be empty", dumpFile.length() > 0);
            
            // Read the dump file and verify contents
            java.util.Scanner scanner = new java.util.Scanner(dumpFile);
            java.util.List<String> lines = new java.util.ArrayList<>();
            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine());
            }
            scanner.close();
            
            // Should have 5 unique keys
            assertEquals("Should have 5 lines in dump file", 5, lines.size());
            
            // Verify keys are in alphabetical order and counts are correct
            assertEquals("First line should be alpha with count 3", "alpha 3", lines.get(0));
            assertEquals("Second line should be beta with count 1", "beta 1", lines.get(1));
            assertEquals("Third line should be charlie with count 2", "charlie 2", lines.get(2));
            assertEquals("Fourth line should be delta with count 1", "delta 1", lines.get(3));
            assertEquals("Fifth line should be echo with count 1", "echo 1", lines.get(4));
            
            // Verify the format is correct (key space count)
            for (String line : lines) {
                assertTrue("Each line should contain exactly one space", 
                    line.indexOf(' ') != -1 && line.indexOf(' ') == line.lastIndexOf(' '));
                String[] parts = line.split(" ");
                assertEquals("Each line should have exactly 2 parts", 2, parts.length);
                assertTrue("Count should be a positive integer", 
                    Integer.parseInt(parts[1]) > 0);
            }
            
        } finally {
            // Clean up test files
            deleteTestFile(dumpTestFile);
            deleteTestFile(btreeTestFile);
        }
    }

    /**
     * Test that verifies dumpToFile works correctly with an empty BTree.
     *
     * @throws BTreeException Exception thrown when BTree encounters an unexpected problem
     * @throws IOException Exception thrown when testing fails due to IO errors
     */
    @Test
    public void testDumpToFileEmptyTree() throws BTreeException, IOException {
        String dumpTestFile = "empty_dump_test.txt";
        String btreeTestFile = "empty_btree_test.btree";
        
        // Clean up any existing test files
        deleteTestFile(dumpTestFile);
        deleteTestFile(btreeTestFile);
        
        try {
            // Create an empty BTree
            BTree emptyTree = new BTree(2, btreeTestFile);
            
            // Create PrintWriter and dump empty tree to file
            PrintWriter writer = new PrintWriter(dumpTestFile);
            emptyTree.dumpToFile(writer);
            writer.close();
            
            // Verify the dump file was created
            File dumpFile = new File(dumpTestFile);
            assertTrue("Dump file should exist", dumpFile.exists());
            
            // Empty tree should produce an empty dump file
            assertEquals("Empty tree dump file should be empty", 0, dumpFile.length());
            
        } finally {
            // Clean up test files
            deleteTestFile(dumpTestFile);
            deleteTestFile(btreeTestFile);
        }
    }

    /**
     * Test that verifies the dumpToDatabase method properly writes BTree contents to a SQLite database.
     * Creates a BTree, inserts various keys (including duplicates), dumps to database,
     * and verifies the database structure and content correctness.
     *
     * @throws BTreeException Exception thrown when BTree encounters an unexpected problem
     * @throws IOException Exception thrown when testing fails due to IO errors
     * @throws SQLException Exception thrown when database operations fail
     */
    @Test
    public void testDumpToDatabase() throws BTreeException, IOException, SQLException {
        String dbTestFile = "test_database.db";
        String tableName = "btree_data";
        String btreeTestFile = "dump_db_btree_test.btree";
        
        // Clean up any existing test files
        deleteTestFile(dbTestFile);
        deleteTestFile(btreeTestFile);
        
        try {
            // Create a BTree and insert test data with some duplicates
            BTree tree = new BTree(3, btreeTestFile);
            
            // Insert keys in non-alphabetical order to test sorting
            tree.insert(new TreeObject("gamma"));
            tree.insert(new TreeObject("alpha"));
            tree.insert(new TreeObject("beta"));
            tree.insert(new TreeObject("delta"));
            tree.insert(new TreeObject("epsilon"));
            
            // Insert some duplicates to test counting
            tree.insert(new TreeObject("alpha")); // Should have count 2
            tree.insert(new TreeObject("gamma")); // Should have count 2
            tree.insert(new TreeObject("alpha")); // Should have count 3
            tree.insert(new TreeObject("delta")); // Should have count 2
            
            // Dump to database
            tree.dumpToDatabase(dbTestFile, tableName);
            
            // Verify the database file was created
            File dbFile = new File(dbTestFile);
            assertTrue("Database file should exist", dbFile.exists());
            assertTrue("Database file should not be empty", dbFile.length() > 0);
            
            // Connect to database and verify contents
            String url = "jdbc:sqlite:" + dbTestFile;
            try (Connection conn = DriverManager.getConnection(url)) {
                
                // Verify table exists and has correct structure
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'");
                    assertTrue("Table should exist", rs.next());
                    assertEquals("Table name should match", tableName, rs.getString("name"));
                }
                
                // Verify table structure
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + tableName + ")");
                    
                    // Should have exactly 2 columns
                    assertTrue("Should have first column", rs.next());
                    assertEquals("First column should be 'key'", "key", rs.getString("name"));
                    assertEquals("First column should be TEXT", "TEXT", rs.getString("type"));
                    assertEquals("First column should be NOT NULL", 1, rs.getInt("notnull"));
                    
                    assertTrue("Should have second column", rs.next());
                    assertEquals("Second column should be 'frequency'", "frequency", rs.getString("name"));
                    assertEquals("Second column should be INTEGER", "INTEGER", rs.getString("type"));
                    assertEquals("Second column should be NOT NULL", 1, rs.getInt("notnull"));
                    
                    assertFalse("Should have exactly 2 columns", rs.next());
                }
                
                // Verify data content and ordering
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery("SELECT key, frequency FROM " + tableName + " ORDER BY key");
                    
                    // Should have 5 unique keys in alphabetical order
                    assertTrue("Should have alpha", rs.next());
                    assertEquals("First key should be alpha", "alpha", rs.getString("key"));
                    assertEquals("Alpha should have count 3", 3, rs.getInt("frequency"));
                    
                    assertTrue("Should have beta", rs.next());
                    assertEquals("Second key should be beta", "beta", rs.getString("key"));
                    assertEquals("Beta should have count 1", 1, rs.getInt("frequency"));
                    
                    assertTrue("Should have delta", rs.next());
                    assertEquals("Third key should be delta", "delta", rs.getString("key"));
                    assertEquals("Delta should have count 2", 2, rs.getInt("frequency"));
                    
                    assertTrue("Should have epsilon", rs.next());
                    assertEquals("Fourth key should be epsilon", "epsilon", rs.getString("key"));
                    assertEquals("Epsilon should have count 1", 1, rs.getInt("frequency"));
                    
                    assertTrue("Should have gamma", rs.next());
                    assertEquals("Fifth key should be gamma", "gamma", rs.getString("key"));
                    assertEquals("Gamma should have count 2", 2, rs.getInt("frequency"));
                    
                    assertFalse("Should have exactly 5 records", rs.next());
                }
                
                // Verify total record count
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM " + tableName);
                    assertTrue("Should have count result", rs.next());
                    assertEquals("Should have 5 records total", 5, rs.getInt("count"));
                }
            }
            
        } finally {
            // Clean up test files
            deleteTestFile(dbTestFile);
            deleteTestFile(btreeTestFile);
        }
    }

    /**
     * Test that verifies dumpToDatabase works correctly with an empty BTree.
     *
     * @throws BTreeException Exception thrown when BTree encounters an unexpected problem
     * @throws IOException Exception thrown when testing fails due to IO errors
     * @throws SQLException Exception thrown when database operations fail
     */
    @Test
    public void testDumpToDatabaseEmptyTree() throws BTreeException, IOException, SQLException {
        String dbTestFile = "empty_test_database.db";
        String tableName = "empty_btree_data";
        String btreeTestFile = "empty_db_btree_test.btree";
        
        // Clean up any existing test files
        deleteTestFile(dbTestFile);
        deleteTestFile(btreeTestFile);
        
        try {
            // Create an empty BTree
            BTree emptyTree = new BTree(2, btreeTestFile);
            
            // Dump empty tree to database
            emptyTree.dumpToDatabase(dbTestFile, tableName);
            
            // Verify the database file was created
            File dbFile = new File(dbTestFile);
            assertTrue("Database file should exist", dbFile.exists());
            
            // Connect to database and verify empty table
            String url = "jdbc:sqlite:" + dbTestFile;
            try (Connection conn = DriverManager.getConnection(url)) {
                
                // Verify table exists
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'");
                    assertTrue("Table should exist", rs.next());
                }
                
                // Verify table is empty
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM " + tableName);
                    assertTrue("Should have count result", rs.next());
                    assertEquals("Empty tree should produce empty table", 0, rs.getInt("count"));
                }
            }
            
        } finally {
            // Clean up test files
            deleteTestFile(dbTestFile);
            deleteTestFile(btreeTestFile);
        }
    }

    /**
     * Test that verifies dumpToDatabase properly replaces existing table.
     *
     * @throws BTreeException Exception thrown when BTree encounters an unexpected problem
     * @throws IOException Exception thrown when testing fails due to IO errors
     * @throws SQLException Exception thrown when database operations fail
     */
    @Test
    public void testDumpToDatabaseReplaceTable() throws BTreeException, IOException, SQLException {
        String dbTestFile = "replace_test_database.db";
        String tableName = "replace_test_table";
        String btreeTestFile1 = "replace_btree_test1.btree";
        String btreeTestFile2 = "replace_btree_test2.btree";
        
        // Clean up any existing test files
        deleteTestFile(dbTestFile);
        deleteTestFile(btreeTestFile1);
        deleteTestFile(btreeTestFile2);
        
        try {
            // Create first BTree with some data
            BTree tree1 = new BTree(2, btreeTestFile1);
            tree1.insert(new TreeObject("first"));
            tree1.insert(new TreeObject("second"));
            
            // Dump first tree to database
            tree1.dumpToDatabase(dbTestFile, tableName);
            
            // Verify first dump
            String url = "jdbc:sqlite:" + dbTestFile;
            try (Connection conn = DriverManager.getConnection(url)) {
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM " + tableName);
                    assertTrue("Should have count result", rs.next());
                    assertEquals("First dump should have 2 records", 2, rs.getInt("count"));
                }
            }
            
            // Create second BTree with different data
            BTree tree2 = new BTree(2, btreeTestFile2);
            tree2.insert(new TreeObject("third"));
            tree2.insert(new TreeObject("fourth"));
            tree2.insert(new TreeObject("fifth"));
            
            // Dump second tree to same database and table (should replace)
            tree2.dumpToDatabase(dbTestFile, tableName);
            
            // Verify table was replaced, not appended to
            try (Connection conn = DriverManager.getConnection(url)) {
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM " + tableName);
                    assertTrue("Should have count result", rs.next());
                    assertEquals("Second dump should replace first (3 records, not 5)", 3, rs.getInt("count"));
                }
                
                // Verify the content is from the second tree only
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery("SELECT key FROM " + tableName + " ORDER BY key");
                    
                    assertTrue("Should have fifth", rs.next());
                    assertEquals("Should be fifth", "fifth", rs.getString("key"));
                    
                    assertTrue("Should have fourth", rs.next());
                    assertEquals("Should be fourth", "fourth", rs.getString("key"));
                    
                    assertTrue("Should have third", rs.next());
                    assertEquals("Should be third", "third", rs.getString("key"));
                    
                    assertFalse("Should not have any more records", rs.next());
                }
            }
            
        } finally {
            // Clean up test files
            deleteTestFile(dbTestFile);
            deleteTestFile(btreeTestFile1);
            deleteTestFile(btreeTestFile2);
        }
    }


    /**
     * Helper method used to validate that all the keys in the BTree
     * are sorted by using an in order traversal of the tree
     *
     * @param b BTree to validate
     * @return true if there are no keys in the BTree, or if the keys are indeed in sorted order.
     *
     */
	@SuppressWarnings("unused")
    private boolean validateSearchTreeProperty(BTree b) throws IOException {

        String[] keys = b.getSortedKeyArray();

        /*if there are no keys, the tree is valid
            Beware, if keys have indeed been inserted but getKeysInOrder is not,
            this method will return true
        */
        if (keys == null | keys.length == 0) {
            return true;
        }

        String prev = keys[0];

        for (int i = 1; i < keys.length; i++) {
            if (prev.compareTo(keys[i]) > 0) {
                return false;
            }
        }
        return true;
    }


    /**
     * Checks BTree b against an array of keys to ensure that they were inserted into the BTree.
     * The important check here is that no keys are being lost during complex
     * operations such as splitting children in the tree.
     *
     * Also used to validate that all the keys in the BTree
     * are sorted by using an in order traversal of the tree
     *
     * IMPORTANT: handles duplicates in inputKeys by removing them.
     *
     * @return true if BTree in order traversal matches provide input
     */
    private boolean validateInserts(BTree b, String[] inputKeys) throws IOException {

        String[] bTreeKeys = b.getSortedKeyArray();

        //input may be unsorted
        Arrays.sort(inputKeys);

        //track input as a dynamic set to easily remove duplicates
        ArrayList<String> inputNoDuplicates = new ArrayList<>(inputKeys.length);

        //Copy with excluding duplicates
        for (int i = 0; i < inputKeys.length; i++) {

            if (i > 0) {
                //only add an element if it is different from the previous iteration.
                if (!inputKeys[i - 1].equals(inputKeys[i])) {
                    inputNoDuplicates.add(inputKeys[i]);
                }
            } else {
                inputNoDuplicates.add(inputKeys[i]);
            }
        }

        if (bTreeKeys.length != inputNoDuplicates.size()) {
            //if input and output arrays are different sizes, they can't be equal
            return false;
        }

        String prev = bTreeKeys[0];

        for (int i = 0; i < bTreeKeys.length; i++) {
            if (!bTreeKeys[i].equals(inputNoDuplicates.get(i))) {
                return false;
            }

            if (i > 0 && prev.compareTo(bTreeKeys[i]) > 0) {
                return false;
            }
        }

        return true;
    }


    /**
     * Utility method to help cleanup the system after unit testing
     *
     * @param filename - the file to delete from the system
     *                 WARNING: deletion is unchecked. Only directories are
     *                 prevented from being deleted.
     *                 Pass a filename only to a file that should be deleted
     *                 or could be restored.
     */
    private static void deleteTestFile(String filename) {
        File file = new File(filename);
        if (file.exists() && !file.isDirectory()) {
            System.out.println("Deleting " + filename);
            file.delete();
        }
    }
}
