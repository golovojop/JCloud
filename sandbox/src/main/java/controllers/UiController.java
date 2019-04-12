package controllers;

import data.dao.CustomerDao;
import data.provider.JdbcProvider;
import data.provider.LocalProvider;
import data.provider.RemoteProvider;
import domain.Customer;
import domain.FileWrapper;
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

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class UiController implements Initializable {

    private final String LOCAL_STORAGE = "local_storage";
    private final String CLOUD_STORAGE = "remote_storage";

    @FXML
    Hyperlink hlCloud;

    @FXML
    private TableView<FileWrapper> tableLocal;
    @FXML
    private TableView<FileWrapper> tableCloud;

    @FXML
    private TableColumn<FileWrapper, String> colLocalName;
    @FXML
    private TableColumn<FileWrapper, String> colCloudName;

    @FXML
    private TableColumn<FileWrapper, String> colLocalSize;
    @FXML
    private TableColumn<FileWrapper, String> colCloudSize;

    private LocalProvider localStorage;
    private RemoteProvider remoteStorage;
    private CustomerDao customerDao;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colLocalName.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        colLocalSize.setCellValueFactory(new PropertyValueFactory<>("fileSize"));
        colCloudName.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        colCloudSize.setCellValueFactory(new PropertyValueFactory<>("fileSize"));

        // TODO: Провайдер локального хранилища
        localStorage = new LocalProvider(LOCAL_STORAGE);
        tableLocal.setItems(localStorage.getStorageModel());

        // TODO: Провайдер удаленного хранилища
        remoteStorage = new RemoteProvider(CLOUD_STORAGE);

        // TODO: Провайдер к таблице Customer
        customerDao = (CustomerDao)(new JdbcProvider());
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
            lc.backUiController = this;

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
            suc.backUiController = this;

            stage.setTitle("Create login");
            stage.setScene(new Scene(root, 400, 200));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean signInCustomer(Customer customer) {
        boolean autorized = customerDao.getCustomerByLoginAndPass(customer.getLogin(), customer.getPass()) != null;

        if(autorized) {
            tableCloud.setItems(remoteStorage.getStorageModel());
        }

        System.out.println(customer.getLogin() + ": " + autorized);
        return autorized;
    }


    public boolean signUpCustomer(Customer customer) {

        if(customerDao.insertCustomer(customer)) {
            return remoteStorage.createDirectory(customer.getLogin());
        } else {
            System.out.println("signUpCustomer: error");
        }

        return false;
    }
}