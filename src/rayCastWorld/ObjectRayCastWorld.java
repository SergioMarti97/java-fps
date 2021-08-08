package rayCastWorld;

import olcPGEApproach.vectors.points2d.Vec2df;

/**
 * Base class for objects that
 * exits in world
 */
public class ObjectRayCastWorld {

    protected int id = 0;

    protected Vec2df pos = new Vec2df();

    protected Vec2df vel = new Vec2df();

    protected float speed = 0.0f;

    protected float heading = 0.0f;

    protected float radius = 0.5f;

    protected boolean visible = true;

    protected boolean remove = false;

    protected boolean collideWithScenery = false;

    protected boolean collideWithObject = false;

    protected boolean notifyObjectCollision = false;

    protected boolean canBeMoved = true;

    protected boolean isActive = true;

    /**
     * Void constructor
     */
    public ObjectRayCastWorld() {

    }

    /**
     * Constructor
     * @param id the id of the object
     * @param pos the position of the object
     */
    public ObjectRayCastWorld(int id, Vec2df pos) {
        this.id = id;
        this.pos = pos;
    }

    /**
     * Constructor
     * @param id the id of the objects
     * @param pos the position of the object
     * @param vel the velocity of the object
     */
    public ObjectRayCastWorld(int id, Vec2df pos, Vec2df vel) {
        this.id = id;
        this.pos = pos;
        this.vel = vel;
    }

    public void updateSpeed(float walkSpeed) {
        speed = walkSpeed;
        vel.setX((float)Math.cos(heading));
        vel.setY((float)Math.sin(heading));
    }

    public void walk(float walkSpeed) {
        pos.addToX((float)Math.sin(heading) * walkSpeed);
        pos.addToY((float)Math.cos(heading) * walkSpeed);
    }

    public void calStrafeSpeed(float strafeSpeed) {
        speed = strafeSpeed;
        vel = new Vec2df((float)Math.cos(heading), (float)Math.sin(heading) * speed);
        vel = (Vec2df) vel.perpendicular();
    }

    public void turn(float turnSpeed) {
        heading += turnSpeed;
        if ( heading < -3.14159f ) {
            heading += 2.0f * -3.14159f;
        }
        if ( heading > 3.14159f ) {
            heading -= 2.0f * 3.14159f;
        }
    }

    public void stop() {
        speed = 0;
        vel.setX(0);
        vel.setY(0);
    }

    // Getters & Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Vec2df getPos() {
        return pos;
    }

    public void setPos(Vec2df pos) {
        this.pos = pos;
    }

    public Vec2df getVel() {
        return vel;
    }

    public void setVel(Vec2df vel) {
        this.vel = vel;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getHeading() {
        return heading;
    }

    public void setHeading(float heading) {
        this.heading = heading;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isRemove() {
        return remove;
    }

    public void setRemove(boolean remove) {
        this.remove = remove;
    }

    public boolean isCollideWithScenery() {
        return collideWithScenery;
    }

    public void setCollideWithScenery(boolean collideWithScenery) {
        this.collideWithScenery = collideWithScenery;
    }

    public boolean isCollideWithObject() {
        return collideWithObject;
    }

    public void setCollideWithObject(boolean collideWithObject) {
        this.collideWithObject = collideWithObject;
    }

    public boolean isNotifyObjectCollision() {
        return notifyObjectCollision;
    }

    public void setNotifyObjectCollision(boolean notifyObjectCollision) {
        this.notifyObjectCollision = notifyObjectCollision;
    }

    public boolean isCanBeMoved() {
        return canBeMoved;
    }

    public void setCanBeMoved(boolean canBeMoved) {
        this.canBeMoved = canBeMoved;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
