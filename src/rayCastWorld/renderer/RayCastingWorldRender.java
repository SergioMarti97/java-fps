package rayCastWorld.renderer;

import olcPGEApproach.gfx.Renderer;
import olcPGEApproach.vectors.points2d.Vec2df;
import olcPGEApproach.vectors.points2d.Vec2di;
import rayCastWorld.CellSide;
import rayCastWorld.ObjectRayCastWorld;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * The ray cast world renderer
 */
public abstract class RayCastingWorldRender extends Renderer {

    /**
     * The depth buffer
     */
    protected float[] depthBuffer;

    /**
     * The camera to render the world
     */
    protected Camera camera;

    protected final float MIN_DISTANCE_OBJECT = 0.5f;

    /**
     * Constructor
     * @param p the array of pixels of the image
     * @param w the width of the screen
     * @param h the height of the screen
     */
    public RayCastingWorldRender(int[] p, int w, int h) {
        super(p, w, h);
        depthBuffer = new float[super.getW()];
        Arrays.fill(depthBuffer, 0.0f);
        camera = new Camera();
    }

    /**
     * @param r the renderer
     */
    public RayCastingWorldRender(Renderer r) {
        this(r.getP(), r.getW(), r.getH());
    }

    /**
     * DDA Algorithm
     */
    private boolean castRayDDA(Vec2df origin, Vec2df direction, TileHit hit) {
        float yDivideByX = direction.getY() / direction.getX();
        float xDivideByY = direction.getX() / direction.getY();
        Vec2df rayDelta = new Vec2df(
                (float)Math.sqrt(1 + (yDivideByX * yDivideByX)),
                (float)Math.sqrt(1 + (xDivideByY * xDivideByY))
        );

        Vec2di mapCheck = new Vec2di((int)origin.getX(), (int)origin.getY());

        Vec2df rayLength1D = new Vec2df();
        Vec2di stepDistance = new Vec2di();

        if ( direction.getX() < 0 ) {
            stepDistance.setX(-1);
            rayLength1D.setX((origin.getX() - (float)mapCheck.getX()) * rayDelta.getX());
        } else {
            stepDistance.setX(1);
            rayLength1D.setX(((float)(mapCheck.getX() + 1) - origin.getX()) * rayDelta.getX());
        }

        if ( direction.getY() < 0 ) {
            stepDistance.setY(-1);
            rayLength1D.setY((origin.getY() - (float)mapCheck.getY()) * rayDelta.getY());
        } else {
            stepDistance.setY(1);
            rayLength1D.setY(((float)(mapCheck.getY() + 1) - origin.getY()) * rayDelta.getY());
        }

        float distanceWall = 0;
        boolean hitWall = false;
        float sampleX = 0.0f;
        while ( !hitWall && distanceWall < camera.getDepth() ) {
            if ( rayLength1D.getX() < rayLength1D.getY() ) {
                mapCheck.addToX(stepDistance.getX());
                distanceWall = rayLength1D.getX();
                rayLength1D.addToX(rayDelta.getX());
            } else {
                mapCheck.addToY(stepDistance.getY());
                distanceWall = rayLength1D.getY();
                rayLength1D.addToY(rayDelta.getY());
            }

            if ( isLocationSolid((float) mapCheck.getX(), (float) mapCheck.getY()) ) {
                hitWall = true;

                Vec2df intersection = new Vec2df();

                float m = direction.getY() / direction.getX();

                if ( origin.getY() <= mapCheck.getY() ) {
                    if ( origin.getX() <= mapCheck.getX() ) { // West
                        hit.setSide(CellSide.WEST);
                        intersection.setY(m * (mapCheck.getX() - origin.getX()) + origin.getY());
                        intersection.setX((float)(mapCheck.getX()));
                        sampleX = intersection.getY() - (float)Math.floor(intersection.getY());
                    } else if ( origin.getX() >= (mapCheck.getX() + 1) ) { // East
                        hit.setSide(CellSide.EAST);
                        intersection.setY(m * ((mapCheck.getX() + 1) - origin.getX()) + origin.getY());
                        intersection.setX((float)(mapCheck.getX() + 1));
                        sampleX = intersection.getY() - (float)Math.floor(intersection.getY());
                    } else { // Norte
                        hit.setSide(CellSide.NORTH);
                        intersection.setY((float)mapCheck.getY());
                        intersection.setX((mapCheck.getY() - origin.getY()) / m + origin.getX());
                        sampleX = intersection.getX() - (float)Math.floor(intersection.getX());
                    }

                    if ( intersection.getY() < mapCheck.getY() ) { // North? or South?
                        hit.setSide(CellSide.SOUTH);
                        intersection.setY((float)mapCheck.getY());
                        intersection.setX((mapCheck.getY() - origin.getY()) / m + origin.getX());
                        sampleX = intersection.getX() - (float)Math.floor(intersection.getX());
                    }
                } else if ( origin.getY() >= mapCheck.getY() + 1 ) {
                    if ( origin.getX() <= mapCheck.getX() ) { // West
                        hit.setSide(CellSide.WEST);
                        intersection.setY(m * (mapCheck.getX() - origin.getX()) + origin.getY());
                        intersection.setX((float)mapCheck.getX());
                        sampleX = intersection.getY() - (float)Math.floor(intersection.getY());
                    } else if ( origin.getX() >= (mapCheck.getX() + 1) ) { // East
                        hit.setSide(CellSide.EAST);
                        intersection.setY(m * ((mapCheck.getX() + 1) - origin.getX()) + origin.getY());
                        intersection.setX((float)(mapCheck.getX() + 1));
                        sampleX = intersection.getY() - (float)Math.floor(intersection.getY());
                    } else { // South
                        hit.setSide(CellSide.SOUTH);
                        intersection.setY((float)(mapCheck.getY() + 1));
                        intersection.setX(((mapCheck.getY() + 1) - origin.getY()) / m + origin.getX());
                        sampleX = intersection.getX() - (float)Math.floor(intersection.getX());
                    }

                    if ( intersection.getY() > (mapCheck.getY() + 1) ) { // South? or North?
                        hit.setSide(CellSide.NORTH);
                        intersection.setY((float)(mapCheck.getY() + 1));
                        intersection.setX(((mapCheck.getY() + 1)- origin.getY()) / m + origin.getX());
                        sampleX = intersection.getX() - (float)Math.floor(intersection.getX());
                    }
                } else {
                    if ( origin.getX() <= mapCheck.getX() ) { // West
                        hit.setSide(CellSide.WEST);
                        intersection.setY(m * (mapCheck.getX() - origin.getX()) + origin.getY());
                        intersection.setX((float)(mapCheck.getX()));
                        sampleX = intersection.getY() - (float)Math.floor(intersection.getY());
                    } else if ( origin.getX() >= (mapCheck.getX() + 1) ) { // East
                        hit.setSide(CellSide.EAST);
                        intersection.setY(m * ((mapCheck.getX() + 1) - origin.getX()) + origin.getY());
                        intersection.setX((float)(mapCheck.getX() + 1));
                        sampleX = intersection.getY() - (float)Math.floor(intersection.getY());
                    }
                }

                hit.setHitPos(intersection);
                hit.setSampleX(sampleX);
            }
        }
        hit.setLength(distanceWall);
        return hitWall;
    }

