package rayCastWorld;

import game.GameContainer;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    public ImageView imgView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        SecondIterationFirstPersonShooter fps = new SecondIterationFirstPersonShooter();
        GameContainer gc = new GameContainer(
                fps,
                (int) imgView.getFitWidth(),
                (int) imgView.getFitHeight(),
                imgView
        );
        imgView.setImage(gc.getImg());
        gc.getTimer().start();
    }

}
