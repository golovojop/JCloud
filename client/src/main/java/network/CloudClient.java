package network;

import conversation.ClientMessage;
import conversation.ClientRequest;
import conversation.Exchanger;
import conversation.ServerMessage;
import conversation.protocol.ServerGetResponse;
import conversation.protocol.ServerPutReadyResponse;
import exception.RemoteHostDisconnected;
import javafx.application.Platform;

import static utils.Debug.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class CloudClient implements Runnable {

    private static SocketChannel channel;
    private AtomicBoolean isRunning;
    private String currentDir;
    private MainView view;
    private final long block_size;

    public CloudClient(InetSocketAddress inetAddress, MainView view, String currentDir) throws IOException {
        channel = SocketChannel.open(inetAddress);
        this.isRunning = new AtomicBoolean();
        this.isRunning.set(true);
        this.currentDir = currentDir;
        this.block_size = 16384;
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
                boolean result = handleCommand(message);
                isRunning.set(result);
            }
        } while (isRunning.get());

        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * TODO: Отправка команд клиента и прием ответов сервера
     */
    public boolean handleCommand(ClientMessage command) {
        ServerMessage response = null;
        long lastCommandId = command.getId();

        dp(this, "handleCommand. Sending command " + command);

        // TODO: Отправить команду
        Exchanger.send(channel, command);

        // TODO: Получить ответ
        try {
            response = (ServerMessage) Exchanger.receive(channel);
        } catch (RemoteHostDisconnected e) {
            dp(this, "handleCommand. Server has crashed.");
            return false;
        }

        if (response.getId() != lastCommandId) {
            dp(this, "handleCommand. Incorrect response sequence number");
            return false;
        }

        switch (response.getResponse()) {
            case SGET:
                boolean isValidFileLength = ((ServerGetResponse)response).getLength() != -1;

                if(isValidFileLength) {
                    receiveFile(currentDir,
                            ((ServerGetResponse) response).getFileName(),
                            ((ServerGetResponse) response).getLength());
                    callInMainThread(view::updateLocalStoreView);
                }
                break;
            case SPUT_READY:
                sendFile(Paths.get(currentDir, ((ServerPutReadyResponse) response).getFileName()), ((ServerPutReadyResponse) response).getLength());
                try {
                    response = (ServerMessage) Exchanger.receive(channel);
                } catch (RemoteHostDisconnected e) {
                    dp(this, "handleCommand. Server has crashed.");
                    return false;
                }

            default: {
                // TODO: Отобразить результат в потоке основного окна
                if (response != null && isRunning.get()) {
                    callInMainThread(view::renderResponse, response);
                }
            }
        }
        return true;
    }

    /**
     * TODO: Выполнить загрузку. Если файл существует, то будет перезаписан.
     * Файл принимается блоками размером block_size. Для приема последнего блока,
     * который не равен block_size нужно установить точный размер.
     */
    private void receiveFile(String dirTo, String destFileName, long length) {
        try (FileOutputStream fos = new FileOutputStream(Paths.get(dirTo, destFileName).toString());
             FileChannel toChannel = fos.getChannel()) {

            callInMainThread(view::startProgressView);

            long received = 0;

            dp(this, "receiveFile. Ready to receive " + length);
            do {
                received += toChannel.transferFrom(channel, received, length - received >= block_size ? block_size : length - received);

                final double progress = ((double) received) / (double) (length);
                callInMainThread(view::updateProgressView, progress);

            } while (received < length);

            toChannel.force(false);
            dp(this, "receiveFile. Bytes received = " + received);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            callInMainThread(view::stopProgressView);
        }
    }

    /**
     * TODO: Отправка
     */
    private void sendFile(Path path, long length) {

        try (FileInputStream fis = new FileInputStream(path.toString());
             FileChannel fromChannel = fis.getChannel()) {

            callInMainThread(view::startProgressView);

            long sent = 0;

            dp(this, "sendFile. Source file length is " + length);

            // TODO: Передать файл блоками block_size
            do {
                long count = length - sent > block_size ? block_size : length - sent;
                sent += fromChannel.transferTo(sent, count, channel);

                final double progress = ((double) sent) / (double) (length);
                callInMainThread(view::updateProgressView, progress);

            } while (sent < length);

            dp(this, "sendFile. Bytes sent " + sent);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            callInMainThread(view::stopProgressView);
        }
    }

    /**
     * TODO: Выполнение кода в потоке FX
     */
    private <T> void callInMainThread(Consumer<T> op, T arg) {
        Platform.runLater(() -> {
            op.accept(arg);
        });
    }

    private void callInMainThread(Runnable op) {
        Platform.runLater(() -> {
            op.run();
        });
    }
}
