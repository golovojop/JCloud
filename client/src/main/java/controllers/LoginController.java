package controllers;

import domain.Customer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class LoginController {
    @FXML
    TextField login;

    @FXML
    PasswordField password;

    @FXML
    VBox globParent;

    public MainController backController;
    public int id;

    @FXML
    public void auth(ActionEvent actionEvent) {
        System.out.println(login.getText() + " " + password.getText());
        System.out.println("id = " + id);
        backController.signInCustomer(new Customer(login.getText(), password.getText()));
        globParent.getScene().getWindow().hide();
    }
}
