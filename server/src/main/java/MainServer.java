import server.CloudServer;

import java.io.IOException;

public class MainServer {
    public static void main(String[] args) throws IOException {
        Thread server = new Thread(new CloudServer());
        server.start();

        try {
            server.join();
        } catch (InterruptedException e) {
            System.out.println("Server stopped");
        }
    }
}
