package server;

import conversation.ClientMessage;
import conversation.ClientRequest;
import conversation.protocol.ClientGet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class FileTransfer implements Runnable {

    private SocketChannel channel;
    private ClientRequest request;
    private Path filePath;

    private Consumer<SocketChannel> op;

    public FileTransfer(SocketChannel channel, ClientRequest request, Path filePath, Consumer<SocketChannel> op) {
        this.channel = channel;
        this.request = request;
        this.filePath = filePath;
        this.op = op;

        new Thread(this).start();
    }

    @Override
    public void run() {

        switch (request) {
            case GET:
                sendFile(filePath);
                break;
            case PUT:
                receiveFile(filePath);
                break;
                default:
        }

        op.accept(channel);
    }

    /**
     * TODO: Отправить файл
     */
    private void sendFile(Path path) {

        try (RandomAccessFile is = new RandomAccessFile(path.toString(), "r")) {
            FileChannel fromChannel = is.getChannel();
            fromChannel.transferTo(0, is.length(), channel);
        } catch (IOException e) {e.printStackTrace();}
    }

    /**
     * TODO: Получить файл
     */
    private void receiveFile(Path path) {

    }
}

