package network;

import conversation.ClientMessage;
import conversation.Exchanger;
import conversation.ServerMessage;
import domain.TestSerialization;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;


public class CloudClient {

    private static SocketChannel channel;
    private static CloudClient instance;
    private static long messageId = 0;
    private MessageHandler handler;

    private CloudClient() {
        try {
            channel = SocketChannel.open(new InetSocketAddress("localhost", 15454));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static CloudClient start(MessageHandler handler) {
        if (instance == null) {
            instance = new CloudClient();
            instance.handler = handler;
        }
        return instance;
    }

    private static void stop() {
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendCommand(ClientMessage command) {

        Exchanger.send(channel, command);
        ServerMessage response = (ServerMessage)Exchanger.receive(channel);

        if(response != null) {
            handler.handleMessage(response);
        }
    }
}