    /**
     * For floors and ceilings, we don't use the ray, instead we just
     * pseudo-project a plane, a la Mode 7.
     * First calculate depth into screen...
     */
    private float getPlaneZCeiling(int y, int height) {
        return (height / 2.0f) / ((height / 2.0f) - (float)y);
    }

    private float getPlaneZFloor(int y, int height) {
        return  (height / 2.0f) / ((float)y - (height / 2.0f));
    }

    /**
     * This method render the walls, ceiling and floor
     */
    private void renderWalls() {
        for (int x = 0; x < super.getW(); x++) {

            // La dirección del rayo se podría calcular cada vez que se
            // modifica el ángulo del jugador
            camera.calRayDirection(x, super.getW());

            TileHit hit = new TileHit();
            float distanceWall = camera.getDepth();
            if (castRayDDA(camera.getPos(), camera.getRayDirection(), hit)) {
                distanceWall = camera.calDistance(hit);
            }

            float ceiling = (super.getH() / 2.0f) - (super.getH() / distanceWall);
            float floor = super.getH() - ceiling;
            float wallHeight = floor - ceiling;

            // depth buffer
            depthBuffer[x] = distanceWall;
            
            for (int y = 0; y < super.getH(); y++) {
                if ( y <= (int)ceiling ) { // ceiling
                    float planeZ = getPlaneZCeiling(y, super.getH());
                    camera.calRayDirection(x, this.getW());
                    camera.calTilePosMode7(planeZ);

                    int planeTileX = (int)camera.getTilePosMode7().getX();
                    int planeTileY = (int)camera.getTilePosMode7().getY();

                    float planeSampleX = camera.getTilePosMode7().getX() - planeTileX;
                    float planeSampleY = camera.getTilePosMode7().getY() - planeTileY;

                    int sampleColor = selectSceneryPixel(
                            planeTileX,
                            planeTileY,
                            CellSide.TOP,
                            planeSampleX,
                            planeSampleY,
                            planeZ);
                    super.setPixel(x, y, sampleColor);
                } else if (y > (int)ceiling && y <= (int)floor ) { // wall
                    float sampleY = ( (float)y - ceiling ) / wallHeight;

                    int sampleColor = selectSceneryPixel(
                            hit.getTilePos().getX(),
                            hit.getTilePos().getY(),
                            hit.getSide(),
                            hit.getSampleX(),
                            sampleY,
                            distanceWall);
                    super.setPixel(x, y, sampleColor);
                } else { // floor
                    float planeZ = getPlaneZFloor(y, super.getH());
                    camera.calRayDirection(x, this.getW());
                    camera.calTilePosMode7(planeZ);

                    int planeTileX = (int)camera.getTilePosMode7().getX();
                    int planeTileY = (int)camera.getTilePosMode7().getY();

                    float planeSampleX = Math.abs(camera.getTilePosMode7().getX() - planeTileX);
                    float planeSampleY = Math.abs(camera.getTilePosMode7().getY() - planeTileY);

                    int sampleColor = selectSceneryPixel(
                            planeTileX,
                            planeTileY,
                            CellSide.BOTTOM,
                            planeSampleX,
                            planeSampleY,
                            planeZ);
                    super.setPixel(x, y, sampleColor);
                }
            }
        }
    }

