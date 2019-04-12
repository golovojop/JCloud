package controllers;

import conversation.ServerMessage;
import conversation.protocol.ClientAuth;
import conversation.protocol.ClientDir;
import conversation.protocol.ServerAuthResponse;
import conversation.protocol.ServerDirResponse;
import data.dao.CustomerDao;
import data.provider.FileProvider;
import data.provider.JdbcProvider;
import domain.Customer;
import domain.FileDescriptor;
import domain.TestSerialization;
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
import network.MessageHandler;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable, MessageHandler {

    private final String LOCAL_STORAGE = "local_storage";
    private final String REMOTE_STORAGE = "remote_storage";

    @FXML
    Hyperlink hlCloud;
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

    private FileProvider localStorage;
    private CustomerDao customerDao;
    private CloudClient client;
    private long messageId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colLocalName.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        colLocalSize.setCellValueFactory(new PropertyValueFactory<>("fileSize"));
        colCloudName.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        colCloudSize.setCellValueFactory(new PropertyValueFactory<>("fileSize"));

        // TODO: Провайдер локального хранилища
        localStorage = new FileProvider(LOCAL_STORAGE);
        tableLocal.setItems(localStorage.getStorageModel());

        // TODO: Провайдер к таблице Customer
        customerDao = (CustomerDao)(new JdbcProvider());

        // TODO: Запуск клиента
        client = CloudClient.start(this);
    }

    /**
     *  TODO: Показать окно авторизации
     */
    @FXML
    public void authPrompt(ActionEvent actionEvent) {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login_window.fxml"));
            Parent root = loader.load();
            LoginController lc = (LoginController) loader.getController();
            lc.id = 100;
            lc.backController = this;

            stage.setTitle("CloudStore Autorization");
            stage.setScene(new Scene(root, 400, 200));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *  TODO: Показать окно регистрации
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
        client.sendCommand(new ClientDir(messageId++, "Hello, Server !"));
    }

    @Override
    public void handleMessage(ServerMessage response) {

        switch (response.getResponse()) {
            case SDIR:
                for(FileDescriptor fd : ((ServerDirResponse)response).getFiles()) {
                    System.out.println(String.format("File: %s, size %d", fd.getFileName(), fd.getFileSize()));
                }
                break;
            case SAUTH:
                ServerAuthResponse resp = (ServerAuthResponse)response;
                System.out.println("Authentication " + resp.isAuthResult());

                break;
            default:
                System.out.println("Unknown server message");
        }
    }

    @Override
    public void handleMessage(TestSerialization response) {
        System.out.println(String.format("File: %s, size %d", response.getText(), response.getValue()));
    }

    /**
     * TODO: Аутентификация
     */
    public boolean signInCustomer(Customer customer) {

        client.sendCommand(new ClientAuth(messageId++, customer));
        return true;

//        boolean autorized = customerDao.getCustomerByLoginAndPass(customer.getLogin(), customer.getPass()) != null;
//        if(autorized) {
//            tableCloud.setItems(remoteStorage.getStorageModel());
//        }
//        System.out.println(customer.getLogin() + ": " + autorized);
//        return autorized;
    }


    /**
     *  TODO: Подписка
     */
    public boolean signUpCustomer(Customer customer) {
//
//        if(customerDao.insertCustomer(customer)) {
//            return remoteStorage.createDirectory(customer.getLogin());
//        } else {
//            System.out.println("signUpCustomer: error");
//        }

        return false;
    }
}