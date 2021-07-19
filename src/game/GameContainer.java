package game;

import javafx.scene.Node;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;

/**
 * This class contains the inner workings of the game
 */
public class GameContainer {

    private AbstractGame game;

    private WritableImage img;

    private CustomTimer timer;

    private Renderer renderer;

    private Input input;

    private Node node;

    /**
     * Constructor
     */
    public GameContainer(AbstractGame game, int screenWidth, int screenHeight, Node node) {
        this.game = game;
        this.node = node;
        img = new WritableImage(screenWidth, screenHeight);
        renderer = new Renderer(new int[screenWidth * screenHeight], screenWidth, screenHeight);
        input = new Input(node);
        timer = new CustomTimer();
        timer.setUpdater(this::update);
        timer.setRenderer(this::render);
        game.initialize(this);
    }

    private void update(float elapsedTime) {
        input.update();
        game.update(this, elapsedTime);
    }

    private void render() {
        img.getPixelWriter().setPixels(
                0, 0,
                renderer.getW(), renderer.getH(),
                PixelFormat.getIntArgbInstance(),
                renderer.getP(),
                0, renderer.getW());
        game.render(this);
    }

    // Getters

    public WritableImage getImg() {
        return img;
    }

    public Renderer getRenderer() {
        return renderer;
    }

    public CustomTimer getTimer() {
        return timer;
    }

    public Input getInput() {
        return input;
    }

    public Node getNode() {
        return node;
    }

}
