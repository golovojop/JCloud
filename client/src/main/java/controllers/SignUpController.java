package controllers;

import domain.Customer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class SignUpController {

    @FXML
    TextField login;

    @FXML
    PasswordField password1;

    @FXML
    PasswordField password2;

    @FXML
    VBox globParent;

    public MainController backController;
    public int id;

    @FXML
    public void signUp(ActionEvent actionEvent) {

        if(password1.getText().equals(password2.getText())) {
            backController.signUpCustomer(new Customer(login.getText(), password1.getText()));

        } else {
            System.out.println("Passwords are not equal");
        }

        globParent.getScene().getWindow().hide();
    }
}
