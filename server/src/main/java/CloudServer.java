import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import conversation.ClientMessage;
import conversation.Exchanger;
import conversation.message.ClientDir;
import conversation.message.ServerDirResponse;
import domain.FileWrapper;

public class CloudServer {

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        Selector selector = Selector.open();
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress("localhost", 15454));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        Iterator<SelectionKey> iter;

        try {
            System.out.println("Server started on port 15454\n");

            while (serverSocket.isOpen()) {
                selector.select();

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                iter = selectedKeys.iterator();

                while (iter.hasNext()) {

                    SelectionKey key = iter.next();
                    iter.remove();

                    if (key.isAcceptable()) {
                        registerClient(selector, serverSocket);
                    }
                    if (key.isReadable()) {
                        handleRequest(key);
                    }
                }
            }

        } catch (IOException e) {e.printStackTrace();}

    }

    private static void handleRequest(SelectionKey key) {
        SocketChannel client = (SocketChannel) key.channel();
        ClientMessage message = (ClientMessage) Exchanger.receive(client);

        if(message == null) return;

        switch (message.getRequest()) {
            case DIR:
                ClientDir dirCommand = (ClientDir) message;
                Exchanger.send(client, new ServerDirResponse(dirCommand.getId(), new FileWrapper[]{new FileWrapper(dirCommand.getTarget(), 0)}));
                break;
            default:
                System.out.println("Unknown client message");
        }
    }

    private static void registerClient(Selector selector, ServerSocketChannel serverSocket) throws IOException {
        SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
    }
}
