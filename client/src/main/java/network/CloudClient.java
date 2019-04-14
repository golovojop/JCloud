package network;

import conversation.ClientMessage;
import conversation.Exchanger;
import conversation.ServerMessage;
import domain.TestSerialization;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class CloudClient implements Runnable {

    private static SocketChannel channel;
    private MainView view;

    public CloudClient(String hostname, int port, MainView view) throws IOException {
        channel = SocketChannel.open(new InetSocketAddress(hostname, port));
        this.view = view;

        (new Thread(this)).start();
    }

    @Override
    public void run() {
        sendCommand(view.nextMessage());
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
        ServerMessage response = (ServerMessage) Exchanger.receive(channel);

        if (response != null) {
            view.renderResponse(response);
        }
    }
}
