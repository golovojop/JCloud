package server;

import com.sun.org.apache.xpath.internal.operations.String;
import conversation.protocol.SessionId;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class FileService implements Runnable {

    private ServerSocketChannel serverSocket;
    private CloudServer cloudServer;

    public FileService(CloudServer cloudServer, int port) throws IOException {
        this.serverSocket = ServerSocketChannel.open();
        this.serverSocket.bind(new InetSocketAddress("localhost", port));

        this.cloudServer = cloudServer;
    }

    @Override
    public void run() {

        try {
            while (serverSocket.isOpen()) {
                SocketChannel socketChannel =  serverSocket.accept();
                new FileOperation(cloudServer, socketChannel);
            }
        } catch (IOException e) {e.printStackTrace();}
    }
}

class FileOperation implements Runnable {
    SocketChannel socketChannel;
    CloudServer cloudServer;

    public FileOperation(CloudServer cloudServer, SocketChannel socketChannel) {
        this.cloudServer = cloudServer;
        this.socketChannel = socketChannel;

        new Thread(this).start();
    }

    @Override
    public void run() {


    }
}
