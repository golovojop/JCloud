package data.provider;

import data.repository.file.FileHelper;
import domain.FileWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class LocalProvider extends FileProvider {

    public LocalProvider(String storagePath) {
        super(storagePath);
    }

}
