package rayCastWorld;

import game.AbstractGame;
import game.GameContainer;
import points2d.Vec2df;
import points2d.Vec2di;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * The ray cast world engine
 */
public abstract class RayCastingWorldGame implements AbstractGame {

    protected HashMap<Integer, Object> objects;

    protected float depth = 32.0f;

    protected float[] depthBuffer;

    protected Vec2df playerPos;

    protected float playerAngle;

    protected final float FOV = (float) (Math.PI / 3.0f);

    protected final float MIN_DISTANCE_OBJECT = 0.5f;

    @Override
    public void initialize(GameContainer gc) {
        depthBuffer = new float[gc.getRenderer().getW()];
        Arrays.fill(depthBuffer, 0.0f);
        objects = new HashMap<>();

        playerPos = new Vec2df();
        playerAngle = 0.0f;
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
        while ( !hitWall && distanceWall < depth ) {
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
     * ... then project polar coordinate (r, theta) from camera into screen (x, y), again
     * compensating with cosine to remove fisheye
     */
    private Vec2df getTilePosMode7(Vec2df rayDirection, float rayAngle, float planeZ) {
        return new Vec2df(
                playerPos.getX() + rayDirection.getX() * planeZ * 2.0f / (float)Math.cos(rayAngle - playerAngle),
                playerPos.getY() + rayDirection.getY() * planeZ * 2.0f / (float)Math.cos(rayAngle - playerAngle)
        );
    }

    /**
     * This method render the walls, ceiling and floor
     */
    private void renderWalls(GameContainer gc) {
        for (int x = 0; x < gc.getRenderer().getW(); x++) {

            // La dirección del rayo se podría calcular cada vez que se
            // modifica el ángulo del jugador
            float rayAngle = (playerAngle - FOV / 2.0f) + ((float) x / (float) gc.getRenderer().getW()) * FOV;

            Vec2df rayDirection = new Vec2df((float) Math.sin(rayAngle), (float) Math.cos(rayAngle));

            TileHit hit = new TileHit();
            float distanceWall = depth;
            if (castRayDDA(playerPos, rayDirection, hit)) {
                Vec2df ray = new Vec2df(
                        hit.getHitPos().getX() - playerPos.getX(),
                        hit.getHitPos().getY() - playerPos.getY()
                );
                distanceWall = ray.mag() * (float) Math.cos(rayAngle - playerAngle);
            }

            float ceiling = (gc.getRenderer().getH() / 2.0f) - (gc.getRenderer().getH() / distanceWall);
            float floor = gc.getRenderer().getH() - ceiling;
            float wallHeight = floor - ceiling;

            // depth buffer
            depthBuffer[x] = distanceWall;
            
            for (int y = 0; y < gc.getRenderer().getH(); y++) {
                if ( y <= (int)ceiling ) { // ceiling
                    float planeZ = getPlaneZCeiling(y, gc.getRenderer().getH());
                    Vec2df planeTilePos = getTilePosMode7(rayDirection, rayAngle, planeZ);

                    int planeTileX = (int)planeTilePos.getX();
                    int planeTileY = (int)planeTilePos.getY();

                    float planeSampleX = planeTilePos.getX() - planeTileX;
                    float planeSampleY = planeTilePos.getY() - planeTileY;

                    int sampleColor = selectSceneryPixel(
                            planeTileX,
                            planeTileY,
                            CellSide.TOP,
                            planeSampleX,
                            planeSampleY,
                            planeZ);
                    gc.getRenderer().setPixel(x, y, sampleColor);
                } else if (y > (int)ceiling && y <= (int)floor ) { // wall
                    float sampleY = ( (float)y - ceiling ) / wallHeight;

                    int sampleColor = selectSceneryPixel(
                            hit.getTilePos().getX(),
                            hit.getTilePos().getY(),
                            hit.getSide(),
                            hit.getSampleX(),
                            sampleY,
                            distanceWall);
                    gc.getRenderer().setPixel(x, y, sampleColor);
                } else { // floor
                    float planeZ = getPlaneZFloor(y, gc.getRenderer().getH());
                    Vec2df planeTilePos = getTilePosMode7(rayDirection, rayAngle, planeZ);

                    int planeTileX = (int)planeTilePos.getX();
                    int planeTileY = (int)planeTilePos.getY();

                    float planeSampleX = Math.abs(planeTilePos.getX() - planeTileX);
                    float planeSampleY = Math.abs(planeTilePos.getY() - planeTileY);

                    int sampleColor = selectSceneryPixel(
                            planeTileX,
                            planeTileY,
                            CellSide.BOTTOM,
                            planeSampleX,
                            planeSampleY,
                            planeZ);
                    gc.getRenderer().setPixel(x, y, sampleColor);
                }
            }
        }
    }

    /**
     * This method renders all objects on screen
     */
    private void renderObjects(GameContainer gc) {
        for (Map.Entry<Integer, Object> e : objects.entrySet()) {
            Object object = e.getValue();

            // Test if the object can be seen by the user
            float vecX = object.getPos().getX() - playerPos.getX();
            float vecY = object.getPos().getY() - playerPos.getY();

            float distanceToPlayer = (vecX * vecX + vecY * vecY);

            // Test if the object is in the field of view of the player
            float eyeX = (float)Math.sin(playerAngle);
            float eyeY = (float)Math.cos(playerAngle);

            // Difference between to angles
            float objectAngle = (float)(Math.atan2(eyeY, eyeX) - Math.atan2(vecY, vecX));
            if ( objectAngle < -3.14159f ) {
                objectAngle += 2.0f * 3.14159f;
            }
            if ( objectAngle > 3.14159f ) {
                objectAngle -= 2.0f * 3.14159f;
            }
            boolean isInPlayerFOV = Math.abs(objectAngle) < (FOV / 2.0f);

            if ( isInPlayerFOV && distanceToPlayer < depth && distanceToPlayer >= MIN_DISTANCE_OBJECT * MIN_DISTANCE_OBJECT ) {
                float objectCeiling = (float)(gc.getRenderer().getH() / 2.0) - gc.getRenderer().getH() / distanceToPlayer;
                float objectFloor = gc.getRenderer().getH() - objectCeiling;
                float objectHeight = objectFloor - objectCeiling;
                float objectAspectRatio = getObjectHeight(object.getId()) / getObjectWidth(object.getId());
                float objectWidth = objectHeight / objectAspectRatio;

                float middleOfObject = (0.5f * (objectAngle / (FOV / 2.0f)) + 0.5f) * gc.getRenderer().getW();

                // Work out its position on the floor...
                /*Vec2df floorPoint = new Vec2df(
                        // Horizontal screen location is determined based on object angle relative to camera angle
                        (0.5f * ((objectAngle / (FOV * 0.5f))) + 0.5f) * (float)gc.getRenderer().getH(),
                        // Vertical screen location is projected distance
                        // Si cambias la suma por una resta, se queda como en el 2n nivel
                        ((float)gc.getRenderer().getH() / 2.0f) + ((float)gc.getRenderer().getH() / distanceToPlayer) / (float)Math.cos(objectAngle / 2.0f)
                );

                /*
                * First we need the objects size, which we can scale into world space (maintaining aspecto ratio)
                * and then project into screen space
                */
                /*Vec2df objectSize = new Vec2df(
                        getObjectWidth(object.getId()) * (2.0f * (float) gc.getRenderer().getH()) / distanceToPlayer,
                        getObjectHeight(object.getId()) * (2.0f * (float) gc.getRenderer().getH()) / distanceToPlayer
                );*/

                /*
                * Second we need the objects top left position in screen space, which is relative to the objects
                * size and assumes the middle of the object is the location in world space
                */
                /*Vec2df objectTopLeft = new Vec2df(
                        floorPoint.getX() - objectSize.getX() / 2.0f,
                        floorPoint.getY() - objectSize.getY()
                );*/

                // Draw the object
                for ( float y = 0; y < objectHeight; y++ ) { // float y = 0; y < objectSize.getY(); y++
                    for ( float x = 0; x < objectWidth; x++ ) { // float x = 0; x < objectSize.getX(); x++
                        // Create normalised sample coordinate
                        float sampleX = x / objectWidth; // objectSize.getX()
                        float sampleY = y / objectHeight; // objectSize.getY()

                        // Get pixel from a suitable texture
                        float niceAngle = playerAngle - object.getHeading() + 3.14159f / 4.0f;
                        if ( niceAngle < 0 ) {
                            niceAngle += 2.0f * 3.14159f;
                        }
                        if ( niceAngle > 2.0f * 3.14159f ) {
                            niceAngle -= 2.0f * 3.14159f;
                        }
                        int color = selectObjectPixel(object.getId(), sampleX, sampleY, distanceToPlayer, niceAngle);

                        // Calculate screen pixel location
                        //Vec2di a = new Vec2di((int)(objectTopLeft.getX() + x), (int)(objectTopLeft.getY() + y));

                        /*
                        * Check if the location is actually on screen (to not go OOB on depth buffer)
                        * and if the pixel is indeed visible (has no transparency component)
                        */
                        if ( (color >> 24) != 0x00 ) {
                            int objectColumn = (int) (middleOfObject + x - (objectWidth / 2.0f));

                            if (objectColumn >= 0 && objectColumn < gc.getRenderer().getW() && y >= 0 && y < gc.getRenderer().getH()) {
                            //if ( a.getX() >= 0 && a.getX() < gc.getRenderer().getW() && a.getY() >= 0 && a.getY() < gc.getRenderer().getH() ) {
                                if ( depthBuffer[objectColumn] >= distanceToPlayer ) { // depthBuffer[a.getX()] >= distanceToPlayer

                                    /*float value = 1 - Math.min(distanceToPlayer / depth, 1);
                                    int r = color >> 16 & 0xff;
                                    int g = color >> 8 & 0xff;
                                    int b = color & 0xff;

                                    int shadedColor = (0xff << 24 | (int) (r * value) << 16 | (int) (g * value) << 8 | (int) (b * value));*/
                                    //gc.getRenderer().setPixel(a.getX(), a.getY(), color);
                                    gc.getRenderer().setPixel(objectColumn, (int) (objectCeiling + y), color);
                                }
                            }
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
    @Override
    public void render(GameContainer gc) {
        renderWalls(gc);
        renderObjects(gc);
    }

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

    public HashMap<Integer, Object> getObjects() {
        return objects;
    }

    public Vec2df getPlayerPos() {
        return playerPos;
    }

    public void setPlayerPos(Vec2df playerPos) {
        this.playerPos = playerPos;
    }

    public float getPlayerAngle() {
        return playerAngle;
    }

    public void setPlayerAngle(float playerAngle) {
        this.playerAngle = playerAngle;
    }

    public float getDepth() {
        return depth;
    }

    public void setDepth(float depth) {
        this.depth = depth;
    }

}
