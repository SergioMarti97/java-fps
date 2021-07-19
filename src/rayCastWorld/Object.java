package rayCastWorld;

import points2d.Vec2df;

/**
 * Base class for objects that
 * exits in world
 */
public class Object {

    private int id = 0;

    private Vec2df pos = new Vec2df();

    private Vec2df vel = new Vec2df();

    private float speed = 0.0f;

    private float heading = 0.0f;

    private float radius = 0.5f;

    private boolean visible = true;

    private boolean remove = false;

    private boolean collideWithScenery = false;

    private boolean collideWithObject = false;

    private boolean notifyObjectCollision = false;

    private boolean canBeMoved = true;

    private boolean isActive = true;

    public Object() {

    }

    public Object(int id, Vec2df pos) {
        this.id = id;
        this.pos = pos;
    }

    public Object(int id, Vec2df pos, Vec2df vel) {
        this.id = id;
        this.pos = pos;
        this.vel = vel;
    }

    public void walk(float walkSpeed) {
        speed = walkSpeed;
        vel = new Vec2df((float)Math.cos(heading), (float)Math.sin(heading) * speed);
    }

    public void strafe(float strafeSpeed) {
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
