package orthographicViewer;

import olcPGEApproach.vectors.points2d.Vec2di;

import java.util.ArrayList;

/**
 * This class represents a world
 * of cells
 */
public class World {

    /**
     * The cells of the world
     * it is store as one dimensional
     * array what stores all the
     * cells.
     * To make it two dimensions,
     * "X + width * Y" trick is used
     */
    private ArrayList<Cell> cells;

    /**
     * The dimensions od the world
     * width and height
     */
    private Vec2di size;

    /**
     * Void constructor
     */
    public World() {
        size = new Vec2di();
        cells = new ArrayList<>();
    }

    /**
     * Parametrized constructor
     * @param width width of the world, in num of tiles
     * @param height height of the world, in num of tiles
     */
    public World(int width, int height) {
        this();
        size.setX(width);
        size.setY(height);
        reset();
    }

    /**
     * Sets all the cells to null cells
     */
    public void reset() {
        for ( int i = 0; i < size.getX() * size.getY(); i++ ) {
            cells.add(new Cell());
        }
    }

    /**
     * This method test if the x and y position
     * is inside the bounds of the world
     */
    private boolean isCellInsideWorld(int x, int y) {
        return x >= 0 && x < size.getX() && y >= 0 && y < size.getY();
    }

    /**
     * This method gets a cell
     */
    public Cell getCell(int x, int y) {
        if ( isCellInsideWorld(x, y) ) {
            return cells.get(y * size.getX() + x);
        } else {
            return null;
        }
    }

    public Cell getCell(Vec2di v) {
        return getCell(v.getX(), v.getY());
    }

    /**
     * This method sets a cell to
     * a given one
     */
    public void setCell(int x, int y, Cell c) {
        if ( isCellInsideWorld(x, y) ) {
            cells.set(y * size.getX() + x, c);
        }
    }

    public void setCell(Vec2di v, Cell c) {
        setCell(v.getX(), v.getY(), c);
    }

    /*
    * Getters & Setters
    */

    public ArrayList<Cell> getCells() {
        return cells;
    }

    public void setCells(ArrayList<Cell> cells) {
        this.cells = cells;
    }

    public Vec2di getSize() {
        return size;
    }

    public void setSize(Vec2di size) {
        this.size = size;
    }

}
