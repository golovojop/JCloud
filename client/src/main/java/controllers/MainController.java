package controllers;

import conversation.ClientMessage;
import conversation.ServerMessage;
import conversation.protocol.*;
import data.provider.FileProvider;
import domain.Customer;
import domain.FileDescriptor;
import static utils.Debug.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import network.CloudClient;
import network.MainView;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MainController implements Initializable, MainView {

    private final String LOCAL_STORAGE = "local_storage";

    @FXML
    Hyperlink hlCloud;
    @FXML
    private Hyperlink hlSignup;
    @FXML
    private TableView<FileDescriptor> tableLocal;
    @FXML
    private TableView<FileDescriptor> tableCloud;
    @FXML
    private TableColumn<FileDescriptor, String> colLocalName;
    @FXML
    private TableColumn<FileDescriptor, String> colCloudName;
    @FXML
    private TableColumn<FileDescriptor, String> colLocalSize;
    @FXML
    private TableColumn<FileDescriptor, String> colCloudSize;

    private FileProvider fileProvider;
    private CloudClient client;
    private SessionId sessionId;
    private long messageId;
    private boolean isAuthenticated;
    private BlockingQueue<ClientMessage> queue;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colLocalName.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        colLocalSize.setCellValueFactory(new PropertyValueFactory<>("fileSize"));
        colCloudName.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        colCloudSize.setCellValueFactory(new PropertyValueFactory<>("fileSize"));

        // TODO: Провайдер локального хранилища
        fileProvider = new FileProvider();
        tableLocal.setItems(fileProvider.getStorageModel(LOCAL_STORAGE));

        // TODO: Очередь сообщений серверу
        queue = new LinkedBlockingQueue<>(10);

        // TODO: Запуск клиента
        try {
            client = new CloudClient("localhost", 15454, this);
        } catch (IOException e) {
            // TODO: Нужен алерт и деактивация кнопок
            System.out.println("Client connection error");
        }
    }

    /**
     * TODO: Показать окно авторизации
     */
    @FXML
    public void signinPrompt(ActionEvent actionEvent) {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login_window.fxml"));
            Parent root = loader.load();
            LoginController lc = (LoginController) loader.getController();
            lc.backController = this;

            stage.setTitle("CloudStore Authentication");
            stage.setScene(new Scene(root, 400, 200));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * TODO: Показать окно регистрации
     */
    @FXML
    public void signupPrompt(ActionEvent actionEvent) {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/signup_window.fxml"));
            Parent root = loader.load();
            SignUpController suc = (SignUpController) loader.getController();
            suc.id = 200;
            suc.backController = this;

            stage.setTitle("Create login");
            stage.setScene(new Scene(root, 400, 200));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void dirRemote(ActionEvent actionEvent) {
        client.sendCommand(new ClientDir(messageId++, sessionId, null));
    }

    @Override
    public void renderResponse(ServerMessage response) {
        switch (response.getResponse()) {
            case SDIR:
                tableCloud.setItems(fileProvider.getStorageModel(((ServerDirResponse) response).getFiles()));
                break;
            case SAUTH:
                ServerAuthResponse resp = (ServerAuthResponse) response;
                isAuthenticated = resp.isAuth();
                sessionId = resp.getSessionId();

                hlSignup.setDisable(isAuthenticated);
                hlCloud.setDisable(isAuthenticated);
//                dp(this, "renderResponse, Authentication " + isAuthenticated + ", session ID " + sessionId);
                if(isAuthenticated) {
                    putInQueue(new ClientDir(messageId++, sessionId,null));
                }
                break;
            default:
                dp(this, "Unknown server message");
        }
    }

    @Override
    public ClientMessage dequeueMessage() {
        ClientMessage message = null;
        try {
            message = queue.take();
        } catch (InterruptedException e) {e.printStackTrace();}

        return message;
    }

    /**
     * TODO: Поместить сообщение в очередь отправки
     */
    private void putInQueue(ClientMessage message) {
//        dp(this, "putInQueue, message: " + message);
        try {
            queue.put(message);
        } catch (InterruptedException e) {e.printStackTrace();}
    }

    /**
     * TODO: Отправка логина/пароля на сервер
     */
    public void signInCustomer(Customer customer) {
        dp(this, "signInCustomer. Put message into the queue");
        putInQueue(new ClientAuth(messageId++, null, customer));
    }

    /**
     * TODO: Регистрация
     */
    public void signUpCustomer(Customer customer) {
    }
}