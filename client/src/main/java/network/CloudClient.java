package network;

import conversation.ClientMessage;
import conversation.Exchanger;
import conversation.ServerMessage;
import javafx.application.Platform;

import static utils.Debug.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

public class CloudClient implements Runnable {

    private static SocketChannel channel;
    private MainView view;
    private AtomicBoolean isRunning;

    public CloudClient(String hostname, int port, MainView view) throws IOException {
        channel = SocketChannel.open(new InetSocketAddress(hostname, port));
        this.isRunning = new AtomicBoolean();
        this.isRunning.set(true);
        this.view = view;

        (new Thread(this)).start();
    }

    @Override
    public void run() {
        while (isRunning.get()) {
            sendCommand(view.dequeueMessage());
        }

        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        isRunning.compareAndSet(true, false);
    }

    public void sendCommand(ClientMessage command) {
        Exchanger.send(channel, command);
        ServerMessage response = (ServerMessage) Exchanger.receive(channel);

        // TODO: Отобразить результат в потоке основного окна
        if (response != null) {
            Platform.runLater(() -> {
                view.renderResponse(response);
            });
        }
    }
}
