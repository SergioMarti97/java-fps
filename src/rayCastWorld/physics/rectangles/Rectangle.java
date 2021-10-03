package rayCastWorld.physics.rectangles;

import olcPGEApproach.vectors.points2d.Vec2df;

/**
 * This class represents a rectangle,
 * in this implementation, a rectangle
 * stores the top left x and y position
 * and the dimensions (width and height)
 */
public class Rectangle {

    /**
     * The position top left of the rectangle
     */
    protected Vec2df pos;

    /**
     * The width and height of the rectangle
     */
    protected Vec2df size;

    /**
     * Constructor
     */
    public Rectangle(Vec2df pos, Vec2df size) {
        this.pos = pos;
        this.size = size;
    }

    // Getters and Setters

    public Vec2df getPos() {
        return pos;
    }

    public void setPos(Vec2df pos) {
        this.pos = pos;
    }

    public Vec2df getSize() {
        return size;
    }

    public void setSize(Vec2df size) {
        this.size = size;
    }

}
