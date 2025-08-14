package cs321.create;

import java.nio.file.Paths;

public class SSHDataWranglerTest {

    public static void main(String[] args) throws Exception {
        // Test the SSHDataWrangler with a sample input
        String[] testArgs = {"rawSSHFile=" +  Paths.get("data/SSH_FIles/SSH_log.txt").toAbsolutePath(), "sshFile=wrangled_SSH_log.txt"};
        SSHDataWrangler.main(testArgs);
    }
}