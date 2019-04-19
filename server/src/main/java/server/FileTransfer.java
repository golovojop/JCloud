package server;




import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

import conversation.ClientRequest;
import static utils.Debug.*;

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
        ByteBuffer lengthByteBuffer = ByteBuffer.wrap(new byte[8]);

        try (FileInputStream fis = new FileInputStream(path.toString());
             FileChannel fromChannel = fis.getChannel()) {

            long block_size = 512;
            long sourceLength = fromChannel.size();
            long sent = 0;

            dp(this, "sendFile. Source file length is " + sourceLength);

            // TODO: Сообщить длину передаваемого файла
            lengthByteBuffer.putLong(0, sourceLength);
            channel.write(lengthByteBuffer);

            // TODO: Передать файл блоками block_size
            do{
                long count = sourceLength - sent > block_size ? block_size : sourceLength - sent;
                dp(this, "sendFile. count " + count);
                sent += fromChannel.transferTo(sent, count, channel);
            } while (sent < sourceLength);

            dp(this, "sendFile. Bytes sent " + sent);

        } catch (IOException e) {e.printStackTrace();}
    }

    /**
     * TODO: Выполнить загрузку. Если файл существует, то будет перезаписан.
     * Файл принимается блоками размером block_size. Для приема последнего блока,
     * который не равен block_size нужно установить точный размер.
     */
    private void receiveFile(Path path) {
        try (FileOutputStream fos = new FileOutputStream(path.toString());
             FileChannel toChannel = fos.getChannel()) {

            // TODO: Прочитать длину файла
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
}

