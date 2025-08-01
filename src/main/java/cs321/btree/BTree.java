package cs321.btree;

import java.io.IOException;
import java.io.PrintWriter;

public class BTree<T extends Comparable<T>> implements BTreeInterface
{
    //------------------------------------------------------------------
    // Private Node Class
    //------------------------------------------------------------------

    /**
     * Inner class to represent a binary search tree node.
     *
     */
    private class Node<S extends Comparable<S>> implements Comparable<Node<S>> {
        private Node<S> left,right,parent;
        private boolean isLeaf;
        private S key;

        /**
         * Constructor for a node.
         * @param element
         */
        public Node(S element) {
            key = element;
            left = right = parent = null;
            isLeaf = false;
        }

        /**
         * Constructor for an empty node (used for root initialization)
         */
        public Node() {
            key = null;
            left = right = parent = null;
            isLeaf = true;
        }

        /**
         * {@inheritDoc}
         */
        public int compareTo(Node<S> otherNode) {
            if (key == null && otherNode.key == null) return 0;
            if (key == null) return -1;
            if (otherNode.key == null) return 1;
            return key.compareTo(otherNode.key);
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
    private Node<T> root;
    //------------------------------------------------------------------
    // Constructor
    //------------------------------------------------------------------

    /**
     * Creates an empty BTree
     */
    public BTree(String name){
        this.root  = new Node<T>();
        this.root.isLeaf = true;
        this.size = 0;
        this.height = 0;
        this.degree = 2; // default degree
    }

    public BTree(int degree, String name) {
        this.root  = new Node<T>();
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
        return countNodes(root);
    }
    
    private long countNodes(Node<T> node) {
        if (node == null) {
            return 0;
        }
        // For now, just return 1 since we only have the root
        // This will need to be updated when the tree structure is properly implemented
        return 1;
    }

    /**
     * @return The height of the BTree
     */
    @Override
    public int getHeight() {
        return height; // This is wrong
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
     *
     * @return
     */
    public String[] getSortedKeyArray(){
        return null;
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

    private void diskRead() {

    }

    private void diskWrite(){

    }

}
