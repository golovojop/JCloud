package data.provider;

import data.repository.file.FileHelper;
import domain.FileDescriptor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public abstract class FileProvider {

    protected ObservableList<FileDescriptor> storageModel;
    protected String storagePath;

    public FileProvider(String storagePath) {
        this.storageModel = FXCollections.observableArrayList();
        this.storagePath = storagePath;
    }

    /**
     * TODO: Получить список файлов
     */
    private void collectFiles(){

        FileDescriptor[] files = FileHelper.listFiles(storagePath)
                .stream()
                .map(f -> new FileDescriptor(f.getName(), f.length()))
                .toArray(FileDescriptor[] ::new);

        prepareObservableList(storageModel, files);
    }

    /**
     * TODO: Получить контент для табличного отображения
     */
    public ObservableList<FileDescriptor> getStorageModel() {
        collectFiles();
        return storageModel;
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
