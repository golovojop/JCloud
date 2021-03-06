package controllers;

import domain.Customer;
import static utils.Debug.*;
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

    @FXML
    public void auth(ActionEvent actionEvent) {
        dp(this, "auth " + login.getText() + ":" + password.getText());
        backController.signInCustomer(new Customer(login.getText(), password.getText()));
        globParent.getScene().getWindow().hide();
    }
}
