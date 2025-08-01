package cs321.btree;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class BTree<T extends Comparable<T>> implements BTreeInterface
{

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
     * Performs a traversal in order starting at node and appends each keys String value to output
     *
     * @param node    the node to traverse
     * @param output  accumulator list that receives the keys encountered during traversal
     */
    private void inOrder(Node<T> node, ArrayList<String> output) {
        if (node == null) return;
        for (int i = 0; i < node.numKeys; i++) {
            if (!node.isLeaf) inOrder(node.childPointers[i], output);
            output.add(node.keys[i].getKey());
        }
        if (!node.isLeaf) inOrder(node.childPointers[node.numKeys], output);
    }
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
        int numKeys;
        TreeObject[] keys;
        Node<S>[] childPointers;

        /**
         * Constructor for a node.
         * @param element
         */
        public Node(S element) {
            key = element;
            left = right = parent = null;
            keys = new TreeObject[2 * degree - 1];
            @SuppressWarnings("unchecked")
            Node<S>[] kids = (Node<S>[]) new Node[2 * degree];
            childPointers = kids;
            numKeys = 0;
            isLeaf  = true;
        }


        /**
         * {@inheritDoc}
         */
        public int compareTo(Node<S> otherNode) {
            return key.compareTo(otherNode.key);
        }


        /**
         * {@inheritDoc}
         */
        public String toString() {
            return "Node:  key = " + key.toString();
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
        this.degree = 2;
        this.root  = new Node<>(null);
        this.size = 0;
        this.height = 0;
    }

    public BTree(int degree, String name) {
        this.root  = null;
        this.size = 0;
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
        return 0;
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
        if (root == null) {
            root = new Node<>(null);
            root.keys[0] = obj;
            root.numKeys = 1;
            size = 1;
            height = 0;
            return;
        }
        Node<T> cursor = root;
        while (true) {
            int position = Arrays.binarySearch(cursor.keys,0, cursor.numKeys, obj);
            if (position >= 0) {
                cursor.keys[position].incCount();
                return;
            }
            position = -position - 1;
            if (cursor.isLeaf){
                break;
            }
            if (cursor.childPointers[position].numKeys == 2 * degree - 1) {
                splitChild(cursor, position);
                if (obj.compareTo(cursor.keys[position]) > 0) position++;
            }
            cursor = cursor.childPointers[position];
        }
        if (root.numKeys == 2 * degree - 1) {
            Node<T> newRoot = new Node<>(null);
            newRoot.isLeaf = false;
            newRoot.childPointers[0] = root;
            splitChild(newRoot, 0);
            root = newRoot;
            height++;
            insertInNodeWithSpace(root, obj);
        } else {
            insertInNodeWithSpace(cursor, obj);
        }
        size++;
    }

    /**
     * Inserts key into the node with space
     *
     * @param node the B-tree node that currently has fewer than 2t-1
     * @param key  the  to insert
     */
    private void insertInNodeWithSpace(Node<T> node, TreeObject key) {
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
        if (node.childPointers[position].numKeys == 2 * degree - 1) {
            splitChild(node, position);
            if (key.compareTo(node.keys[position]) > 0){
                position++;
            }
        }
        insertInNodeWithSpace(node.childPointers[position], key);
    }

    /**
     * Splits the full child at parent.childPointers[childIndex] and promotes the median key in the parent
     *
     * @param parent the node whose child is being split
     * @param childIndex index of the full child within parent.childPointers
     */
    private void splitChild(Node<T> parent, int childIndex) {
        Node<T> fullChild  = parent.childPointers[childIndex];
        Node<T> newSibling = new Node<>(null);
        newSibling.isLeaf  = fullChild.isLeaf;
        newSibling.numKeys = degree - 1;
        System.arraycopy(fullChild.keys, degree, newSibling.keys, 0, degree - 1);
        if (!fullChild.isLeaf) {
            System.arraycopy(fullChild.childPointers, degree, newSibling.childPointers, 0, degree);
        }
        fullChild.numKeys = degree - 1;
        System.arraycopy(parent.childPointers, childIndex + 1, parent.childPointers,childIndex + 2, parent.numKeys - childIndex);
        parent.childPointers[childIndex + 1] = newSibling;
        System.arraycopy(parent.keys, childIndex, parent.keys,childIndex + 1, parent.numKeys - childIndex);
        parent.keys[childIndex] = fullChild.keys[degree - 1];
        parent.numKeys++;
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

    private void diskRead() {

    }

    private void diskWrite(){

    }

}
