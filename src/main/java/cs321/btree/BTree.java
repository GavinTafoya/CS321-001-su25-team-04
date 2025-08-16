package cs321.btree;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;

// addition of the cache to improve speed
import cs321.cache.Cache;
import cs321.cache.KeyInterface;

public class BTree implements BTreeInterface {

    //------------------------------------------------------------------
    // Private Node Class
    //------------------------------------------------------------------

    /**
     * Inner class to represent a binary search tree node.
     *
     */
    private class Node implements KeyInterface<Long> {
        private long address; //8 bytes
        private long parent; //8 bytes
        private int numKeys; //4 bytes
        private boolean isLeaf; //1 byte
        private TreeObject[] keys;
        private long[] childPointers;

        /**
         * Constructor for a node.
         */
        public Node(TreeObject key) {
            parent = 0;
            keys = new TreeObject[2 * degree - 1];
            keys[0] = key;
            childPointers = new long[2 * degree];
            numKeys = 0;
            isLeaf = true;
            address = nextDiskAddress;
            nextDiskAddress += nodeSize;
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            String string = "Node:  keys = ";
            for (TreeObject key : keys) {
                string += key.toString() + "  ";
            }
            return string;
        }

        /**
         * This method constructs a non alocating Node
         * */
        private Node() {
            parent = 0;
            keys = new TreeObject[2 * degree - 1];
            childPointers = new long[2 * degree];
            numKeys = 0;
            isLeaf = true;
        }

        @Override
        public Long getKey() {
            return address;
        }

    } // end of Private Node Class

    //------------------------------------------------------------------
    // Variables
    //------------------------------------------------------------------
    private long size;
    private int height;
    private int degree;
    private int METADATA_SIZE = 2 * Long.BYTES; // rootAddress + size
    private long nextDiskAddress;
    private FileChannel file;
    private ByteBuffer buffer;
    private int nodeSize;

    private long rootAddress;
    private Node root;

    // Cache (Project 1) — configurable via flags
    private boolean useCache;               // false when disabled
    private Cache<Long, Node> cache;        // null when disabled


    private static final int DEFAULT_DEGREE = 25;



    //------------------------------------------------------------------
    // Constructor
    //------------------------------------------------------------------

    /**
     * Creates an empty BTree
     */
    public BTree(String name){
        this(2, name);
    }

