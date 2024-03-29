package tcpclient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TCPClient {

    public TCPClient() {
    }
    public byte[] askServer(String hostname, int port, byte[] toServerBytes) throws IOException {
        ByteArrayOutputStream fromServer = new ByteArrayOutputStream();
        try {
            Socket clientSocket = new Socket(hostname, port); // call socket and connects to "hostname" on port "port"
            clientSocket.getOutputStream().write(toServerBytes); //sends bytes on socket

            int flag = 0;
            int index;
            do {
                byte[] buffer = new byte[128];
                flag = clientSocket.getInputStream().read(buffer);
                index = flag;
                for (int i = 0; i < index; i++)
                    fromServer.write(buffer[i]);
            } while (flag != -1);

        } catch (IOException ex) {
            System.out.println("I/O exception " + ex);
        }
        return fromServer.toByteArray();
    }
}
