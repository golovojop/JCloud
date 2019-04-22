package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;

public class ProgressView {
    @FXML
    HBox globParent;

    @FXML
    ProgressBar progressBar;

    public void setWidth() {
        progressBar.setPrefWidth(250);
    }
    public void update(Double progress) {
        progressBar.setProgress(progress);
    }
    public void close() {
        globParent.getScene().getWindow().hide();
    }
}
