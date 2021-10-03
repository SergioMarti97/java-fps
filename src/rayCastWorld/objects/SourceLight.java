package rayCastWorld.objects;

import olcPGEApproach.vectors.points2d.Vec2df;
import rayCastWorld.renderer.RayShadowing;

/**
 * This class represents a object from the ray cast world
 * what is a source of light
 */
public class SourceLight extends Obj {

    private final float MIN_LIGHT = 0.2f;

    private float lightSourceDistance = 0;

    private float maxLight = 1.0f;

    private float maxDistance;

    public SourceLight(int id, Vec2df pos, float maxDistance, float maxLight) {
        super(id, pos);
        this.maxDistance = maxDistance;
        this.maxLight = maxLight;
    }

    public void calLight(int planeTileX, int planeTileY, float planeSampleX, float planeSampleY) {
        float pixelPosX = (float)planeTileX + planeSampleX;
        float pixelPosY = (float)planeTileY + planeSampleY;
        lightSourceDistance =
                (pos.getX() - pixelPosX) * (pos.getX() - pixelPosX) +
                        (pos.getY() - pixelPosY) * (pos.getY() - pixelPosY);
    }

    public boolean isNear() {
        return lightSourceDistance < maxDistance;
    }

    public float getLightSourceDistance() {
        return Math.max(MIN_LIGHT, RayShadowing.calShadow(lightSourceDistance, maxDistance));
    }

    public float getMaxLight() {
        return maxLight;
    }

    public float getMaxDistance() {
        return maxDistance;
    }

    public void setMaxLight(float maxLight) {
        this.maxLight = maxLight;
    }

    public void setMaxDistance(float maxDistance) {
        this.maxDistance = maxDistance;
    }
}