    /**
     * This method renders all objects on screen
     * @param objects the objects of the world to render
     */
    private void renderObjects(HashMap<Integer, ObjectRayCastWorld> objects) {
        for (Map.Entry<Integer, ObjectRayCastWorld> e : objects.entrySet()) {
            renderObject(e.getValue(), camera);
        }
    }

    /**
     * This method render the object passed by parameter
     * @param o the object of the world to render
     * @param camera the camera
     */
    private void renderObject(ObjectRayCastWorld o, Camera camera) {
        // Test if the object can be seen by the user
        float vecX = o.getPos().getX() - camera.getPos().getX();
        float vecY = o.getPos().getY() - camera.getPos().getY();
        float distanceToPlayer = (float)Math.sqrt(vecX * vecX + vecY * vecY);

        // Test if the object is in the field of view of the player
        float eyeX = (float)Math.sin(camera.getHeading());
        float eyeY = (float)Math.cos(camera.getHeading());

        // Difference between to angles
        float objectAngle = (float)(Math.atan2(eyeY, eyeX) - Math.atan2(vecY, vecX));
        if ( objectAngle < -3.14159f ) {
            objectAngle += 2.0f * 3.14159f;
        }
        if ( objectAngle > 3.14159f ) {
            objectAngle -= 2.0f * 3.14159f;
        }
        boolean isInPlayerFOV = Math.abs(objectAngle) < (camera.getFieldOfVision() / 2.0f);

        if ( isInPlayerFOV && distanceToPlayer < camera.getDepth() && distanceToPlayer >= camera.getMinDistToObject() ) {
            float objectCeiling = (float)(super.getH() / 2.0) - super.getH() / distanceToPlayer;
            float objectFloor = super.getH() - objectCeiling;
            float objectHeight = objectFloor - objectCeiling;
            float objectAspectRatio = getObjectHeight(o.getId()) / getObjectWidth(o.getId());
            float objectWidth = objectHeight / objectAspectRatio;

            float middleOfObject = (0.5f * (objectAngle / (camera.getFieldOfVision() / 2.0f)) + 0.5f) * super.getW();

            // Draw the object
            for ( float y = 0; y < objectHeight; y++ ) {
                for ( float x = 0; x < objectWidth; x++ ) {
                    // Create normalised sample coordinate
                    float sampleX = x / objectWidth;
                    float sampleY = y / objectHeight;

                    // Get pixel from a suitable texture
                    float niceAngle = camera.getHeading() - o.getHeading() + 3.14159f / 4.0f;
                    if ( niceAngle < 0 ) {
                        niceAngle += 2.0f * 3.14159f;
                    }
                    if ( niceAngle > 2.0f * 3.14159f ) {
                        niceAngle -= 2.0f * 3.14159f;
                    }
                    int color = selectObjectPixel(o.getId(), sampleX, sampleY, distanceToPlayer, niceAngle);

                    /*
                     * Check if the location is actually on screen (to not go OOB on depth buffer)
                     * and if the pixel is indeed visible (has no transparency component)
                     */
                    int objectColumn = (int) (middleOfObject + x - (objectWidth / 2.0f));

                    if (objectColumn >= 0 && objectColumn < super.getW() && y >= 0 && y < super.getH()) {
                        if ( depthBuffer[objectColumn] >= distanceToPlayer ) { // depthBuffer[a.getX()] >= distanceToPlayer
                            super.setPixel(objectColumn, (int) (objectCeiling + y), color);
                        }
                    }
                }
            }
        }
    }

    /**
     * This method renders the walls and the objects
     * of the world
     */
    public void render(HashMap<Integer, ObjectRayCastWorld> objects) {
        renderWalls();
        //renderObjects(objects);
    }
    
    /* Abstract methods */

    public abstract boolean isLocationSolid(float x, float y);

    public abstract int selectSceneryPixel(
            int planeTileX,
            int planeTileY,
            CellSide side,
            float planeSampleX,
            float planeSampleY,
            float planeSampleZ);

    public abstract int selectObjectPixel(
            int id,
            float sampleX,
            float sampleY,
            float distanceToObject,
            float niceAngle
    );

    public abstract float getObjectWidth(int id);

    public abstract float getObjectHeight(int id);

    /*
    * Getters and Setters
    */

    public ObjectRayCastWorld getCamera() {
        return camera;
    }

}
