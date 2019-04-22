package network;

import controllers.MainController;
import conversation.ClientMessage;
import conversation.ClientRequest;
import conversation.Exchanger;
import conversation.ServerMessage;
import conversation.protocol.ClientGet;
import conversation.protocol.ClientPut;
import javafx.application.Platform;

import static utils.Debug.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
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

    /**
     * TODO: Отправка команд клиента и прием ответов сервера
     */
    public void handleCommand(ClientMessage command) {

        // TODO: Отправить команду
        Exchanger.send(channel, command);

        dp(this, "handleCommand. Sent command " + command);
        // TODO: Обработать ответ сервера
        switch (command.getRequest()) {
            case GET:
                receiveFile(currentDir, ((ClientGet) command).getFileName());
                callInMainThread(view::updateLocalStoreView);
//                Platform.runLater(() -> {
//                    view.updateLocalStoreView();
//                });
                break;
            case PUT:
                sendFile(Paths.get(currentDir, ((ClientPut) command).getFileName()));
                callInMainThread(view::updateRemoteStoreView);
//                Platform.runLater(() -> {
//                    view.updateRemoteStoreView();
//                });
                break;
            default: {
                ServerMessage response = (ServerMessage) Exchanger.receive(channel);

                // TODO: Отобразить результат в потоке основного окна
                if (response != null && isRunning.get()) {
                    callInMainThread(view::renderResponse, response);
//                    Platform.runLater(() -> {
//                        view.renderResponse(response);
//                    });
                }
            }
        }
    }

    /**
     * TODO: Выполнить загрузку. Если файл существует, то будет перезаписан.
     * Файл принимается блоками размером block_size. Для приема последнего блока,
     * который не равен block_size нужно установить точный размер.
     */
    private void receiveFile(String dirTo, String destFileName) {
        try (FileOutputStream fos = new FileOutputStream(Paths.get(dirTo, destFileName).toString());
             FileChannel toChannel = fos.getChannel()) {

            callInMainThread(view::startProgressView);

//            Platform.runLater(() -> {
//                view.startProgressView();
//            });

            // TODO: Прочитать длину файла
            ByteBuffer lengthByteBuffer = ByteBuffer.wrap(new byte[8]);
            channel.read(lengthByteBuffer);

            long sourceLength = lengthByteBuffer.getLong(0);
            final long block_size = 8192;
            long received = 0;

            dp(this, "receiveFile. Ready to receive " + sourceLength);
            do {
                received += toChannel.transferFrom(channel, received, sourceLength - received >= block_size ? block_size : sourceLength - received);

                final double progress = ((double) received) / (double) (sourceLength);
                callInMainThread(view::updateProgressView, progress);
//                Platform.runLater(() -> {
//                    view.updateProgressView(progress);
//                });
            } while (received < sourceLength);

            toChannel.force(false);
            dp(this, "receiveFile. Bytes received = " + received);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            callInMainThread(view::stopProgressView);
//            Platform.runLater(() -> {
//                view.stopProgressView();
//            });
        }
    }

    /**
     * TODO: Отправка
     */
    private void sendFile(Path path) {
        ByteBuffer lengthByteBuffer = ByteBuffer.wrap(new byte[8]);

        try (FileInputStream fis = new FileInputStream(path.toString());
             FileChannel fromChannel = fis.getChannel()) {

            callInMainThread(view::startProgressView);

            long block_size = 8192;
            long sourceLength = fromChannel.size();
            long sent = 0;

            dp(this, "sendFile. Source file length is " + sourceLength);

            // TODO: Сообщить длину передаваемого файла
            lengthByteBuffer.putLong(0, sourceLength);
            channel.write(lengthByteBuffer);

            // TODO: Передать файл блоками block_size
            do {
                long count = sourceLength - sent > block_size ? block_size : sourceLength - sent;
                sent += fromChannel.transferTo(sent, count, channel);

                final double progress = ((double) sent) / (double) (sourceLength);
                callInMainThread(view::updateProgressView, progress);

            } while (sent < sourceLength);

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
    private <T> void callInMainThread(Consumer<T> op, T arg){
        Platform.runLater(() -> {
            op.accept(arg);
        });
    }
    private void callInMainThread(Runnable op){
        Platform.runLater(() -> {
            op.run();
        });
    }
}
