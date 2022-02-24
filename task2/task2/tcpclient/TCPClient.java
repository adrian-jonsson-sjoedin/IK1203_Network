package tcpclient;

import java.net.*;
import java.io.*;

public class TCPClient {
    boolean shutdown;
    Integer timout;
    Integer limit;

    public TCPClient(boolean shutdown, Integer timeout, Integer limit) {
        this.shutdown = shutdown;
        if (timeout != null) {
            this.timout = timeout;
        } else {
            this.timout = null;
        }
        this.limit = limit;
    }

    public byte[] askServer(String hostname, int port, byte[] toServerBytes) throws IOException {
        ByteArrayOutputStream fromServer = new ByteArrayOutputStream();
        try {
            Socket clientSocket = new Socket(hostname, port); // call socket and connects to "hostname" on port "port"
            if (this.timout != null) {
                clientSocket.setSoTimeout(this.timout);
            }
            clientSocket.getOutputStream().write(toServerBytes); //sends bytes on socket
            if (this.shutdown) {
                clientSocket.shutdownOutput(); //shuts down output stream after data has been sent if we passed shutdown
            }                                  //as parameter

            byte[] buffer = new byte[128]; //static buffer for temporary data storage
            int index;
            int totalReadBytes = 0;
            if (this.limit != null) {
                if (this.limit <= 128) { //if the limit is smaller than our buffer size we can just read up to the limit
                    //index = clientSocket.getInputStream().read(buffer);   //this could potentially work as well
                    index = clientSocket.getInputStream().read(buffer, 0, this.limit - 1);
                    for (int i = 0; i < index; i++)
                        fromServer.write(buffer[i]);
                } else { //handles the case when the limit is bigger than our buffer
                    while (totalReadBytes < this.limit) {
                        index = clientSocket.getInputStream().read(buffer);
                        totalReadBytes += index;
                        for (int i = 0; i < index; i++) {
                            fromServer.write(buffer[i]);
                        }
                    }
                }
            } else {
                int flag;
                do {
                    flag = clientSocket.getInputStream().read(buffer);
                    index = flag;
                    for (int i = 0; i < index; i++)
                        fromServer.write(buffer[i]);
                } while (flag != -1);
            }
            clientSocket.close();
        } catch (IOException ex) {
            System.out.println("I/O exception " + ex);
            return fromServer.toByteArray();
        }
        return fromServer.toByteArray();
    }
}
