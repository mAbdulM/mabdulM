// IN2011 Computer Networks
// Coursework 2022/2023
//
// Submission by
// YOUR_NAME_GOES_HERE
// YOUR_STUDENT_ID_NUMBER_GOES_HERE
// YOUR_EMAIL_GOES_HERE

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;

import static java.lang.Math.min;

public class DSTNode {
    private InetAddress host;
    private int port;
    private String id;
    private String name;
    private String nID;
    private ArrayList<String> activeMappings = new ArrayList<>(10);
    private ServerSocket serverSocket;
    private Hashtable<String,String> hashtable = new Hashtable<>(10);

    // Do not change the interface!
    public DSTNode (InetAddress host, int port, String id) {
        // Using the IP address, port number and identifier compute the node name and node ID.
        this.host = host;
        this.port = port;
        this.id = id;
        this.name = host.getHostAddress() +"/"+ port +"/"+ id;
        this.nID = sha256(this.name);
        // Open a ServerSocket.
        try {
            serverSocket = new ServerSocket(port,50,host);
        } catch (IOException e) {
            e.printStackTrace();
        }
        activeMappings.add(name);
    }

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
    public void handleIncomingConnections(String startingNodeName) {
        String[] names = startingNodeName.split("/");

        try {
            if (!startingNodeName.equalsIgnoreCase(name)) {
                // Connect to the DSTHash23 network using the given node name.
                Socket startingNodeSocket = new Socket(names[0], Integer.parseInt(names[1]));

                // Create a BufferedReader to read messages from the socket's input stream.
                BufferedReader in = new BufferedReader(new InputStreamReader(startingNodeSocket.getInputStream()));

                // Create a PrintWriter to write messages to the socket's output stream.
                PrintWriter out = new PrintWriter(startingNodeSocket.getOutputStream());

                // Send a HELLO message to the starting node.
                out.println("HELLO " + name);
                out.flush();


                // Read the response from the starting node.
                String response = in.readLine();


                // Parse the response to get the node ID of the starting node.
                String startingNodeId = response.split(" ")[1];

                // Create an active mapping for the starting node.
                activeMappings.add(startingNodeName);

                out.println("BYE time-out");
                out.flush();
                startingNodeSocket.close();
            }

            // Accept incoming connections from other nodes and respond to all protocol commands correctly.
            while (true) {

                Socket clientSocket = serverSocket.accept();


                // Set up readers and writers for convenience
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintStream writer = new PrintStream(clientSocket.getOutputStream());
                Thread myThread = new ClientHandler(clientSocket, reader, writer, this);
                myThread.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

	
	// Accept multiple incoming connections and respond to all protocol commands correctly.
    	return;
    }

    public synchronized String[] CompareHashes(String key) {
        String[] closestNodes = new String[min(3,activeMappings.size())];
        int[] clNodeScore = new int[min(3,activeMappings.size())];
        Arrays.fill(clNodeScore,257);


        for (String node:activeMappings) {
            String hashedNode = sha256(node);
            int count = 256;
            for (int i = 0; i < key.length(); i++) {
                if (key.charAt(i) == hashedNode.charAt(i)){
                    count--;
                }else{
                    break;
                }
            }
            for (int i = 0; i < clNodeScore.length; i++) {
                if (count < clNodeScore[i]) {
                    for (int j = clNodeScore.length-2; j >= i ; j--) {
                        clNodeScore[j + 1] = clNodeScore[j];
                        closestNodes[j + 1] = closestNodes[j];
                    }
                    clNodeScore[i] = count;
                    closestNodes[i]= node;
                    break;
                }
            }
        }
        return closestNodes;
    }

    public String getName() {
        return name;
    }

    public synchronized ArrayList<String> getActiveMappings() {
        return activeMappings;
    }

    public synchronized void addToActiveMappings(String name){
        activeMappings.add(name);
    }

    public static int countLines(String str){
        String[] lines = str.split("\r\n|\r|\n");
        return  lines.length;
    }

    public synchronized String getValue(String key) {
        return this.hashtable.get(key);
    }

    public synchronized void putValue(String key, String value) {
        this.hashtable.put(key, value);
    }
}
