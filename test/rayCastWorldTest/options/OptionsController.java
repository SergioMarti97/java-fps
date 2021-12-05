package rayCastWorldTest.options;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class OptionsController implements Initializable {

    @FXML
    private ComboBox<String> comboBoxResolution;

    @FXML
    private TextField txtFieldScreenWidth;

    @FXML
    private TextField txtFieldScreenHeight;

    @FXML
    private Slider sliderFOV;

    @FXML
    private Label lblFOV;

    @FXML
    private Slider sliderRenderingWidth;

    @FXML
    private Label lblRenderingWidth;

    @FXML
    private ComboBox<String> comboBoxRenderingMethod;

    /**
     * Flag to said if the user has finished to edit the options
     */
    private boolean isFinish = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    // Getters and Setters

    public boolean isFinish() {
        return isFinish;
    }

    public void setFinish(boolean finish) {
        isFinish = finish;
    }

}
