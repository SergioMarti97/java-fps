package physics;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import olcPGEApproach.GameContainer;
import physics.rectangles.PhysicsTestGame;

import java.net.URL;
import java.util.ResourceBundle;

public class PhysicsController implements Initializable {

    @FXML
    private ImageView imgView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        PhysicsTestGame game = new PhysicsTestGame();
        GameContainer gc = new GameContainer(
                game,
                640,
                360,
                imgView
        );
        imgView.setImage(gc.getImg());
        gc.getTimer().start();
    }

}
