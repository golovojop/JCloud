package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import controller.CommandController;
import conversation.ClientMessage;
import conversation.Exchanger;
import conversation.protocol.ClientAuth;
import conversation.protocol.ClientDir;
import conversation.protocol.ServerAuthResponse;
import conversation.protocol.SessionId;

import static utils.Debug.*;

import data.dao.CustomerDao;
import data.provider.FileProvider;
import data.provider.JdbcProvider;
import domain.Customer;
import domain.Session;

public class CloudServer implements Runnable {
    private final String CLOUD_STORAGE = "remote_storage";
    private Map<SessionId, Session> activeClients;
    private CommandController controller;

    private ServerSocketChannel serverSocket;
    private Selector selector;

    private FileProvider fileProvider;
    private CustomerDao customerDao;

    public CloudServer() throws IOException {
        this.selector = Selector.open();
        this.serverSocket = ServerSocketChannel.open();
        this.serverSocket.bind(new InetSocketAddress("localhost", 15454));
        this.serverSocket.configureBlocking(false);
        this.serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        this.activeClients = new HashMap<>();

        // TODO: Провайдер локального хранилища
        fileProvider = new FileProvider();

        // TODO: Провайдер к таблице Customer
        customerDao = new JdbcProvider();

        this.controller = new CommandController(this, fileProvider, CLOUD_STORAGE);
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
                        registerConnect(selector, serverSocket);
                    }
                    if (key.isReadable()) {
                        handleRequest(key);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * TODO: Новый сетевой коннект регистрируем в селекторе
     */
    private void registerConnect(Selector selector, ServerSocketChannel serverSocket) throws IOException {
        SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
    }

    /**
     * TODO: Прием сообщений от клиентов
     */
    private void handleRequest(SelectionKey key) {
        SocketChannel client = (SocketChannel) key.channel();
        ClientMessage message = (ClientMessage) Exchanger.receive(client);

        if (message != null) {
            switch (message.getRequest()) {
                case AUTH:
                    ClientAuth authCommand = (ClientAuth) message;
                    SessionId sessionId = createSessionId(key);
                    Exchanger.send(client, new ServerAuthResponse(
                            message.getId(),
                            authenticateClient(authCommand.getCustomer(), sessionId),
                            sessionId));
                    break;
                case DIR:
//                    dp(this, "handleRequest:DIR. sessionID = " + message.getSessionId());
//                    dp(this, "handleRequest:DIR. session = " + activeClients.get(message.getSessionId()));
                    Session session = activeClients.get(message.getSessionId());
                    Exchanger.send(client, controller.commandDir((ClientDir) message, session));
                    break;
                default:
                    dp(this, "Unknown client message");
            }
        } else {
            try {
                activeClients.remove(createSessionId(key));
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * TODO: Client authentication
     */
    private boolean authenticateClient(Customer customer, SessionId sessionId) {

        if (isAuthenticated(sessionId)) {
//            dp(this, "authenticateClient[1]. " + activeClients.get(sessionId).getSessionId());
            return true;
        } else if (customerDao.getCustomerByLoginAndPass(customer.getLogin(), customer.getPass()) != null) {
            activeClients.put(sessionId, new Session(sessionId, customer, customer.getLogin()));
//            dp(this, "authenticateClient[2]. " + activeClients.get(sessionId).getSessionId());
            return true;
        }
        return false;
    }

    /**
     * TODO:
     */
    private boolean isAuthenticated(SessionId sessionId) {
        return activeClients.get(sessionId) != null;
    }

    /**
     * TODO:
     */
    private SessionId createSessionId(SelectionKey key) {
//        dp(this, "createSessionId. id = " + ((SocketChannel) key.channel()).socket().getRemoteSocketAddress().hashCode());
        return new SessionId(((SocketChannel) key.channel()).socket().getRemoteSocketAddress().hashCode());
    }
}
