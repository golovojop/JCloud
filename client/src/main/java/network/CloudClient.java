package network;

import conversation.ClientMessage;
import conversation.ClientRequest;
import conversation.Exchanger;
import conversation.ServerMessage;
import conversation.protocol.ClientGet;
import javafx.application.Platform;

import static utils.Debug.*;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
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
            // TODO: Очередная команда пользователя
            ClientMessage message = view.dequeueMessage();

            if (message.getRequest() == ClientRequest.BYE) {
                isRunning.compareAndSet(true, false);
                // TODO: Известить сервер, если сессия валидна
                if (message.getSessionId() != null) {
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

        switch (command.getRequest()) {
            case GET:
                receiveFile(((ClientGet)command).getFileName());
                break;
            case PUT:
                break;
            default: {
                ServerMessage response = (ServerMessage) Exchanger.receive(channel);

                // TODO: Отобразить результат в потоке основного окна
                if (response != null && isRunning.get()) {
                    Platform.runLater(() -> {
                        view.renderResponse(response);
                    });
                }
            }
        }
    }

    /**
     * TODO: Выполнить загрузку
     */
    private void receiveFile(String fileTo) {
        try (RandomAccessFile fis = new RandomAccessFile(fileTo, "w")) {
            FileChannel toChannel = fis.getChannel();
            final long block_size = 1024;
            long position = 0;
            long received = 0;

            do {
                received = toChannel.transferFrom(channel, position, block_size);
                position += received;

            } while (received == block_size);
            toChannel.close();
        } catch (IOException e) {e.printStackTrace();}
    }

    private void sendFile() {

    }

}
