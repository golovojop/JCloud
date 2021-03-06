package data.provider;

import data.repository.file.FileHelper;
import domain.FileDescriptor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FileProvider {

    public FileProvider() {
    }

    /**
     * TODO: Получить контент для табличного отображения
     */
    public ObservableList<FileDescriptor> getStorageModel(FileDescriptor[] files) {
        ObservableList<FileDescriptor> storageModel = FXCollections.observableArrayList();
        prepareObservableList(storageModel, files);
        return storageModel;
    }

    /**
     * TODO: Получить контент для табличного отображения
     */
    public ObservableList<FileDescriptor> getStorageModel(String storagePath) {
        ObservableList<FileDescriptor> storageModel = FXCollections.observableArrayList();
        prepareObservableList(storageModel, collectFiles(storagePath));
        return storageModel;
    }

    /**
     * TODO: Получить список файлов
     */
    public FileDescriptor[] collectFiles(String path){
        return collectFiles(Paths.get(path));
    }

    public FileDescriptor[] collectFiles(Path path){
        return FileHelper.listFiles(path)
                .stream()
                .map(f -> new FileDescriptor(f.getName(), f.length()))
                .toArray(FileDescriptor[] ::new);
    }

    /**
     * TODO: Создать файл
     */
    public Path createFile(Path filePath) {
        return FileHelper.createFile(filePath);
    }

    /**
     * TODO: Создать каталог
     */
    public boolean createDirectory(Path targetDir) {
        return FileHelper.createDirectory(targetDir);
    }

    /**
     * TODO: Удалить файл
     */
    public boolean deleteFile(Path path) {
        return FileHelper.deleteFile(path);
    }

    /**
     * TODO: Cодержимое строк таблицы
     */
    public static <T> void prepareObservableList(ObservableList<T> list, T[] elements) {
        list.removeAll();
        for (T e : elements) {
            list.add(e);
        }
    }
}
