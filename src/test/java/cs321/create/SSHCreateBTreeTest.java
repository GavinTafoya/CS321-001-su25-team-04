package cs321.create;

import java.io.File;
import java.util.Random;

import static org.junit.Assert.fail;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

public class SSHCreateBTreeTest
{
    Random rand = new Random();

    @Test
    public void testMain() {

        String cache = "0";
        String degree = rand.nextInt(23) + 2 + "";
        String sshFile = "data/SSH_Files/SSH_log.txt";
        String[] types = {"accepted-ip", "accepted-time", "failed-ip", "failed-time", "invalid-ip", "invalid-time", "reverseaddress-ip", "reverseaddress-time", "user-ip"};
        String type = types[rand.nextInt(types.length)];
        String database = "no";

        String[] args = {"--cache=" + cache, "--degree=" + degree, "--sshFile=" + sshFile, "--type=" + type, "--database=" + database};
        try {
            SSHCreateBTree.main(args);
        } catch (Exception e) {
            fail("Exception thrown during main execution: " + e.getMessage());
        }
        
        assertTrue(new File("SSH_log.txt.ssh.btree." + type + "." + degree).exists());
    }
}