    public BTree(int degree, String name) {
        this.degree = degree;
        if (degree == 0) {
            this.degree = DEFAULT_DEGREE;
        } else if (degree < 2) {
            throw new IllegalArgumentException("Degree must be at least 2 (or 0 for default).");
        } else {
            this.degree = degree;
        }
        this.size = 0;
        this.height = 0;
        this.useCache = false;
        this.cache = null;

        nodeSize = Integer.BYTES + ((2 * this.degree) - 1) * TreeObject.BYTES + 1 + Long.BYTES + (2 * this.degree) * Long.BYTES + Long.BYTES;
        buffer = ByteBuffer.allocateDirect(nodeSize);
        File tempFile = new File(name);

        try {
            if (!tempFile.exists()) {
                tempFile.createNewFile();
                RandomAccessFile randomAccessFile = new RandomAccessFile(tempFile, "rw");
                file = randomAccessFile.getChannel();
                nextDiskAddress = METADATA_SIZE;
                root = new Node(null);
                rootAddress = root.address;
                diskWrite(root);
                writeMetaData();
            }
            else {
                RandomAccessFile randomAccessFile = new RandomAccessFile(tempFile, "rw");
                file = randomAccessFile.getChannel();
                readMetaData();
                root = diskRead(rootAddress);
                nextDiskAddress = Math.max(file.size(), METADATA_SIZE);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BTree(int degree, String name, boolean useCache, int cacheSize) {
        this(degree, name);
        this.useCache = useCache;
        this.cache = useCache ? new cs321.cache.Cache<>(cacheSize) : null;
    }

    public void printCacheStats(java.io.PrintStream out) {
        if (useCache && cache != null) {
            out.println(cache.toString());
        }
    }

    //------------------------------------------------------------------
    // Exception
    //------------------------------------------------------------------

    /**
     * Write an exception to prevent the user form writing a BTree of degree 1
     * Otherwise we have a BTree with zero keys at t - 1 = keys.
     *
     * Write the Exception in the BTreeException Class
     */


    //------------------------------------------------------------------
    // Methods
    //------------------------------------------------------------------

    /**
     * This is the total size of the BTree
     * @return Returns the number of keys in the BTree.
     */
    @Override
    public long getSize() {return size;}

    /**
     * Here is the degree
     * @return The degree of the BTree.
     */
    @Override
    public int getDegree() {return degree;}

    /**
     * This calls a recursive helper function to calculate the number of nodes from the root
     * @return Returns the number of nodes in the BTree.
     */
    @Override
    public long getNumberOfNodes() {return countNodes(root);}

    /**
     * Calls a recursive helper function to calculate the height of the tree
     * @return The height of the BTree
     */
    @Override
    public int getHeight() {
        return calculateHeight(root);
    }

    /**
     * Returns all keys currently stored in the B-tree in a increasing order
     *
     * @return a non-null array of keys in sorted order and the array is empty if the tree has no keys
     */
    public String[] getSortedKeyArray() {
        ArrayList<String> out = new ArrayList<>();
        inOrder(root, out);
        return out.toArray(new String[0]);
    }


    /**
     * Insert a given SSH key into the B-Tree. If the key already exists in the B-Tree,
     * the frequency count is incremented. Otherwise, a new node is inserted
     * following the B-Tree insertion algorithm.
     *
     * @param obj A TreeObject representing an SSH key.
     */
    @Override
    public void insert(TreeObject obj) throws IOException {
        // If the root is empty, set the object to be inserted to be the root
        if (root.numKeys == 0) {
            root.keys[0] = obj;
            root.numKeys = 1;
            root.isLeaf = true;
            size = 1;
            diskWrite(root);
            return; // Exit
        }

        // If root is full, split it by number of degrees first to allow a non-full insertion
        if (root.numKeys == 2 * degree - 1) {
            Node newRoot = new Node(null);
            newRoot.isLeaf = false; // We have to set the newRoot.isLeaf to be false so it can have children for insertion
            newRoot.childPointers[0] = root.address;
            diskWrite(newRoot);
            splitChild(newRoot, 0);
            root = newRoot;
            rootAddress = root.address;
            writeMetaData();
        }
        boolean insertedNew = insertNonFull(root, obj);
        if (insertedNew){
            size++;
            writeMetaData(); // Update metadata with new size
        }
    }


        /**
     * Print out all objects in the given BTree in an inorder traversal to a file.
     *
     * @param printWriter PrintWriter Object for handling output to a file.
     */
    @Override
    public void dumpToFile(PrintWriter printWriter) {
        if (root == null) {
            return;
        }
        dumpInOrder(root, printWriter);
    }

    /**
     * Helper method to perform in-order traversal and dump keys with frequencies to file.
     *
     * @param node        the current node to traverse
     * @param printWriter the PrintWriter to write to
     */
    private void dumpInOrder(Node node, PrintWriter printWriter) {
        if (node == null) return;
        
        for (int i = 0; i < node.numKeys; i++) {
            // Visit left child if not a leaf
            if (!node.isLeaf) {
                dumpInOrder(diskRead(node.childPointers[i]), printWriter);
            }
            
            // Process current key
            TreeObject treeObj = node.keys[i];
            printWriter.println(treeObj.getKey() + " " + treeObj.getCount());
        }
        
        // Visit rightmost child if not a leaf
        if (!node.isLeaf) {
            dumpInOrder(diskRead(node.childPointers[node.numKeys]), printWriter);
        }
    }

    /**
     * Dump out all objects in the given BTree in an inorder traversal to a table in the database.
     * <p>
     * If the database does not exist, then it is created and the table is added.
     * <p>
     * If the provided database already exists, then the table is added. If the table already exists,
     * then the table is replaced.
     *
     * @param dbName    String referring to the name of the database.
     * @param tableName String referring to the table of the database.
     */
    @Override
    public void dumpToDatabase(String dbName, String tableName) throws IOException {
        if (root == null) {
            return;
        }

        String url = "jdbc:sqlite:" + dbName;
        
        try (Connection conn = DriverManager.getConnection(url)) {
            // Create table (replacing if it exists) - wrap table name in quotes for safety
            String quotedTableName = "\"" + tableName + "\"";
            String createTableSQL = "DROP TABLE IF EXISTS " + quotedTableName + "; " +
                                  "CREATE TABLE " + quotedTableName + " (" +
                                  "key TEXT NOT NULL, " +
                                  "frequency INTEGER NOT NULL" +
                                  ");";
            
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(createTableSQL);
            }
            
            // Insert data using in-order traversal
            String insertSQL = "INSERT INTO " + quotedTableName + " (key, frequency) VALUES (?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                dumpInOrderToDatabase(root, pstmt);
            }

        } catch (SQLException e) {
            throw new IOException("Database error: " + e.getMessage(), e);
        }
    }

    /**
     * Helper method to perform in-order traversal and dump keys with frequencies to database.
     *
     * @param node  the current node to traverse
     * @param pstmt the PreparedStatement for database insertion
     * @throws SQLException if database operation fails
     */
    private void dumpInOrderToDatabase(Node node, PreparedStatement pstmt) throws SQLException {
        if (node == null) return;
        
        for (int i = 0; i < node.numKeys; i++) {
            // Visit left child if not a leaf
            if (!node.isLeaf) {
                dumpInOrderToDatabase(diskRead(node.childPointers[i]), pstmt);
            }
            
            // Process current key
            TreeObject treeObj = node.keys[i];
            pstmt.setString(1, treeObj.getKey());
            pstmt.setLong(2, treeObj.getCount());
            pstmt.executeUpdate();
        }
        
        // Visit rightmost child if not a leaf
        if (!node.isLeaf) {
            dumpInOrderToDatabase(diskRead(node.childPointers[node.numKeys]), pstmt);
        }
    }

    /**
     * Searches for a key in the given BTree.
     *
     * @param key The key value to search for.
     */
    @Override
    public TreeObject search(String key) throws IOException {
        Node currentNode = root;
        while (currentNode != null) {
            int position = Arrays.binarySearch(currentNode.keys, 0, currentNode.numKeys, new TreeObject(key));
            if (position >= 0) {
                return currentNode.keys[position]; // Key found, return the TreeObject
            }
            position = -position - 1; // Get the insertion point
            if (currentNode.isLeaf) {
                break; // Key not found in a leaf node
            }
            // Move to the appropriate child pointer
            long childAddress = currentNode.childPointers[position];
            currentNode = diskRead(childAddress);
        }

        return null; // Key not found
    }

    /**
     * Deletes a key from the BTree. Not Implemented.
     * We do not need this completed
     * @param key the key to be deleted
     */
    @Override
    public void delete(String key) {

    }

    //------------------------------------------------------------------
    // Private Helper Methods
    //------------------------------------------------------------------

    private Node Successor(Node S){
        if (S.isLeaf) {
            return null; // No successor for leaf nodes
        }
        // Traverse to the leftmost child of the right subtree
        Node current = diskRead(S.childPointers[0]);
        while (!current.isLeaf) {
            current = diskRead(current.childPointers[0]);
        }
        return current;
    }

    private Node Predecessor(Node P){
        if (P.isLeaf) {
            return null; // No predecessor for leaf nodes
        }
        // Traverse to the rightmost child of the left subtree
        Node current = diskRead(P.childPointers[P.numKeys]);
        while (!current.isLeaf) {
            current = diskRead(current.childPointers[current.numKeys]);
        }
        return current;
    }

    private Node TreeMinimum(Node S){
        if (S.isLeaf) {
            return S;
        }
        // Traverse to the leftmost child
        Node current = diskRead(S.childPointers[0]);
        while (!current.isLeaf) {
            current = diskRead(current.childPointers[0]);
        }
        return current;
    }

    private Node TreeMaximum(Node S){
        if (S.isLeaf) {
            return S;
        }
        // Traverse to the rightmost child
        Node current = diskRead(S.childPointers[S.numKeys]);
        while (!current.isLeaf) {
            current = diskRead(current.childPointers[current.numKeys]);
        }
        return current;
    }

    private void Transplant(Node u, Node v){
        throw new UnsupportedOperationException("Transplant method is not implemented yet.");
    }

    /**
     * This is a recursive method that finds the height of the BTree
     * @param node
     * @return maxHeight + 1
     */
    private int calculateHeight(Node node){
        if (node == null) {return 0;}

        // If the node is a leaf with no keys, height is 0 (empty tree)
        if (node.isLeaf && node.numKeys == 0) {return 0;}

        // If the node is a leaf with keys, height is 0 (single level)
        if (node.isLeaf) {return 0;}

        // For internal nodes, follow the first child and add 1
        Node firstChild = diskRead(node.childPointers[0]);
        return calculateHeight(firstChild) + 1;
    }

    /**
     * Counts all the nodes in the B tree starting from the given node
     * and checks all the child nodes then adds them to the count
     *
     * @param node the node to start the counting from
     * @return the number of nodes in the subtree
     */
    private long countNodes(Node node) {
        if (node == null) {
            return 0;
        }
        long count = 1;
        if (!node.isLeaf) {
            for (int i = 0; i <= node.numKeys; i++) {
                Node child = diskRead(node.childPointers[i]);
                count += countNodes(child);
            }
        }
        return count;
    }


    /**
     * Inserts the key into subtree rooted at Node x which isnt full
     * It then splits a full child while going down
     *
     * @param x node to go down from
     * @param k key to insert
     * @return true if a new key was added else return false if count is increased
     * @throws IOException for the disk IO errors
     */
    private boolean insertNonFull(Node x, TreeObject k) throws IOException {

        for (int j = 0; j < x.numKeys; j++) {
            int cmp = k.compareTo(x.keys[j]);
            if (cmp == 0) {
                x.keys[j].incCount();
                diskWrite(x);
                return false;
            }
            if (x.isLeaf && cmp < 0) break;
        }

        if (x.isLeaf) {

            int i = x.numKeys - 1;
            while (i >= 0 && k.compareTo(x.keys[i]) < 0) {
                x.keys[i + 1] = x.keys[i];
                i--;
            }
            x.keys[i + 1] = k;
            x.numKeys++;
            diskWrite(x);
            return true;
        }
        int i = x.numKeys - 1;
        while (i >= 0 && k.compareTo(x.keys[i]) < 0) i--;
        int childIdx = i + 1;

        Node child = diskRead(x.childPointers[childIdx]);

        if (child != null && child.numKeys == 2 * degree - 1) {
            splitChild(x, childIdx, child);   // promotes median into x

            int cmpMid = k.compareTo(x.keys[childIdx]);
            if (cmpMid == 0) {
                x.keys[childIdx].incCount();
                diskWrite(x);
                return false;
            }
            if (cmpMid > 0){
                childIdx++;
            }
            child = diskRead(x.childPointers[childIdx]);
        }
        return insertNonFull(child, k);
    }


    /**
     * Splits the full child at childIndex of parent
     * *
     * @param parent parent node
     * @param childIndex index of the full child to split
     */
    private void splitChild(Node parent, int childIndex) {
        Node fullChild = diskRead(parent.childPointers[childIndex]);
        splitChild(parent, childIndex, fullChild);
    }

    /**
     * Splits the full child at parent.children[childIndex] and promotes the median key in the parent
     *
     * @param parent the node whose child is being split
     * @param childIndex index of the full child within parent.children
     */
    private void splitChild(Node parent, int childIndex, Node fullChild) {
        Node newSibling = new Node(null);
        newSibling.isLeaf = fullChild.isLeaf;
        newSibling.parent = parent.address;
        TreeObject median = fullChild.keys[degree - 1];
        newSibling.numKeys = degree - 1;

        System.arraycopy(fullChild.keys, degree, newSibling.keys, 0, degree - 1);
        if (!fullChild.isLeaf) {
            System.arraycopy(fullChild.childPointers, degree, newSibling.childPointers, 0, degree);
            Arrays.fill(fullChild.childPointers, degree, degree * 2, 0L);
        }

        // clear moved keys in old child
        Arrays.fill(fullChild.keys, degree - 1, 2 * degree - 1, null);
        fullChild.numKeys = degree - 1;

        // Make space for new child pointer in parent
        System.arraycopy(parent.childPointers, childIndex + 1, parent.childPointers, childIndex + 2, parent.numKeys - childIndex);
        parent.childPointers[childIndex + 1] = newSibling.address;

        // Make space for new key in parent
        System.arraycopy(parent.keys, childIndex, parent.keys, childIndex + 1, parent.numKeys - childIndex);
        parent.keys[childIndex] = median;
        parent.numKeys++;

        diskWrite(fullChild);
        diskWrite(newSibling);
        diskWrite(parent);

    }

    /**
     * Performs a traversal in order starting at node and appends each keys String value to output
     *
     * @param node    the node to traverse
     * @param output  accumulator list that receives the keys encountered during traversal
     */
    private void inOrder(Node node, ArrayList<String> output) {
        if (node == null) return;
        for (int i = 0; i < node.numKeys; i++) {
            if (!node.isLeaf) inOrder(diskRead(node.childPointers[i]), output);
            output.add(node.keys[i].getKey());
        }

        if (!node.isLeaf) inOrder(diskRead(node.childPointers[node.numKeys]), output);
    }

    /**
     * Writes the metadata of the BTree to the file.
     */
    private void writeMetaData() {
        try {
            // Position the file channel to the start of the file
            file.position(0);

            // Write the root address and size to the metadata
            ByteBuffer tmpbuffer = ByteBuffer.allocateDirect(METADATA_SIZE);

            tmpbuffer.clear();
            tmpbuffer.putLong(rootAddress);
            tmpbuffer.putLong(size);

            tmpbuffer.flip();
            file.write(tmpbuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads the metadata of the BTree from the file.
     */
    private void readMetaData() {
        try {
            // Position the file channel to the start of the file
            file.position(0);

            // Allocate a temporary buffer to read the metadata
            ByteBuffer tmpbuffer = ByteBuffer.allocateDirect(METADATA_SIZE);

            tmpbuffer.clear();
            file.read(tmpbuffer);

            // Flip the buffer to prepare it for reading
            tmpbuffer.flip();
            rootAddress = tmpbuffer.getLong();
            size = tmpbuffer.getLong();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads a node from disk at the specified address.
     * @param diskAddress the address of the node on disk
     * @return node read from disk, or null if the address is 0 (indicating no node)
     */
    private Node diskRead(long diskAddress) {

        // If the disk address is 0, return null (indicating no node)
        if(diskAddress == 0) {
            return null;
        }

        if (useCache) {
            Node hit = cache.get(diskAddress);
            if (hit != null) {
                return hit;
            }
        }

        // Position the file channel to the disk address
        try {
            file.position(diskAddress);

            buffer.clear();

            int n;
            try {
                n = file.read(buffer);
            } catch (IOException e) { n = -1; }
            if (n < nodeSize) {
                throw new RuntimeException(new IOException("Short read @"+diskAddress+" got "+n+" of "+nodeSize));
            }
            buffer.flip();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create a new Node and read its properties from the buffer
        Node tempNode = new Node();

        // Read the number of keys
        tempNode.numKeys = buffer.getInt();

        // Read the key and counts
        for (int i = 0; i < (2 * degree) - 1; i++) {
            byte[] readBytes = new byte[TreeObject.BYTES - Long.BYTES];
            buffer.get(readBytes);

            int actualLen = readBytes.length;
            for (int j = 0; j < readBytes.length - 1; j += 2) {
                if (readBytes[j] == 0 && readBytes[j + 1] == 0) {
                    actualLen = j;
                    break;
                }
            }

            long count = buffer.getLong();
            if (actualLen > 0) {
                String string = new String(readBytes, 0, actualLen, StandardCharsets.UTF_16BE);
                tempNode.keys[i] = new TreeObject(string, count);
            } else {
                tempNode.keys[i] = null;
            }
        }

        // Read whether the node is a leaf
        tempNode.isLeaf = buffer.get() == 1;

        // Read the parent address
        tempNode.parent = buffer.getLong();

        // Read the child pointers
        for (int i = 0; i < 2 * degree; i++) {
            tempNode.childPointers[i] = buffer.getLong();
        }

        // Set the address of the node
        tempNode.address = diskAddress;
        buffer.getLong();

        if (useCache) {
            cache.add(tempNode);
        }

        return tempNode;
    }

    /**
     * Writes the node to disk at its address.
     * @param x the node to write to disk
     */
    private void diskWrite(Node x) {
        try {
            file.position(x.address);

            buffer.clear();

            // Write the number of keys
            buffer.putInt(x.numKeys);

            // Write the keys and counts
            for (int i = 0; i < (2 * degree) - 1; i++) {
                TreeObject key = x.keys[i];
                // If the key is null, write empty bytes
                if (key == null) {
                    for (int j = 0; j < (TreeObject.BYTES - Long.BYTES); j++) {
                        buffer.put((byte) 0);
                    }
                    buffer.putLong(0);
                }
                // If the key is not null, write the key and count
                else {
                    byte[] charset = key.getKey().getBytes(StandardCharsets.UTF_16BE);
                    byte[] paddedKey = Arrays.copyOf(charset, TreeObject.BYTES - Long.BYTES);
                    buffer.put(paddedKey);
                    buffer.putLong(key.getCount());
                }
            }

            // Write whether the node is a leaf
            if (x.isLeaf) {
                buffer.put((byte) 1);
            } else {
                buffer.put((byte) 0);
            }

            // Write the parent address
            buffer.putLong(x.parent);

            // Write the child pointers
            for (int i = 0; i < 2 * degree; i++) {
                if (x.childPointers[i] == 0) {
                    buffer.putLong(0);
                }
                else {
                    buffer.putLong(x.childPointers[i]);
                }
            }

            buffer.putLong(x.address); // Write the address of the node

            buffer.flip();

            // Write the buffer to the file
            file.write(buffer);

            if (useCache) {
                cache.add(x);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}