/**
 * Materials:
 * https://stackoverflow.com/questions/5862971/java-readobject-with-nio
 */

package conversation;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Exchanger {

    /**
     * TODO: Отправить сообщение
     */
    public static void send(SocketChannel channel, Serializable serializable) {

        // TODO: Сразу же создает байтовый массив
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // TODO: Первые 4 байта будут полем length. Обнуляем.
        for (int i = 0; i < 4; i++) {
            baos.write(0);
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {

            // TODO: Сериализуем в массив
            oos.writeObject(serializable);

            // TODO: Переводим массив в буфер
            ByteBuffer buffer = ByteBuffer.wrap(baos.toByteArray());
            buffer.putInt(0, baos.size() - 4);
            channel.write(buffer);

        } catch (IOException e) {e.printStackTrace();}
    }

    /**
     * TODO: Получить и распаковать
     */
    public static Serializable receive(SocketChannel socket) {

        ByteBuffer lengthByteBuffer = ByteBuffer.wrap(new byte[4]);
        ByteBuffer dataByteBuffer = null;
        Serializable message = null;

        try {
            // TODO: Прочитать длину сообщения
            socket.read(lengthByteBuffer);
            if (lengthByteBuffer.remaining() == 0 && lengthByteBuffer.getInt(0) > 0) {
                dataByteBuffer = ByteBuffer.allocate(lengthByteBuffer.getInt(0));

                // TODO: Прочитать все остальное сообщение
                socket.read(dataByteBuffer);
                if (dataByteBuffer.remaining() == 0) {
                    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(dataByteBuffer.array()));
                    message = (Serializable) ois.readObject();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return message;
    }
}
