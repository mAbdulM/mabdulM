// IN2011 Computer Networks
// Coursework 2022/2023
//
// Submission by
// YOUR_NAME_GOES_HERE
// YOUR_STUDENT_ID_NUMBER_GOES_HERE
// YOUR_EMAIL_GOES_HERE

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.Arrays;

public class DSTStore {

    public static String sha256(final String base) {
        try{
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hash = digest.digest(base.getBytes("UTF-8"));
            final StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < hash.length; i++) {
                final String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    // Do not change the interface!
    public DSTStore () {}

    // Do not change the interface!
    public boolean storeValue(String startingNodeName, String value) {
        String[] names = startingNodeName.split("/");
        String key = sha256(value);
        String[] closNodes = new String[3];
        String closNode = startingNodeName;
        Arrays.fill(closNodes,null);
        int successful = 0;
        try {
            Socket socket = new Socket(names[0], Integer.parseInt(names[1]));

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintStream writer = new PrintStream(socket.getOutputStream());
            String msg;
            while (true) {
                msg = reader.readLine();
                if (msg.contains("HELLO")) {
                    writer.println("HELLO ephemeral");
                    writer.println("PING");
                }

                if (msg.contains("PONG")) {
                    writer.println("FINDNEAREST " + key);
                }
                if (msg.contains("NODES")) {
                    int num = Integer.parseInt(msg.split(" ")[1]);
                    String[] newNodes = new String[3];
                    for (int i = 0; i < num; i++) {
                        closNodes[i] = reader.readLine();
                    }
                    writer.println("BYE test");
                    socket.close();
                    break;
                }
            }


            for (String node : closNodes) {
                if (node != null) {
                    String[] nodeNames = node.split("/");
                    Socket nSocket = new Socket(nodeNames[0], Integer.parseInt(nodeNames[1]));
                    reader = new BufferedReader(new InputStreamReader(nSocket.getInputStream()));
                    writer = new PrintStream(nSocket.getOutputStream());
                    while (true) {
                        msg = reader.readLine();
                        if (msg == null) {
                            break;
                        }
                        if (msg.contains("HELLO")) {
                            writer.println("HELLO ephemeral");
                            writer.println("STORE "+DSTNode.countLines(value));
                            writer.println(value);
                            //change 1

                        }
                        if (msg.contains("NOTSTORED")) {
                            writer.println("BYE test");
                            nSocket.close();
                            return false;

                        } else if (msg.contains("STORED")) {
                            writer.println("BYE test");
                            nSocket.close();
                            successful+=1;
                            break;

                        }
                    }
                }
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
	// Compute the key for the input using the SHA-256 hash.
	
	// Connect to the DSTHash23 network using startingNodeName.

	// Find the three nodes in the network with the closest IDs to the key.

	// Store the contents of the file on all of the three closest nodes.

	// If this works, return true.
	
	// If it does not, return false.
	return successful==3;
    }
}
