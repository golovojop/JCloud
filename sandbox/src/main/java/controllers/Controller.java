package controllers;

import data.provider.LocalProvider;
import domain.FileWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import data.repository.file.FileHelper;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private final String LOCAL_STORAGE = "local_storage";

    private ObservableList<FileWrapper> localStorageModel = FXCollections.observableArrayList();


    @FXML
    Hyperlink hlCloud;

    @FXML
    private TableView<FileWrapper> tableLocal;

    @FXML
    private TableColumn<FileWrapper, String> colLocalName;

    @FXML
    private TableColumn<FileWrapper, String> colLocalSize;

    private LocalProvider localStorage;


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


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colLocalName.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        colLocalSize.setCellValueFactory(new PropertyValueFactory<>("fileSize"));
        localStorage = new LocalProvider(LOCAL_STORAGE);

        tableLocal.setItems(localStorage.getLocalStorageModel());
//        localStorage.showLocalStorage();
    }

    /**
     * TODO: Отобразить в таблице список локальных файлов
     */
    private void showLocalStorage(){
        FileWrapper[] files = FileHelper.listFiles(LOCAL_STORAGE)
                .stream()
                .map(f -> new FileWrapper(f.getName(), f.length()))
                .toArray(FileWrapper[] ::new);

        prepareModel(localStorageModel, files);
        tableLocal.setItems(localStorage.getLocalStorageModel());
    }

    /**
     * TODO: Подготовить содержимое таблицы
     */
    private <T> void prepareModel( ObservableList<T> list, T[] elements) {
        list.removeAll();
        for (T e : elements) {
            list.add(e);
        }
    }
}