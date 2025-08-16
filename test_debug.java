import cs321.search.SSHSearchBTree;
import java.io.*;

public class test_debug {
    public static void main(String[] args) {
        // Capture output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
        
        // Test with non-existent files
        String[] testArgs = {"--btree-file=/tmp/nonexistent.btree", 
                           "--query-file=/tmp/nonexistent.txt", "--degree=10"};
        
        SSHSearchBTree.main(testArgs);
        
        System.out.println("STDOUT: " + outContent.toString());
        System.out.println("STDERR: " + errContent.toString());
    }
}
