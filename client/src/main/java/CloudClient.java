import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class CloudClient {

    private static SocketChannel client;
    private static ByteBuffer buffer;
    private static CloudClient instance;

    public static void main(String[] args) {
        CloudClient client = start();
        System.out.println(client.sendMessage("Hello, Server !"));
        stop();
    }

    public static CloudClient start() {
        if (instance == null)
            instance = new CloudClient();

        return instance;
    }

    public static void stop() {
        try {
            client.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            buffer = null;
        }
    }

    private CloudClient() {
        try {
            client = SocketChannel.open(new InetSocketAddress("localhost", 15454));
            buffer = ByteBuffer.allocate(256);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String sendMessage(String msg) {
        buffer = ByteBuffer.wrap(msg.getBytes());
        String response = null;
        try {
            client.write(buffer);
            buffer.clear();
            client.read(buffer);
            response = new String(buffer.array()).trim();
            System.out.println("response=" + response);
            buffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;

    }
}
