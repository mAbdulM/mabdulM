import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Arrays;

class ClientHandler extends Thread
{
    final BufferedReader reader;
    final PrintStream writer;
    final Socket newSocket;
    final DSTNode node;


    // Constructor
    public ClientHandler(Socket mynewSocket, BufferedReader reader, PrintStream writer, DSTNode node)
    {
        this.newSocket = mynewSocket;
        this.reader = reader;
        this.writer = writer;
        this.node = node;
    }

    @Override
    public void run()
    {
        String name = node.getName();
        try{
            writer.println("HELLO " + name);
            String response = reader.readLine();
            if (response.contains("HELLO")) {
                String nodeName = response.split(" ")[1];
                if (!nodeName.equals("ephemeral")) {
                    // Create an active mapping for the starting node.
                    node.addToActiveMappings(nodeName);
                }
                while (true) {
                    response = reader.readLine();
                    if (response == null) {
                        break;
                    }
                    if (response.contains("BYE")) {
                        newSocket.close();
                        break;
                    }

                    if (response.contains("PING")) {
                        writer.println("PONG");
                    }
                    if (response.contains("STORE")) {
                        int num = Integer.parseInt(response.split(" ")[1]);
                        String s = "";
                        s += reader.readLine();
                        for (int i = 0; i < num-1; i++) {
                             s += "\n"+ reader.readLine();
                        }

                        String key = DSTNode.sha256(s);
                        String[] results = node.CompareHashes(key);
                        if (Arrays.asList(results).contains(name)) {
                            node.putValue(key, s);
                            writer.println("STORED " + key);
                        } else {
                            writer.println("NOTSTORED");
                        }
                    }
                    if (response.contains("LOOKUP")) {
                        String value = node.getValue(response.split(" ")[1]);
                        if (value != null) {
                            writer.println("FOUND " + DSTNode.countLines(value));
                            writer.println(value);
                        } else {
                            writer.println("NOTFOUND");
                        }
                    }
                    if (response.contains("FINDNEAREST")) {
                        String[] results = node.CompareHashes(response.split(" ")[1]);
                        writer.println("NODES " + results.length);
                        for (String node : results) {
                            writer.println(node);
                        }
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}