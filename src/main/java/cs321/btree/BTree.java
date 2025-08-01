package cs321.btree;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class BTree<T extends Comparable<T>> implements BTreeInterface
{
    //------------------------------------------------------------------
    // Private Node Class
    //------------------------------------------------------------------

    /**
     * Inner class to represent a binary search tree node.
     *
     */
    private class Node {
        private long left,right,parent;
        private long address;
        private boolean isLeaf;
        private TreeObject key;

        /**
         * Constructor for a node.
         */
        public Node() {
            key = null;
            left = right = parent = 0;
            isLeaf = true;
        }

        /**
         * Constructor for a node with a key.
         * @param element
         */
        public Node(TreeObject element) {
            key = element;
            left = right = parent = 0;
            isLeaf = true;
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return "Node:  key = " + (key != null ? key.toString() : "null");
        }

    }// end of Private Node Class

    //------------------------------------------------------------------
    // Variables
    //------------------------------------------------------------------
    private int size, height, degree;
    private Node root;
    
    //Disk Variables

    private int METADATA_SIZE = Long.BYTES;
    private long rootAddress = METADATA_SIZE;
    private long nextDiskAddress = METADATA_SIZE;
    private FileChannel file;
    private ByteBuffer buffer;
    
    //------------------------------------------------------------------
    // Constructor
    //------------------------------------------------------------------

    /**
     * Creates an empty BTree
     */
    public BTree(String name){
        this.root  = new Node();
        this.root.isLeaf = true;
        this.size = 0;
        this.height = 0;
        this.degree = 2; // default degree
    }

    public BTree(int degree, String name) {
        this.root  = new Node();
        this.root.isLeaf = true;
        this.size = 0;
        this.height = 0;
        this.degree = degree;
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
     * @return Returns the number of keys in the BTree.
     */
    @Override
    public long getSize() {
        return size;
    }

    /**
     * @return The degree of the BTree.
     */
    @Override
    public int getDegree() {
        return degree;
    }

    /**
     * @return Returns the number of nodes in the BTree.
     */
    @Override
    public long getNumberOfNodes() {
        if (root == null) {
            return 0;
        }
        return 1; // For now, just the root node exists
    }

    /**
     * @return The height of the BTree
     */
    @Override
    public int getHeight() {
        return height; // This is wrong
    }


    public String[] getSortedKeyArray() {
        return null;
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
        return null;
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
        return null;
    }

    private Node Predecessor(Node P){
        return null;
    }

    private Node TreeMinimum(Node S){
        return null;
    }

    private Node TreeMaximum(Node S){
        return null;
    }

    private void Transplant(){

    }

    private Node diskRead(long diskAdress) throws IOException{
    	if(diskAdress == 0) return null;
    	
    	file.position(diskAdress);
    	buffer.clear();
    	
    	file.read(buffer);
    	buffer.flip();

    	long value = buffer.getLong();
    	long frequency = buffer.getLong();
    	
    	TreeObject key = new TreeObject(value + " ", frequency);
 	
    	byte flag = buffer.get();
    	boolean leaf = false;
    	if(flag == 1) {
    		leaf = true;
    	}
    	
    	long parent = buffer.getLong();
    	long left = buffer.getLong();
    	long right = buffer.getLong();
    	
    	Node x = new Node(key); 
    	x.isLeaf = leaf;
    	x.parent = parent;
    	x.left = left;
    	x.right = right;
    	x.address = diskAdress;
    	
    	return x;
    }

    private void diskWrite(Node x) throws IOException{
    	file.position(x.address);
    	buffer.clear();
    	
    	buffer.putLong(Long.parseLong(x.key.getKey()));
    	if(x.isLeaf) {
    		buffer.put((byte) 1);
    	}
    	else {
    		buffer.put((byte) 0);
    	}
    	
    	buffer.putLong(x.parent);
    	buffer.putLong(x.left);
    	buffer.putLong(x.right);
    	
    	buffer.flip();
    	file.write(buffer);
    }

}
