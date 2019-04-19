package controllers;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;


public class DeleteController {

    public static boolean confirmDelete = false;

    public DeleteController(String info) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle("Удаление af");
        alert.setContentText("Удалить запись\n" + info + " ?");

        Optional<ButtonType> result = alert.showAndWait();

        confirmDelete = result.get() == ButtonType.OK;
    }
}
