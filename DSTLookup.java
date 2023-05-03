// IN2011 Computer Networks
// Coursework 2022/2023
//
// Submission by
// YOUR_NAME_GOES_HERE
// YOUR_STUDENT_ID_NUMBER_GOES_HERE
// YOUR_EMAIL_GOES_HERE

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class DSTLookup {

    // Do not change the interface!
    public DSTLookup () {}

    // Do not change the interface!
    public String getValue(String startingNodeName, String key) {

        String[] names = startingNodeName.split("/");
        String lines = "";
        try {
            Socket socket = new Socket(names[0], Integer.parseInt(names[1]));

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintStream writer = new PrintStream( socket.getOutputStream() );
            String closNode = startingNodeName;

            int count = 500000;
            String msg;
            while (true) {
                msg = reader.readLine();
                if (msg == null){
                    break;
                }
                if (msg.contains("HELLO")){
                    writer.println("HELLO ephemeral");
                    writer.println("FINDNEAREST " +key);
                }
                if (msg.contains("NODES")){
                    int num = Integer.parseInt(msg.split(" ")[1]);
                    String NewClNode = reader.readLine();
                    if (!closNode.equals(NewClNode)){
                        closNode = NewClNode;
                        writer.println("BYE test");
                        socket.close();
                        String[] nodeNames = closNode.split("/");
                        Socket nSocket = new Socket(nodeNames[0], Integer.parseInt(nodeNames[1]));
                        reader = new BufferedReader(new InputStreamReader(nSocket.getInputStream()));
                        writer = new PrintStream(nSocket.getOutputStream());
                    }
                    else {
                        writer.println("BYE test");
                        socket.close();
                        String[] nodeNames = closNode.split("/");
                        Socket nSocket = new Socket(nodeNames[0], Integer.parseInt(nodeNames[1]));
                        reader = new BufferedReader(new InputStreamReader(nSocket.getInputStream()));
                        writer = new PrintStream(nSocket.getOutputStream());
                        break;
                    }
                }
            }
            while (true) {
                msg = reader.readLine();
                if (msg == null) {
                    break;
                }
                if (msg.contains("HELLO")) {
                    writer.println("HELLO ephemeral");
                    writer.println("LOOKUP " + key);

                }
                if (msg.contains("NOTFOUND")) {
                    writer.println("BYE test");
                    socket.close();
                    return null;

                } else if (msg.contains("FOUND")) {
                    int num = Integer.parseInt(msg.split(" ")[1]);
                    for (int i = 0; i < num; i++) {
                        lines += reader.readLine() + "\n";
                    }
                    writer.println("BYE test");
                    socket.close();
                    return lines;
                }
            }



        } catch (IOException e) {
            e.printStackTrace();
        }


        // Connect to the DSTHash23 network using startingNodeName.

	// Find the node in the network with the ID closest to the key.

	// Use the key to get the value from the closest node.

	// If the value is found, return it.
	
        // If the value is not found, return null.
	return null;
    }
}
