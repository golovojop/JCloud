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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import network.CloudClient;
import network.MainView;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
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

    private BlockingQueue<ClientMessage> queue;
    private FileProvider fileProvider;
    private CloudClient client;
    private SessionId sessionId;
    private ProgressView progressView;
    private boolean isAuthenticated;
    private long messageId;
    private List<String> commandArgs;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colLocalName.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        colLocalSize.setCellValueFactory(new PropertyValueFactory<>("fileSize"));
        colCloudName.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        colCloudSize.setCellValueFactory(new PropertyValueFactory<>("fileSize"));

        // TODO: Провайдер локального хранилища
        fileProvider = new FileProvider();
        dirLocal(null);

        // TODO: Очередь сообщений серверу
        queue = new LinkedBlockingQueue<>(10);

        // TODO: Запуск клиента
        try {
            InetSocketAddress address;
            if(commandArgs.size() == 2) {
                address = new InetSocketAddress(commandArgs.get(0), Integer.parseInt(commandArgs.get(1)));
            } else {
                address = new InetSocketAddress("localhost", 15454);
            }

            client = new CloudClient(address, this, LOCAL_STORAGE);
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

            stage.setTitle("Авторизация");
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

            stage.setTitle("Регистрация");
            stage.setScene(new Scene(root, 400, 200));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void dirLocal(ActionEvent actionEvent) {
        updateLocalStoreView();
    }

    @FXML
    public void dirRemote(ActionEvent actionEvent) {
        putInQueue(new ClientDir(messageId++, sessionId, null));
    }

    @FXML
    public void delLocalFile() {
        FileDescriptor fd = tableLocal.getSelectionModel().getSelectedItem();
        new DeleteController(fd.getFileName());
        if (DeleteController.confirmDelete) {
            if (fileProvider.deleteFile(Paths.get(LOCAL_STORAGE, fd.getFileName()))) {
                tableLocal.getItems().clear();
                updateLocalStoreView();
            }
        }
    }

    @FXML
    public void delRemoteFile() {
        FileDescriptor fd = tableCloud.getSelectionModel().getSelectedItem();
        new DeleteController(fd.getFileName());
        if (DeleteController.confirmDelete) {
            putInQueue(new ClientDelFile(messageId++, sessionId, fd.getFileName()));
        }
    }

    @FXML
    public void initiateDownload(ActionEvent actionEvent) {
        FileDescriptor fd = tableCloud.getSelectionModel().getSelectedItem();
        putInQueue(new ClientGet(messageId++, sessionId, fd.getFileName()));
    }

    @FXML
    public void initiateUpload(ActionEvent actionEvent) {
        FileDescriptor fd = tableLocal.getSelectionModel().getSelectedItem();
        putInQueue(new ClientPut(messageId++, sessionId, fd.getFileName()));
    }

    /**
     * TODO: Отобразить ответ сервера
     */
    @Override
    public void renderResponse(ServerMessage response) {
        switch (response.getResponse()) {
            case SDIR:
                FileDescriptor[] fd = ((ServerDirResponse) response).getFiles();
                if (fd.length != 0) {
                    tableCloud.setItems(fileProvider.getStorageModel(fd));
                }
                break;
            case SAUTH:
                ServerAuthResponse respAuth = (ServerAuthResponse) response;
                isAuthenticated = respAuth.isAuth();
                sessionId = respAuth.getSessionId();

                hlSignup.setDisable(isAuthenticated);
                hlCloud.setDisable(isAuthenticated);
                dp(this, "renderResponse:SAUTH. response is " + isAuthenticated);

                // TODO: Если аутентифицированы, то запросить список файлов
                // TODO: Иначе вывести алерт об ошибке
                if (isAuthenticated) {
                    putInQueue(new ClientDir(messageId++, sessionId, null));
                } else {
                    showAlert("Incorrect password or login\nTry again");
                }
                break;
            case SSIGNUP:
                ServerSignupResponse respSignup = (ServerSignupResponse) response;
                if (!respSignup.isStatus()) {
                    showAlert(respSignup.getMessage());
                }
                hlSignup.setDisable(respSignup.isStatus());
                break;
            case SALERT:
                showAlert(((ServerAlertResponse) response).getMessage());
                break;
            case SDELETE:
                ServerDelResponse respDel = (ServerDelResponse) response;
                if (respDel.getUpdatedFileList().length != 0) {
                    tableCloud.setItems(fileProvider.getStorageModel(respDel.getUpdatedFileList()));
                }
                break;
            case SGET:
                break;
            default:
                dp(this, "renderResponse. Unknown server message");
        }
    }

    @Override
    public ClientMessage dequeueMessage() {
        ClientMessage message = null;
        try {
            message = queue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return message;
    }

    @Override
    public void updateLocalStoreView() {
        tableLocal.setItems(fileProvider.getStorageModel(LOCAL_STORAGE));
    }

    @Override
    public void updateRemoteStoreView() {
        putInQueue(new ClientDir(messageId++, sessionId, null));
    }

    @Override
    public void startProgressView() {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/progress_window.fxml"));
            Parent root = loader.load();
//            stage.initStyle(StageStyle.UNDECORATED);
            stage.initStyle(StageStyle.TRANSPARENT);
            progressView = (ProgressView) loader.getController();
            progressView.setWidth();
            updateProgressView(0.0);

            stage.setScene(new Scene(root, 300, 50));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopProgressView() {
        if (progressView != null) {
            progressView.close();
        }
        progressView = null;
    }

    @Override
    public void updateProgressView(Double progress) {
        if (progressView != null) progressView.update(progress);
    }

    /**
     * TODO: Отправка логина/пароля на сервер
     */
    public void signInCustomer(Customer customer) {
        putInQueue(new ClientAuth(messageId++, null, customer));
    }

    /**
     * TODO: Регистрация
     */
    public void signUpCustomer(Customer customer) {
        putInQueue(new ClientSignup(messageId++, customer));
    }

    /**
     * TODO: Остановка потока обработки сетевых сообщений.
     */
    public void stop() {
        putInQueue(new ClientBye(messageId++, sessionId, "Bye"));
    }

    /**
     * TODO: Сохранить аргументы командной строки
     */
    public void setArgs(List<String> args) {
        commandArgs = args;
    }

    /**
     * TODO: Поместить сообщение в очередь отправки
     */
    private void putInQueue(ClientMessage message) {
        try {
            queue.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // TODO: Сообщение об ошибке
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.CLOSE);
        alert.showAndWait();
    }
}