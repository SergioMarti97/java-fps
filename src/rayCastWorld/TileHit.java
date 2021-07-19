package rayCastWorld;

import points2d.Vec2df;
import points2d.Vec2di;

/**
 * A convenient utility struct to store all the info required to understand how a ray
 * has hit a specific tile
 */
public class TileHit {

    private Vec2di tilePos = new Vec2di();

    private Vec2df hitPos = new Vec2df();

    private float length = 0.0f;

    private float sampleX = 0.0f;

    private Engine.CellSide side = Engine.CellSide.NORTH;

    public TileHit() {

    }

    public TileHit(Vec2di tilePos, Vec2df hitPos, float length, float sampleX, Engine.CellSide side) {
        this.tilePos = tilePos;
        this.hitPos = hitPos;
        this.length = length;
        this.sampleX = sampleX;
        this.side = side;
    }

    public Vec2di getTilePos() {
        return tilePos;
    }

    public void setTilePos(Vec2di tilePos) {
        this.tilePos = tilePos;
    }

    public Vec2df getHitPos() {
        return hitPos;
    }

    public void setHitPos(Vec2df hitPos) {
        this.hitPos = hitPos;
    }

    public float getLength() {
        return length;
    }

    public void setLength(float length) {
        this.length = length;
    }

    public float getSampleX() {
        return sampleX;
    }

    public void setSampleX(float sampleX) {
        this.sampleX = sampleX;
    }

    public Engine.CellSide getSide() {
        return side;
    }

    public void setSide(Engine.CellSide side) {
        this.side = side;
    }

}
