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
import conversation.ClientRequest;
import conversation.Exchanger;
import conversation.protocol.ClientAuth;
import conversation.protocol.ClientDir;
import conversation.protocol.ServerAuthResponse;
import conversation.protocol.SessionId;
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

    private FileProvider localStorage;
    private CustomerDao customerDao;

    public CloudServer() throws IOException {
        this.selector = Selector.open();
        this.serverSocket = ServerSocketChannel.open();
        this.serverSocket.bind(new InetSocketAddress("localhost", 15454));
        this.serverSocket.configureBlocking(false);
        this.serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        this.activeClients = new HashMap<>();
        this.controller = new CommandController(this);

        // TODO: Провайдер локального хранилища
        localStorage = new FileProvider(CLOUD_STORAGE);

        // TODO: Провайдер к таблице Customer
        customerDao = new JdbcProvider();
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

        } catch (IOException e) {e.printStackTrace();}
    }

    private void registerConnect(Selector selector, ServerSocketChannel serverSocket) throws IOException {
        SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
    }

    private void handleRequest(SelectionKey key) {
        System.out.println("handleRequest: message received");

        SocketChannel client = (SocketChannel) key.channel();
        ClientMessage message = (ClientMessage) Exchanger.receive(client);

        if(message != null) {
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
                    Exchanger.send(client, controller.commandDir((ClientDir) message));
                    break;

                default:
                    System.out.println("Unknown client message");
            }
        } else {
            try {
                activeClients.remove(createSessionId(key));
                client.close();
            } catch (IOException e) { e.printStackTrace(); }
        }
    }



    /**
     * TODO: Client authentication
     */
    private boolean authenticateClient(Customer customer, SessionId sessionId) {

        if(isAuthenticated(sessionId)){
            return true;
        } else if(customerDao.getCustomerByLoginAndPass(customer.getLogin(), customer.getPass()) != null) {
            activeClients.put(sessionId, new Session(sessionId, customer, customer.getLogin()));
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
        return new SessionId(((SocketChannel) key.channel()).socket().getRemoteSocketAddress().hashCode());
    }
}
