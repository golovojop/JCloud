package data.provider;

import data.repository.file.FileHelper;
import domain.FileWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.nio.file.Paths;

public class RemoteProvider extends FileProvider {

    public RemoteProvider(String storagePath) {
        super(storagePath);
    }

    public boolean createDirectory(String dir) {
        return FileHelper.createDirectory(Paths.get(storagePath, dir));
    }
}
