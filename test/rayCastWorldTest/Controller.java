package rayCastWorldTest;

import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import olcPGEApproach.GameContainer;
import rayCastWorldTest.third.Iteration3FPS;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    public ImageView imgView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Iteration3FPS fps = new Iteration3FPS();
        GameContainer gc = new GameContainer(
                fps,
                1280, // 426
                820, // 240
                imgView
        );
        imgView.setImage(gc.getImg());
        gc.getTimer().start();
    }

}
