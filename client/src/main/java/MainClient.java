/**
 * Materials:
 * https://www.mkyong.com/java/java-how-to-list-all-files-in-a-directory/
 * https://dzone.com/articles/building-simple-data-access-layer-using-jdbc
 *
 * NIO:
 * http://tutorials.jenkov.com/java-nio/overview.html
 * https://www.baeldung.com/java-nio-selector
 * https://www.baeldung.com/java-nio-2-async-channels
 *
 * Streams:
 * https://winterbe.com/posts/2014/07/31/java8-stream-tutorial-examples/
 */


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainClient extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("main_window.fxml"));
        primaryStage.setTitle("GUI Pattern");
        primaryStage.setScene(new Scene(root, 800, 500));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}