package network;

import controllers.MainController;
import conversation.ClientMessage;
import conversation.ClientRequest;
import conversation.Exchanger;
import conversation.ServerMessage;
import conversation.protocol.ClientGet;
import javafx.application.Platform;

import static utils.Debug.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

public class CloudClient implements Runnable {

    private static SocketChannel channel;
    private AtomicBoolean isRunning;
    private String currentDir;
    private MainView view;

    public CloudClient(String hostname, int port, MainView view, String currentDir) throws IOException {
        channel = SocketChannel.open(new InetSocketAddress(hostname, port));
        this.isRunning = new AtomicBoolean();
        this.isRunning.set(true);
        this.currentDir = currentDir;
        this.view = view;
        new Thread(this).start();
    }

    @Override
    public void run() {
        do {
            // TODO: Очередная команда пользователя
            ClientMessage message = view.dequeueMessage();
            dp(this, "run. Dequeued message " + message);

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
        dp(this, "handleCommand. Sent command " + command);

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
     * TODO: Выполнить загрузку. Если файл существует, то будет перезаписан.
     */
    private void receiveFile(String fileTo) {
        try (FileOutputStream fos = new FileOutputStream(Paths.get(currentDir, fileTo).toString());
             FileChannel toChannel = fos.getChannel()) {

            ByteBuffer lengthByteBuffer = ByteBuffer.wrap(new byte[8]);
            channel.read(lengthByteBuffer);

            long sourceLength = lengthByteBuffer.getLong(0);
            final long block_size = 512;
            long received = 0;

            dp(this, "receiveFile. Ready to receive " + sourceLength);
            do {
                received += toChannel.transferFrom(channel, received, sourceLength - received >= block_size ? block_size : sourceLength - received);
            } while (received < sourceLength);

            toChannel.force(false);
            dp(this, "receiveFile. Bytes received = " + received);

        } catch (IOException e) {e.printStackTrace();}
    }

    private void sendFile() {

    }

}
