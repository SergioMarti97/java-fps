package rayCastWorld.objects;

import olcPGEApproach.vectors.points2d.Vec2df;

public class MovingObj extends Obj {

    protected Vec2df vel = new Vec2df();

    protected float speed = 0.0f;

    public MovingObj() {

    }

    /**
     * Constructor
     * @param id the id of the objects
     * @param pos the position of the object
     * @param vel the velocity of the object
     */
    public MovingObj(int id, Vec2df pos, Vec2df vel) {
        super(id, pos);
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

    public void stop() {
        speed = 0;
        vel.setX(0);
        vel.setY(0);
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


}
