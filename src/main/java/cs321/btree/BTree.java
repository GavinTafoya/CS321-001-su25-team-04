package cs321.btree;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
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
        private long parent; //8 bytes
        private long address; //8 bytes
        private boolean isLeaf; //1 byte
        private int numKeys; //4 bytes
        private TreeObject[] keys; //64 + 8 bytes
        private long[] childPointers;
        private Node[] children;

        /**
         * Constructor for a node.
         */
        public Node() {
            parent = 0;
            keys = new TreeObject[2 * degree - 1];
            childPointers = new long[2 * degree];
            numKeys = 0;
            isLeaf = true;
            children = new Node[2 * degree];
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
    private int height, degree;
    private int METADATA_SIZE = Long.BYTES;
    private long nextDiskAddress = METADATA_SIZE;
    private FileChannel file;
    private ByteBuffer buffer;
    private int nodeSize;

    private long rootAddress = METADATA_SIZE;
    private Node root;


    //------------------------------------------------------------------
    // Constructor
    //------------------------------------------------------------------

    /**
     * Creates an empty BTree
     */
    public BTree(String name){
        this.degree = 2;
        this.root  = new Node();
        this.size = 0;
        this.height = 0;
    }

    public BTree(int degree, String name) {
        this.degree = degree;
        this.root = new Node();
        this.size = 0;
        this.height = 0;
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
            Node newRoot = new Node();
            newRoot.isLeaf = false; // We have to set the newRoot.isLeaf to be false so it can have children for insertion
            newRoot.children[0] = root;
            splitChild(newRoot, 0);
            root = newRoot;
        }
        
        insertNonFull(root, obj);
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
        Node firstChild = node.children[0];
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
                Node child = node.children[i];
                count += countNodes(child);
            }
        }
        return count;
    }


    /**
     * Inserts key into a non-full node
     *
     * @param node the B-tree node that currently has fewer than 2t-1 keys
     * @param key  the key to insert
     */
    private void insertNonFull(Node node, TreeObject key) {
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
            if (node.children[i] == null) {
                node.children[i] = new Node();
            }

            // Check if child is full
            if (node.children[i].numKeys == 2 * degree - 1) {
                splitChild(node, i);
                if (key.compareTo(node.keys[i]) > 0) {
                    i++;
                }
            }

            insertNonFull(node.children[i], key);
        }
    }

    /**
     * Inserts key into the node with space
     *
     * @param node the B-tree node that currently has fewer than 2t-1
     * @param key  the  to insert
     */
    private void insertInNodeWithSpace(Node node, TreeObject key) {
        if (node.isLeaf) {
            int position = Arrays.binarySearch(node.keys, 0, node.numKeys, key);
            position = -position - 1;
            System.arraycopy(node.keys, position, node.keys, position + 1, node.numKeys - position);
            node.keys[position] = key;
            node.numKeys++;
            return;
        }
        int position = Arrays.binarySearch(node.keys, 0, node.numKeys, key);
        position = -position - 1;

        Node tempNode = diskRead(node.childPointers[position]);

        if (tempNode.numKeys == 2 * degree - 1) {
            splitChild(node, position);
            if (key.compareTo(node.keys[position]) > 0){
                position++;
            }
        }
        insertInNodeWithSpace(tempNode, key);
    }

    /**
     * Splits the full child at parent.children[childIndex] and promotes the median key in the parent
     *
     * @param parent the node whose child is being split
     * @param childIndex index of the full child within parent.children
     */
    private void splitChild(Node parent, int childIndex) {
        Node fullChild = parent.children[childIndex];
        Node newSibling = new Node();

        newSibling.isLeaf = fullChild.isLeaf;
        newSibling.numKeys = degree - 1;

        // Copy the larger half of keys to new sibling
        System.arraycopy(fullChild.keys, degree, newSibling.keys, 0, degree - 1);

        // Copy children if not a leaf
        if (!fullChild.isLeaf) {
            System.arraycopy(fullChild.children, degree, newSibling.children, 0, degree);
        }

        // Reduce the number of keys in full child
        fullChild.numKeys = degree - 1;

        // Make space for new child pointer in parent
        System.arraycopy(parent.children, childIndex + 1, parent.children, childIndex + 2, parent.numKeys - childIndex);
        parent.children[childIndex + 1] = newSibling;

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
            if (!node.isLeaf) inOrder(node.children[i], output);
            output.add(node.keys[i].getKey());
        }

        if (!node.isLeaf) inOrder(node.children[node.numKeys], output);
    }


    private Node diskRead(long diskAddress) {
        if(diskAddress == 0) {
            return null;
        }

        try {
            file.position(diskAddress);

            buffer.clear();

            file.read(buffer);
            buffer.flip();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Node tempNode = new Node();

        tempNode.numKeys = buffer.getInt();

        for (int i = 0; i < (2 * degree) - 1; i++) {
            String string = "";
            for (int j = 0; j < (TreeObject.BYTES - Long.BYTES) / Long.BYTES; j++) {
                string += (char) buffer.getLong();
            }
            tempNode.keys[i] = new TreeObject(string, buffer.getLong());
        }

        tempNode.isLeaf = buffer.get() == 1;

        tempNode.parent = buffer.getLong();

        for (int i = 0; i < 2 * degree; i++) {
            tempNode.childPointers[i] = buffer.getLong();
        }

        tempNode.address = diskAddress;

        return tempNode;
    }

    private void diskWrite(Node x) {
        try {
            file.position(x.address);

            buffer.clear();

            buffer.putInt(x.numKeys);

            for (int i = 0; i < (2 * degree) - 1; i++) {
                TreeObject key = x.keys[i];
                if (key == null) {
                    for (int j = 0; j < (TreeObject.BYTES) / Long.BYTES; j++) {
                        buffer.putLong(0);
                        buffer.putLong(0);
                    }
                } else {
                    byte[] byteSet = key.getKey().getBytes();
                    for (int j = 0; j < (TreeObject.BYTES - Long.BYTES) / Long.BYTES; j++) {
                        buffer.putLong(byteSet[j]);
                    }
                    buffer.putLong(key.getCount());
                }
            }

            if (x.isLeaf) {
                buffer.put((byte) 1);
            } else {
                buffer.put((byte) 0);
            }

            buffer.putLong(x.parent);

            for (int i = 0; i < 2 * degree; i++) {
                if (x.childPointers[i] == 0) {
                    buffer.putLong(0);
                }
                else {
                    buffer.putLong(x.childPointers[i]);
                }
            }

            buffer.flip();
            file.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}