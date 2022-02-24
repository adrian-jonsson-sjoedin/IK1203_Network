import tcpclient.TCPClient;

import java.io.*;
import java.net.ServerSocket;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class HTTPAsk {
    public static void main(String[] args) throws IOException {
        // Our HTTP headers
        String HTTP200 = "HTTP/1.1 200 OK\r\n\r\n";
        String HTTP400 = "HTTP/1.1 400 Bad Request\r\n";
        String HTTP404 = "HTTP/1.1 404 Not Found\r\n";
        try {
            int myServerPort = Integer.parseInt(args[0]);
            //int myServerPort = 8888;
            ServerSocket serverSocket = new ServerSocket(myServerPort);

            //Create an infinite loop for the socket to listen on
            while (true) {
                Socket connectionSocket = serverSocket.accept();
                System.out.println("Client connected");
                BufferedReader in = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                PrintWriter output = new PrintWriter(connectionSocket.getOutputStream(), true);
                String spec = in.readLine();
                System.out.println("URL entry: " + spec);

                String[] headers = spec.split("[\\?= &]"); //splits the string wherever there's a / ? = &
                for (int i = 0; i < headers.length; i++) {
                    System.out.println(headers[i]);
                }

                String hostname = null;
                String portString = null;
                String limitString = null;
                String timeoutString = null;
                String shutdownString = null;
                String toServerBytesString = null;
                for (int i = 1; i < headers.length; i++) {
                    if (headers[i].equals("hostname")) {
                        hostname = headers[i + 1];
                    } else if (headers[i].equals("port")) {
                        portString = headers[i + 1];
                    } else if (headers[i].equals("limit")) {
                        limitString = headers[i + 1];
                    } else if (headers[i].equals("timeout")) {
                        timeoutString = headers[i + 1];
                    } else if (headers[i].equals("shutdown")) {
                        shutdownString = headers[i + 1];
                    } else if (headers[i].equals("string")) {
                        toServerBytesString = headers[i + 1];
                    }
                }
                //converting the parameters to the correct type
                int port = -1;
                if (portString != null)
                    port = Integer.parseInt(portString);
                Integer limit = null;
                Integer timeout = null;
                boolean shutdown = false;
                byte[] toServerBytes = null;
                if (limitString != null)
                    limit = Integer.parseInt(limitString);
                if (timeoutString != null)
                    timeout = Integer.parseInt(timeoutString);
                if (shutdownString != null)
                    shutdown = Boolean.parseBoolean(shutdownString);
                if (toServerBytesString != null) {
                    toServerBytesString += "\n";
                    toServerBytes = toServerBytesString.getBytes(StandardCharsets.UTF_8);
                }
                /* Printing to terminal to check that all parameters are correct */
//                System.out.println("hostname: " + hostname);
//                System.out.println("port: " + port);
//                System.out.println("timeout:" + timeout);
//                System.out.println("limit: " + limit);
//                System.out.println("shutdown: " + shutdown);
//                System.out.println("toServerBytes: " + toServerBytesString); //string passed in the web browser


                //check if both hostname and port are assigned and if it is an /ask request
                if (port > 0 && hostname != null && portString.matches("[0-9]+") && headers[1].equals("/ask")) {
                    try {
                        TCPClient tcpClient = new TCPClient(shutdown, timeout, limit);
                        byte[] serverBytes = tcpClient.askServer(hostname, port, toServerBytes);
                        String serverOutput = new String(serverBytes);
                        /* Uncomment the line below to see the TCPClient's response*/
                        //    System.out.println(serverOutput);
                        String responseS = HTTP200 + serverOutput;
                        byte[] response = responseS.getBytes(StandardCharsets.UTF_8);
                        connectionSocket.getOutputStream().write(response);
                        connectionSocket.close();
                        System.out.println("Client disconnected");
                    } catch (IOException ex) {
                        //The if statement above failed. Couldn't connect to server
                        byte[] response = HTTP404.getBytes(StandardCharsets.UTF_8);
                        connectionSocket.getOutputStream().write(response);
                        connectionSocket.close();
                        System.out.println("Client disconnected");
                    }
                } else {
                    //if hostname or port is empty, or we didn't include /ask
                    byte[] response = HTTP400.getBytes(StandardCharsets.UTF_8);
                    connectionSocket.getOutputStream().write(response);
                    connectionSocket.close();
                    System.out.println("Client disconnected");
                }
            }
        } catch (java.io.IOException ex) {
            System.out.println("Error: " + ex);
        }

    }
}

