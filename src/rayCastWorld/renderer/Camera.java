package rayCastWorld.renderer;

import olcPGEApproach.vectors.points2d.Vec2df;
import rayCastWorld.ObjectRayCastWorld;

/**
 * This class represents a camera
 * of the ray casting world engine
 */
public class Camera extends ObjectRayCastWorld {

    /**
     * The field of vision of the camera, on radians
     */
    private float fieldOfVision = (float) (Math.PI / 3.0f);

    /**
     * The max rendering distance
     */
    private float depth = 32.0f;

    /**
     * The minimum distance to the objects
     * to get render
     */
    private float minDistToObject = 0.5f;

    /**
     * Ray angle between the camera position and the hit,
     * the hit is the hit of the cast ray with the walls or objects
     */
    private float rayAngle = 0;

    /**
     * Similar to the ray angle, it is the x and y of the
     * ray angle, is needed sinus and cosine to cal
     */
    private final Vec2df rayDirection;

    /**
     * The plane tile position to mode 7
     */
    private final Vec2df planeTilePosMode7;

    /**
     * Constructor
     */
    public Camera() {
        rayDirection = new Vec2df();
        planeTilePosMode7 = new Vec2df();
    }

    /**
     * ... then project polar coordinate (r, theta) from camera into screen (x, y), again
     * compensating with cosine to remove fisheye
     */
    public void calTilePosMode7(float planeZ) {
        planeTilePosMode7.setX(pos.getX() + rayDirection.getX() * planeZ * 2.0f / (float)Math.cos(rayAngle - heading));
        planeTilePosMode7.setY(pos.getY() + rayDirection.getY() * planeZ * 2.0f / (float)Math.cos(rayAngle - heading));
    }

    /**
     * This method cals the ray angle for a
     * given x screen position of the screen
     * @param x the position x of the screen
     * @param width the width of the screen
     */
    public void calRayAngle(int x, int width) {
        rayAngle = (heading - fieldOfVision / 2.0f) + ((float) x / (float) width) * fieldOfVision;
    }

    /**
     * This method cals the ray direction for a
     * given x screen position of the screen
     * @param x the position x of the screen
     * @param width the width of the screen
     */
    public void calRayDirection(int x, int width) {
        // La dirección del rayo se podría calcular cada vez que se
        // modifica el ángulo del jugador
        calRayAngle(x, width);
        rayDirection.setX((float) Math.sin(rayAngle));
        rayDirection.setY((float) Math.cos(rayAngle));
    }

    /**
     * This method calculates the distance from the
     * camera to the hit (the hit of the cast ray to the wall)
     * @return the distance from the camera to the hit
     */
    public float calDistance(TileHit hit) {
        float distX = hit.getHitPos().getX() - pos.getX();
        float distY = hit.getHitPos().getY() - pos.getY();
        float mag = (float) Math.sqrt(distX * distX + distY * distY);
        return mag * (float) Math.cos(rayAngle - heading);
    }

    // Getters and Setters

    public float getRayAngle() {
        return rayAngle;
    }

    public Vec2df getRayDirection() {
        return rayDirection;
    }

    public Vec2df getTilePosMode7() {
        return planeTilePosMode7;
    }

    public float getDepth() {
        return depth;
    }

    public void setDepth(float depth) {
        this.depth = depth;
    }

    public float getFieldOfVision() {
        return fieldOfVision;
    }

    public void setFieldOfVision(float fieldOfVision) {
        this.fieldOfVision = fieldOfVision;
    }

    public float getMinDistToObject() {
        return minDistToObject;
    }

    public void setMinDistToObject(float minDistToObject) {
        this.minDistToObject = minDistToObject;
    }

}
