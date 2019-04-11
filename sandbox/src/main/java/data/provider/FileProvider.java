package data.provider;

import data.repository.file.FileHelper;
import domain.FileWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public abstract class FileProvider {

    protected ObservableList<FileWrapper> storageModel;
    protected String storagePath;

    public FileProvider(String storagePath) {
        this.storageModel = FXCollections.observableArrayList();
        this.storagePath = storagePath;
    }

    /**
     * TODO: Получить список файлов
     */
    private void collectFiles(){

        FileWrapper[] files = FileHelper.listFiles(storagePath)
                .stream()
                .map(f -> new FileWrapper(f.getName(), f.length()))
                .toArray(FileWrapper[] ::new);

        prepareObservableList(storageModel, files);
    }

    /**
     * TODO: Получить контент для табличного отображения
     */
    public ObservableList<FileWrapper> getStorageModel() {
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
