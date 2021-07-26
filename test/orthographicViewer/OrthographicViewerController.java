package orthographicViewer;

import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import olcPGEApproach.GameContainer;

import java.net.URL;
import java.util.ResourceBundle;

public class OrthographicViewerController implements Initializable {

    public ImageView imgView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        OrthographicViewer ov = new OrthographicViewer();
        GameContainer gc = new GameContainer(
                ov,
                (int) imgView.getFitWidth(),
                (int) imgView.getFitHeight(),
                imgView
        );
        imgView.setImage(gc.getImg());
        gc.getTimer().start();
    }

}
