package data.repository.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileHelper {

    /**
     * TODO: Получить список файлов в папке path
     */
    public static List<File> listFiles(String path) {
        return listFiles(Paths.get(path));
    }

    /**
     * TODO: Получить список файлов в папке path
     */
    public static List<File> listFiles(Path path) {
        List<File> result = null;

        try (Stream<Path> walk = Files.walk(path)) {
            result = walk.filter(Files::isRegularFile)
                    .map(p -> p.toFile())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * TODO: Создать файл (пока без атрибутов)
     */
    public static Path createFile(Path filePath) {
        if(!Files.exists(filePath)) {
            try {
                return Files.createFile(filePath);
            } catch (IOException e) {e.printStackTrace();}
        }
        return null;
    }

    /**
     * TODO: Создать каталог
     */
    public static boolean createDirectory(Path targetDir) {
        boolean result = false;
        if(!Files.exists(targetDir)) {
            try {
                Files.createDirectories(targetDir);
                result = true;
            } catch (IOException e) {e.getStackTrace();}

        } else {
            System.out.println("Directory " + targetDir.toString() + " already exists");
        }
        return result;
    }

    /**
     * TODO: Удалить, если существует
     */
    public static boolean deleteFile(Path path) {
        try {
            return Files.deleteIfExists(path);
        } catch (IOException e) {e.printStackTrace();}

        return false;
    }
}
