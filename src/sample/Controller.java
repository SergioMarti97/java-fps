package sample;

import fps.FirstPersonShooterGame;
import game.GameContainer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private ImageView imgView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        GameContainer gameContainer = new GameContainer(
                new FirstPersonShooterGame(),
                (int) imgView.getFitWidth(),
                (int) imgView.getFitHeight(),
                imgView);
        imgView.setImage(gameContainer.getImg());
        gameContainer.getTimer().start();
    }

}
