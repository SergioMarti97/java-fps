package rayCastWorld.physics.rectangles;

import olcPGEApproach.vectors.points2d.Vec2df;

public class MovingRectangle extends Rectangle {

    /**
     * The velocity of the rectangle
     */
    private Vec2df vel;

    /**
     * Constructor
     */
    public MovingRectangle(Vec2df pos, Vec2df size) {
        super(pos, size);
        this.vel = new Vec2df();
    }

    /**
     * Constructor
     */
    public MovingRectangle(Vec2df pos, Vec2df size, Vec2df vel) {
        super(pos, size);
        this.vel = vel;
    }

    public void update(float elapsedTime) {
        pos.addToX(vel.getX() * elapsedTime);
        pos.addToY(vel.getY() * elapsedTime);
    }

    // Getters and Setters

    public Vec2df getVel() {
        return vel;
    }

    public void setVel(Vec2df vel) {
        this.vel = vel;
    }

}
