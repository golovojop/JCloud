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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import controller.CommandController;
import conversation.ClientMessage;
import conversation.Exchanger;
import conversation.protocol.*;

import static utils.Debug.*;

import data.dao.CustomerDao;
import data.provider.FileProvider;
import data.provider.JdbcProvider;
import domain.Customer;
import domain.Session;

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

    @Override
    public void run() {

        try {
            System.out.println("Server started on port 15454\n");
            Iterator<SelectionKey> iter;

            while (serverSocket.isOpen()) {

                // TODO: Синхронизация с методом attachToSelector()
                registerLock.lock();
                registerLock.unlock();

                selector.select(500);

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
        attachToSelector(client);
    }

    /**
     * TODO: Регистрация канала в селекторе.
     * Метод вызывается из разных потоков (CloudServer, FileTransfer),
     * поэтому требуется дополнительная синхронизация (здесь реализовано через Lock)
     * https://bit.ly/2ULK9KM
     * https://bit.ly/2IxCTeW
     */
    private void attachToSelector(SocketChannel client){
        try {
            registerLock.lock();
            selector.wakeup();
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            registerLock.unlock();
        }
    }

    /**
     * TODO: Прием сообщений от клиентов
     */
    private void handleRequest(SelectionKey key) {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ClientMessage message = (ClientMessage) Exchanger.receive(clientChannel);

        if (message != null) {
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
        } else {
            try {
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
        if(session == null) {
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
                dp(this, String.format("Client terminated with message:'%s'", ((ClientBye)message).getMessage()));
                break;
            case GET:   // TODO: Отключаем SocketChannel от селектора
                key.cancel();
                new FileTransfer(clientChannel,
                        message.getRequest(),
                        Paths.get(session.getCurrentDir().toString(), ((ClientGet)message).getFileName()),
                        this::attachToSelector);
                break;
            case PUT:   // TODO: и передаем его в поток передачи файла.
                key.cancel();
                new FileTransfer(clientChannel,
                        message.getRequest(),
                        Paths.get(session.getCurrentDir().toString(), ((ClientPut)message).getFileName()),
                        this::attachToSelector);
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
