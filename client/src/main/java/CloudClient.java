import conversation.ClientMessage;
import conversation.Exchanger;
import conversation.ServerMessage;
import conversation.message.ClientDir;
import conversation.message.ServerDirResponse;
import domain.FileWrapper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class CloudClient {

    private static SocketChannel client;
    private static CloudClient instance;
    private static long messageId = 0;

    public static void main(String[] args) {
        CloudClient client = start();

        // TODO: Команда "dir"
        System.out.println(client.sendCommand(new ClientDir(messageId++, "Hello, Server !")));
        stop();
    }

    private CloudClient() {
        try {
            client = SocketChannel.open(new InetSocketAddress("localhost", 15454));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static CloudClient start() {
        if (instance == null) {
            instance = new CloudClient();
        }
        return instance;
    }

    private static void stop() {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String sendCommand(ClientMessage command) {

        Exchanger.send(client, command);
        ServerMessage response = (ServerMessage)Exchanger.receive(client);

        switch (response.getResponse()) {
            case SDIR:
                for(FileWrapper fw : ((ServerDirResponse)response).getFiles()) {
                    System.out.println(String.format("File %s, size %d", fw.getFileName(), fw.getFileSize()));
                }
                break;
            default:
                System.out.println("Unknown server message");
        }

        return "OK";
    }
}
