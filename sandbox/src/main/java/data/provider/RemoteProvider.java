package data.provider;

import data.repository.file.FileHelper;

import java.nio.file.Paths;

public class RemoteProvider {

    private String storagePath;

    public RemoteProvider(String storagePath) {
        this.storagePath = storagePath;
    }

    public boolean createDirectory(String dir) {
        return FileHelper.createDirectory(Paths.get(storagePath, dir));
    }

}
