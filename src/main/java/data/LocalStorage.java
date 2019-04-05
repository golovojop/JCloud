package data;

import data.file.FileHelper;
import domain.FileWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class LocalStorage {

    private ObservableList<FileWrapper> localStorageModel;
    private String storagePath;


    public LocalStorage(String storagePath) {
        this.localStorageModel = FXCollections.observableArrayList();
        this.storagePath = storagePath;
    }

    /**
     * TODO: Подготовить данные для отображения
     */
    public ObservableList<FileWrapper> getLocalStorageModel() {
        collectFiles();
        return localStorageModel;
    }

    /**
     * TODO: Получить список файлов
     */
    private void collectFiles(){

        FileWrapper[] files = FileHelper.listFiles(storagePath)
                .stream()
                .map(f -> new FileWrapper(f.getName(), f.length()))
                .toArray(FileWrapper[] ::new);

        prepareObservableList(localStorageModel, files);
    }

    /**
     * TODO: Cодержимое строк таблицы
     */
    private <T> void prepareObservableList( ObservableList<T> list, T[] elements) {
        list.removeAll();
        for (T e : elements) {
            list.add(e);
        }
    }

}
