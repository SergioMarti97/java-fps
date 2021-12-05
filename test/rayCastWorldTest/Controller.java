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
                426, // 426
                240, // 240
                imgView
        );
        imgView.setImage(gc.getImg());
        /*imgView.setOnKeyPressed((value)->{
            if (value.getCode() == KeyCode.E) {
                Stage stage = new Stage();
                stage.setTitle("Options");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("options/LayoutOptions.fxml"));
                Parent root;
                try {
                    root = loader.load();
                    stage.setScene(new Scene(root));
                    stage.initModality(Modality.WINDOW_MODAL);
                    stage.show();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        });*/
        gc.getTimer().start();
    }

}
