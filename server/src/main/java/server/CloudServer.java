package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import controller.CommandController;
import conversation.ClientMessage;
import conversation.ClientRequest;
import conversation.Exchanger;
import conversation.SessionId;
import conversation.protocol.*;

import static utils.Debug.*;

import data.dao.CustomerDao;
import data.provider.FileProvider;
import data.provider.JdbcProvider;
import domain.Customer;
import domain.Session;
import exception.RemoteHostDisconnected;

public class CloudServer implements Runnable {
    public final String CLOUD_STORAGE = "remote_storage";
    private Map<SessionId, Session> activeClients;
    private CommandController controller;

    private ServerSocketChannel serverSocket;
    private Selector selector;

    private FileProvider fileProvider;
    private CustomerDao customerDao;
    private ReentrantLock registerLock;

    public CloudServer() throws IOException {
        this.selector = Selector.open();
        this.serverSocket = ServerSocketChannel.open();
        this.serverSocket.bind(new InetSocketAddress("localhost", 15454));
        this.serverSocket.configureBlocking(false);
        this.serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        this.activeClients = new HashMap<>();
        this.registerLock = new ReentrantLock();

        // TODO: Провайдер локального хранилища
        fileProvider = new FileProvider();

        // TODO: Провайдер к таблице Customer
        customerDao = new JdbcProvider();

        this.controller = new CommandController(this, fileProvider, CLOUD_STORAGE);
    }

    /**
     * https://bit.ly/2KZbpRm - на счет iter.remove()
     *
     */
    @Override
    public void run() {

        try {
            System.out.println("Server started on port 15454\n");
            Iterator<SelectionKey> iter;

            while (serverSocket.isOpen()) {

                // TODO: Синхронизация с методом attachToSelector()
                registerLock.lock();
                registerLock.unlock();

                int keysQty = selector.select(500);

                if(keysQty == 0){
                    continue;
                }
                else {
                    dp(this, "run. Keys selected " + keysQty);
                }

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
        if (client != null) {
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
            dp(this, "registerConnect. Received a new connection from " + client.socket().getRemoteSocketAddress());
        }
    }

    /**
     * TODO: Регистрация канала в селекторе.
     * Метод вызывается из разных потоков (CloudServer, FileTransfer),
     * поэтому требуется дополнительная синхронизация (здесь реализовано через Lock)
     * https://bit.ly/2ULK9KM
     * https://bit.ly/2IxCTeW
     */
    private void attachToSelector(SocketChannel client) {
        if (client != null) {
            try {
                registerLock.lock();
                client.configureBlocking(false);
                client.register(selector, SelectionKey.OP_READ);
                dp(this, "attachToSelector. Attached " + client.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                registerLock.unlock();
            }
        }
    }

    /**
     * TODO: Выполнить дополнительные действия после передачи файла
     */
    private void postFileTransferHandler(SocketChannel client, ClientMessage message) {
        attachToSelector(client);

        if(message.getRequest() == ClientRequest.PUT) {
            // TODO: Отправить клиенту обновленный список файлов в папке
            Exchanger.send(
                    client,
                    controller.commandPutFinished((ClientPut) message, activeClients.get(message.getSessionId())));
        }
    }

    /**
     * TODO: Прием сообщений от клиентов
     */
    private void handleRequest(SelectionKey key) {
        SocketChannel clientChannel = (SocketChannel) key.channel();

        try {
            ClientMessage message = (ClientMessage) Exchanger.receive(clientChannel);

            if (message != null) {
                dp(this, "handleRequest. Received " + message.getRequest());
                switch (message.getRequest()) {
                    case AUTH:
                        ClientAuth authCommand = (ClientAuth) message;
                        SessionId sessionId = createSessionId(key);
                        Exchanger.send(clientChannel, new ServerAuthResponse(
                                message.getId(),
                                authenticateClient(authCommand.getCustomer(), sessionId),
                                sessionId));
                        break;
                    case SIGNUP:
                        ClientSignup cs = (ClientSignup) message;
                        Exchanger.send(clientChannel, signupClient(message.getId(), cs.getCustomer(), createSessionId(key)));
                        break;
                    default:
                        commandProcessor(key, message);
                }
            }
        } catch (RemoteHostDisconnected ex) {
            try {
                dp(this, "handleRequest. Client with sessionId  " + createSessionId(key).getId() + " disconnected");
                activeClients.remove(createSessionId(key));
                clientChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * TODO: Взаимодействие с клиентом
     */
    private void commandProcessor(SelectionKey key, ClientMessage message) {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        Session session = activeClients.get(message.getSessionId());

        // TODO: Авторизация сессии
        if (session == null) {
            Exchanger.send(clientChannel, new ServerAlertResponse(message.getId(), "You should authenticate first"));
            return;
        }

        switch (message.getRequest()) {
            case DIR:
                Exchanger.send(clientChannel, controller.commandDir((ClientDir) message, session));
                break;
            case DELETE:
                Exchanger.send(clientChannel, controller.commandDel((ClientDelFile) message, session));
                break;
            case BYE:
                activeClients.remove(message.getSessionId());
                dp(this, String.format("Client terminated with message:'%s'", ((ClientBye) message).getMessage()));
                break;
            case GET:
                // TODO: Response READY + file length
                Exchanger.send(clientChannel, controller.commandGet((ClientGet) message, session));
                // TODO: Disconnect from selector
                key.cancel();
                // TODO: Send file in new thread
                new FileTransfer(clientChannel,
                        message,
                        Paths.get(session.getCurrentDir().toString(), ((ClientGet) message).getFileName()),
                        this::postFileTransferHandler);
                break;
            case PUT:
                // TODO: Response READY
                Exchanger.send(clientChannel, controller.commandPutReady((ClientPut) message, session));
                // TODO: Disconnect from selector
                key.cancel();
                dp(this, "commandProcessor. Ready to receive file with size " + ((ClientPut) message).getLength());
                // TODO: Receive file in new thread
                new FileTransfer(clientChannel,
                        message,
                        Paths.get(session.getCurrentDir().toString(), ((ClientPut) message).getFileName()),
                        ((ClientPut) message).getLength(),
                        this::postFileTransferHandler);
                break;
            default:
                dp(this, "commandProcessor. Unknown client message");
        }
    }

    /**
     * TODO: Client authentication
     */
    private boolean authenticateClient(Customer customer, SessionId sessionId) {

        if (isAuthenticated(sessionId)) {
            return true;
        } else if (customerDao.getCustomerByLoginAndPass(customer.getLogin(), customer.getPass()) != null) {
            activeClients.put(sessionId, new Session(sessionId, customer, Paths.get(CLOUD_STORAGE, customer.getLogin())));
            return true;
        }
        return false;
    }

    /**
     * TODO: Ограничения на регистрацию:
     * TODO:    - клиент не должен быть аутентифицирован (sessionId == null),
     * TODO:    - в базе все логины уникальны
     * TODO:    - ??
     */
    private ServerSignupResponse signupClient(long messageId, Customer customer, SessionId sessionId) {

        if (activeClients.get(sessionId) == null
                && customerDao.insertCustomer(customer)
                && fileProvider.createDirectory(Paths.get(CLOUD_STORAGE, customer.getLogin()))) {
            return new ServerSignupResponse(messageId, true, "ok");
        }
        return new ServerSignupResponse(messageId, false, "Try again with another login");
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
