package game;

/**
 * This interface contains the main methods
 * of any game
 */
public interface AbstractGame {

    void initialize(GameContainer gc);

    void update(GameContainer gc, float elapsedTime);

    void render(GameContainer gc);

}
