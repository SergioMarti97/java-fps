package orthographicViewer;

import olcPGEApproach.gfx.images.Image;
import rayCastWorld.CellSide;

import java.util.HashMap;

/**
 * This class represents a structure
 * of a cell in the world
 * It stores if it is a wall or not,
 * and the images what is going to
 * render when is a wall or floor
 *
 * In future, it must store more information
 * to manages different behaviour,
 * to make the world more interesting
 * such as:
 *  - can it be broken by bombs?
 *  - can it be moved?
 *  - can it emit light?
 *  - etc...
 */
public class Cell {

    /**
     * If the Cell is a wall
     */
    private boolean isWall = false;

    /**
     * The ids to store the sprites
     * It's only needed to store
     * the position of the image on
     * TileImage (which contains all images)
     */
    private HashMap<CellSide, Image> faces;

    /**
     * Null constructor
     */
    public Cell() {
        faces = new HashMap<>();
    }

    /**
     * Parametrized constructor
     */
    public Cell(boolean isWall, HashMap<CellSide, Image> faces) {
        this.isWall = isWall;
        this.faces = faces;
    }

    /*
    * Getters & Setters
    */

    public boolean isWall() {
        return isWall;
    }

    public void setWall(boolean wall) {
        isWall = wall;
    }

    public HashMap<CellSide, Image> getFaces() {
        return faces;
    }

    public void setFaces(HashMap<CellSide, Image> faces) {
        this.faces = faces;
    }

}
