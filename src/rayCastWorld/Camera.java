package rayCastWorld;

import com.sun.javafx.geom.Vec2f;

/**
 * This class represents a camera
 * in a ray cast world engine
 */
public class Camera {

    /**
     * The position of the camera
     */
    private Vec2f pos;

    /**
     * The angle of the camera
     */
    private float angle;

    /**
     * The field of view of the camera
     */
    private float fieldOfView;

    /**
     * Constructor
     * @param pos the position of the camera
     * @param angle the angle of the camera
     * @param fieldOfView the field of view of the camera
     */
    public Camera(Vec2f pos, float angle, float fieldOfView) {
        this.pos = pos;
        this.angle = angle;
        this.fieldOfView = fieldOfView;
    }

    /*
    * Getters and Setters
    */

    public Vec2f getPos() {
        return pos;
    }

    public void setPos(Vec2f pos) {
        this.pos = pos;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public float getFieldOfView() {
        return fieldOfView;
    }

    public void setFieldOfView(float fieldOfView) {
        this.fieldOfView = fieldOfView;
    }

}
