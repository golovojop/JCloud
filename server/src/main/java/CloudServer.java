import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import conversation.ClientMessage;
import conversation.Exchanger;
import conversation.protocol.ClientDir;
import conversation.protocol.ServerDirResponse;
import domain.FileDescriptor;
import domain.TestSerialization;

public class CloudServer implements Runnable {

    private ServerSocketChannel serverSocket;
    private Selector selector;

    public CloudServer() throws IOException {
        this.selector = Selector.open();
        this.serverSocket = ServerSocketChannel.open();
        this.serverSocket.bind(new InetSocketAddress("localhost", 15454));
        this.serverSocket.configureBlocking(false);
        this.serverSocket.register(selector, SelectionKey.OP_ACCEPT);
    }

    @Override
    public void run() {

        try {
            System.out.println("Server started on port 15454\n");
            Iterator<SelectionKey> iter;

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
        System.out.println("handleRequest: message received");
        SocketChannel client = (SocketChannel) key.channel();
        ClientMessage message = (ClientMessage) Exchanger.receive(client);

        if(message != null) {
            switch (message.getRequest()) {
                case DIR:
                    ClientDir dirCommand = (ClientDir) message;

                    TestSerialization ts = new TestSerialization("hello", 1l);

                    ServerDirResponse resp = new ServerDirResponse(dirCommand.getId(), new FileDescriptor[]{
                            new FileDescriptor("File1", 10),
                            new FileDescriptor("File2", 20),
                            new FileDescriptor("File3", 30),
                    });

                    Exchanger.send(client, resp);
                    break;
                default:
                    System.out.println("Unknown client message");
            }
        } else {
            try {
                client.close();
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    private static void registerClient(Selector selector, ServerSocketChannel serverSocket) throws IOException {
        SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
    }

    public static void main(String[] args) throws IOException {
        Thread server = new Thread(new CloudServer());
        server.start();

        try {
            server.join();
        } catch (InterruptedException e) {
            System.out.println("Server stopped");
        }
    }

}
