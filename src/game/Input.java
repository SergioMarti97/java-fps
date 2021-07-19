package game;

import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

import java.util.HashMap;

/**
 * This class manages the user
 * input
 */
public class Input {

    private final Node node;

    private final HashMap<KeyCode, Boolean> mapKeysState;

    private final HashMap<KeyCode, Boolean> mapKeysStateLast;

    private final HashMap<MouseButton, Boolean> mapButtons;

    private final HashMap<MouseButton, Boolean> mapButtonLast;

    private double mouseX;

    private double mouseY;

    private int scroll;

    public Input(Node node) {
        this.node = node;

        mouseX = 0.0;
        mouseY = 0.0;
        scroll = 0;

        mapKeysState = new HashMap<>();
        mapKeysStateLast = new HashMap<>();
        for ( KeyCode keyCode : KeyCode.values() ) {
            mapKeysState.put(keyCode, Boolean.FALSE);
        }

        mapButtons = new HashMap<>();
        mapButtonLast = new HashMap<>();
        for ( MouseButton btn : MouseButton.values() ) {
            mapButtons.put(btn, Boolean.FALSE);
        }

        setNodeClickEvents();
        setNodeKeyEvents();
        node.setOnScroll(event -> scroll += event.getDeltaY());
    }

    private void setNodeKeyEvents() {
        node.setOnKeyPressed(event -> {
            mapKeysState.replace(event.getCode(), Boolean.TRUE);
        });

        node.setOnKeyReleased(event -> {
            mapKeysState.replace(event.getCode(), Boolean.FALSE);
        });
    }

    private void setNodeClickEvents() {
        node.setOnMouseMoved(event -> {
            mouseX = event.getX();
            mouseY = event.getY();
        });
        node.setOnMousePressed(event -> {
            System.out.println("You have press: " + event.getButton());
            mapButtons.replace(event.getButton(), Boolean.TRUE);
        });
        node.setOnMouseReleased(event -> {
            System.out.println("You have release: " + event.getButton());
            mapButtons.replace(event.getButton(), Boolean.FALSE);
        });
    }

    public void update() {
        mapKeysStateLast.clear();
        mapKeysStateLast.putAll(mapKeysState);

        mapButtonLast.clear();
        mapButtonLast.putAll(mapButtons);
    }

    public boolean isKey(KeyCode keyCode) {
        return mapKeysState.get(keyCode);//return keysState[keyCode];
    }

    public boolean isKeyUp(KeyCode keyCode) {
        return !mapKeysState.get(keyCode) && mapKeysStateLast.get(keyCode);
    }

    public boolean isKeyDown(KeyCode keyCode) {
        return mapKeysState.get(keyCode) && !mapKeysStateLast.get(keyCode);
    }

    public boolean isKeyHeld(KeyCode keyCode) {
        boolean isPressed = mapKeysState.get(keyCode);
        boolean wasPressed = mapKeysStateLast.get(keyCode);
        return isPressed && wasPressed;
    }

    public boolean isButton(MouseButton button) {
        return mapButtons.get(button);
    }

    public boolean isButtonUp(MouseButton button) {
        return !mapButtons.get(button) && mapButtonLast.get(button);
    }

    public boolean isButtonDown(MouseButton button) {
        return mapButtons.get(button) && !mapButtonLast.get(button);
    }

    public boolean isButtonHeld(MouseButton button) {
        return mapButtons.get(button) && mapButtonLast.get(button);
    }

    public Node getNode() {
        return node;
    }

    public double getMouseX() {
        return mouseX;
    }

    public double getMouseY() {
        return mouseY;
    }

    public int getScroll() {
        return scroll;
    }

}
