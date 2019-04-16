package network;

import conversation.ClientMessage;
import conversation.ClientRequest;
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
        new Thread(this).start();
    }

    @Override
    public void run() {

        do {
            ClientMessage message = view.dequeueMessage();

            if (message.getRequest() == ClientRequest.BYE) {
                isRunning.compareAndSet(true, false);
                // TODO: Известить сервер, если сессия валидна
                if(message.getSessionId() != null) {
                    Exchanger.send(channel, message);
                }
            } else {
                handleCommand(message);
            }
        } while (isRunning.get());

        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleCommand(ClientMessage command) {
        Exchanger.send(channel, command);
        ServerMessage response = (ServerMessage) Exchanger.receive(channel);

        // TODO: Отобразить результат в потоке основного окна
        if (response != null && isRunning.get()) {
            Platform.runLater(() -> {
                view.renderResponse(response);
            });
        }
    }
}
