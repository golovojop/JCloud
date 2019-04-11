/**
 * Materials:
 * https://docs.oracle.com/javase/7/docs/api/java/nio/ByteBuffer.html#wrap(byte[])
 */

package network;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Client {
    /**
     * TODO: Отправить сообщение
     */
    public static void send(SocketChannel socket, Serializable serializable) throws IOException {
        // TODO: Сразу же создает байтовый массив
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // TODO: Первые 4 байта будут полем length. Обнуляем.
        for (int i = 0; i < 4; i++) {
            baos.write(0);
        }

        // TODO: Упаковка объекта в массив
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(serializable);
        oos.close();

        // TODO: Wrap array to Buffer
        final ByteBuffer wrap = ByteBuffer.wrap(baos.toByteArray());
        wrap.putInt(0, baos.size() - 4);
        socket.write(wrap);
    }

    /**
     * TODO: Получить и распаковать
     */
    public static Serializable recv(SocketChannel socket) throws IOException, ClassNotFoundException {

        ByteBuffer lengthByteBuffer = ByteBuffer.wrap(new byte[4]);
        ByteBuffer dataByteBuffer = null;
        Serializable ret = null;

        // TODO: Прочитать длину сообщения
        socket.read(lengthByteBuffer);
        if (lengthByteBuffer.remaining() == 0) {
            dataByteBuffer = ByteBuffer.allocate(lengthByteBuffer.getInt(0));

            // TODO: Прочитать все остальное сообщение
            socket.read(dataByteBuffer);
            if (dataByteBuffer.remaining() == 0) {
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(dataByteBuffer.array()));
                ret = (Serializable) ois.readObject();
            }
            dataByteBuffer = null;
        }
        lengthByteBuffer = null;

        return ret;
    }
}
