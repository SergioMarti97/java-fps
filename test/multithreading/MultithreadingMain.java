package multithreading;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * This class is for test the ability to do the multithreading
 * to render everything with the First Person Shooter
 * renderer
 */
public class MultithreadingMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("multithreadingLayout.fxml")));
        primaryStage.setTitle("Multithreading First Person Shooter");
        primaryStage.setScene(new Scene(root, 1280, 720));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
