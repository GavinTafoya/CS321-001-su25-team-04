package cs321.btree;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class BTree implements BTreeInterface {

    //------------------------------------------------------------------
    // Private Node Class
    //------------------------------------------------------------------

    /**
     * Inner class to represent a binary search tree node.
     *
     */
    private class Node {
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
    } // end of Private Node Class

    //------------------------------------------------------------------
    // Variables
    //------------------------------------------------------------------
    private long size;
    private int height;
    private int degree;
    private int METADATA_SIZE = Long.BYTES;
    private long nextDiskAddress;
    private FileChannel file;
    private ByteBuffer buffer;
    private int nodeSize;

    private long rootAddress;
    private Node root;


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
        this.root = new Node(null);
        this.size = 0;
        this.height = 0;

        nodeSize = Long.BYTES * 3 + 1 + Integer.BYTES + (2 * this.degree - 1) * TreeObject.BYTES + (2 * this.degree) * Long.BYTES;
        buffer = ByteBuffer.allocateDirect(nodeSize);
        File tempFile = new File(name);

        try {
            if (!tempFile.exists()) {
                tempFile.createNewFile();
                RandomAccessFile randomAccessFile = new RandomAccessFile(tempFile, "rw");
                file = randomAccessFile.getChannel();
                nextDiskAddress = root.address = METADATA_SIZE;
                writeMetaData();
            }
            else {
                RandomAccessFile randomAccessFile = new RandomAccessFile(tempFile, "rw");
                file = randomAccessFile.getChannel();
                readMetaData();
                root = diskRead(rootAddress);
                nextDiskAddress = root.address + nodeSize;
            }
            
        } catch (IOException e) {
            e.printStackTrace();
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
            return; // Exit
        }
        
        // If root is full, split it by number of degrees first to allow a non-full insertion
        if (root.numKeys == 2 * degree - 1) {
            Node newRoot = new Node(obj);
            newRoot.isLeaf = false; // We have to set the newRoot.isLeaf to be false so it can have children for insertion
            newRoot.childPointers[0] = root.address;
            splitChild(newRoot, 0);
            root = newRoot;
        }

        insertInNodeWithSpace(root, obj);
        size++;
    }


    /**
     * Print out all objects in the given BTree in an inorder traversal to a file.
     *
     * @param out PrintWriter object representing output.
     */
    @Override
    public void dumpToFile(PrintWriter out) throws IOException {

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
            int position = Arrays.binarySearch(currentNode.keys, 0, currentNode.numKeys, key);
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
     * Inserts key into the node with space
     *
     * @param node the B-tree node that currently has fewer than 2t-1
     * @param key  the  to insert
     */
    private void insertInNodeWithSpace(Node node, TreeObject key) {
        int i = node.numKeys - 1;

        if (node.isLeaf) {
            // Find position and insert key
            while (i >= 0 && key.compareTo(node.keys[i]) < 0) {
                node.keys[i + 1] = node.keys[i];
                i--;
            }

            // Check for duplicate
            if (i >= 0 && key.compareTo(node.keys[i]) == 0) {
                node.keys[i].incCount();
                size--; // Don't count duplicates toward size
                return;
            }

            node.keys[i + 1] = key;
            node.numKeys++;
        } else {
            // Find child to insert into
            while (i >= 0 && key.compareTo(node.keys[i]) < 0) {
                i--;
            }

            // Check for duplicate in current node
            if (i >= 0 && key.compareTo(node.keys[i]) == 0) {
                node.keys[i].incCount();
                size--; // Don't count duplicates toward size
                return;
            }

            i++; // Move to correct child index

            // Create child if it doesn't exist
            if (diskRead(node.childPointers[i]) == null) {
                node.childPointers[i] = new Node(null).address;
            }

            // Check if child is full
            if (diskRead(node.childPointers[i]).numKeys == 2 * degree - 1) {
                splitChild(node, i);
                if (key.compareTo(node.keys[i]) > 0) {
                    i++;
                }
            }

            insertInNodeWithSpace(diskRead(node.childPointers[i]), key);
        }
    }

    /**
     * Splits the full child at parent.children[childIndex] and promotes the median key in the parent
     *
     * @param parent the node whose child is being split
     * @param childIndex index of the full child within parent.children
     */
    private void splitChild(Node parent, int childIndex) {
        Node fullChild = diskRead(parent.childPointers[childIndex]);
        Node newSibling = new Node(null);

        newSibling.isLeaf = fullChild.isLeaf;
        newSibling.numKeys = degree - 1;

        // Copy the larger half of keys to new sibling
        System.arraycopy(fullChild.keys, degree, newSibling.keys, 0, degree - 1);

        // Copy children if not a leaf
        if (!fullChild.isLeaf) {
            System.arraycopy(fullChild.childPointers, degree, newSibling.childPointers, 0, degree);
        }

        // Reduce the number of keys in full child
        fullChild.numKeys = degree - 1;

        // Make space for new child pointer in parent
        System.arraycopy(parent.childPointers, childIndex + 1, parent.childPointers, childIndex + 2, parent.numKeys - childIndex);
        parent.childPointers[childIndex + 1] = newSibling.address;

        // Make space for new key in parent
        System.arraycopy(parent.keys, childIndex, parent.keys, childIndex + 1, parent.numKeys - childIndex);
        parent.keys[childIndex] = fullChild.keys[degree - 1];
        parent.numKeys++;

        // Clear the promoted key from the full child
        fullChild.keys[degree - 1] = null;
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

            // Write the root address to the metadata
            ByteBuffer tmpbuffer = ByteBuffer.allocateDirect(METADATA_SIZE);

            
            tmpbuffer.clear();
            tmpbuffer.putLong(rootAddress);

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

        // Position the file channel to the disk address
        try {
            file.position(diskAddress);

            buffer.clear();

            file.read(buffer);
            buffer.flip();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create a new Node and read its properties from the buffer
        Node tempNode = new Node(null);

        // Read the number of keys
        tempNode.numKeys = buffer.getInt();

        // Read the key and counts
        for (int i = 0; i < (2 * degree) - 1; i++) {
            byte[] readBytes = new byte[TreeObject.BYTES - Long.BYTES];
            buffer.get(readBytes);
            String string = new String(readBytes, StandardCharsets.UTF_16).trim();
            tempNode.keys[i] = new TreeObject(string, buffer.getLong());
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
                    byte[] charset = key.getKey().getBytes(StandardCharsets.UTF_16);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}