package rayCastWorld.renderer;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;
import olcPGEApproach.gfx.Renderer;
import olcPGEApproach.vectors.points2d.Vec2df;
import olcPGEApproach.vectors.points2d.Vec2di;
import rayCastWorld.CellSide;
import rayCastWorld.objects.MovingObj;
import rayCastWorld.objects.Obj;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * The ray cast world renderer
 */
public abstract class RayCastingWorldRender extends Renderer {

    /**
     * There are two options to render the walls,
     * the naive method and the multiple threads method
     */
    public enum RenderMethod {
        NAIVE,
        POOL_THREAD
    }

    /**
     * The method to render the walls
     */
    protected RenderMethod renderMethod = RenderMethod.POOL_THREAD;

    /**
     * The depth buffer
     */
    protected float[] depthBuffer;

    /**
     * A buffer to say if a column of the screen has been calculated
     */
    protected boolean[] doneBuffer;

    /**
     * The camera to render the world
     */
    protected Camera camera;

    /**
     * Needed service to work with threads. It isn't essential
     * but in the original example what I follow is used
     */
    private ScheduledService<Boolean> scheduledService;

    /**
     * The pool of threads
     */
    private ThreadPoolExecutor executor;

    protected int screenSection;

    private long accTimeRenderWalls = 0L;

    private int countRenderWalls = 0;

    private long accTimeCastingRay = 0L;

    private int countCastingRay = 0;

    /**
     * Constructor
     * @param p the array of pixels of the image
     * @param w the width of the screen
     * @param h the height of the screen
     */
    public RayCastingWorldRender(int[] p, int w, int h) {
        super(p, w, h);
        depthBuffer = new float[getW()];
        Arrays.fill(depthBuffer, 0.0f);
        doneBuffer = new boolean[getW()];
        Arrays.fill(doneBuffer, false);
        camera = new Camera();
        screenSection = getW();
    }

    /**
     * @param r the renderer
     */
    public RayCastingWorldRender(Renderer r) {
        this(r.getP(), r.getW(), r.getH());
    }

    /**
     * Needed method for the service
     */
    public void setScheduleService() {
        if ( scheduledService == null ) {
            scheduledService = new ScheduledService<Boolean>() {
                @Override
                protected Task<Boolean> createTask() {
                    return new Task<Boolean>() {
                        @Override
                        protected Boolean call() {
                            return executor.isTerminated();
                        }
                    };
                }
            };

            scheduledService.setDelay(Duration.millis(500));
            scheduledService.setPeriod(Duration.seconds(1));
            scheduledService.setOnSucceeded(e -> {
                if (scheduledService.getValue()) {
                    scheduledService.cancel();
                }
            });
        }
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
     *
     * There are two parameters: the start x and the end x
     * this is for subdivide the task of render walls
     * in different sections of screen, each one for one
     * thread
     *
     * @param starX the star x pixel to render the walls
     * @param endX the end x pixel to render the walls
     */
    private synchronized void renderWallsSection(int starX, int endX) {
        //long t1 = System.nanoTime();
        for (int x = starX; x < endX; x++) {
            if (doneBuffer[x] || depthBuffer[x] == 0.0f) {
                camera.calRayDirection(x, getW());

                TileHit hit;
                float distanceWall = camera.getDepth();
                if (castRayDDA(camera.getPos(), camera.getRayDirection(), hit = new TileHit())) {
                    distanceWall = camera.calDistance(hit);
                }

                int ceiling = (int) ((getH() / 2.0f) - (getH() / distanceWall));
                int floor = getH() - ceiling;

                depthBuffer[x] = distanceWall;

                for (int y = 0; y < getH(); y++) {
                    if (y <= ceiling) {
                        renderCeiling(x, y);
                    } else if (y <= floor) {
                        renderWall(x, y, hit, ceiling, floor, distanceWall);
                    } else {
                        renderFloor(x, y);
                    }
                }
                doneBuffer[x] = true;
            }
        }
        /*long t2 = System.nanoTime();
        accTimeCastingRay += (t2 - t1);
        countCastingRay++;
        if (accTimeCastingRay > 1000000000L) {
            System.out.printf("(1) Avg time taken to cast a ray: %f\n", accTimeCastingRay / (1000000000.0f * countCastingRay));
            accTimeCastingRay -= 1000000000L;
            countCastingRay = 0;
        }*/
    }

    private void renderCeiling(int x, int y) {
        float planeZ = getPlaneZCeiling(y, getH());
        camera.calTilePosMode7(planeZ);

        int floorPlaneTileX = (int)camera.getTilePosMode7().getX();
        int floorPlaneTileY = (int)camera.getTilePosMode7().getY();

        float planeSampleX = camera.getTilePosMode7().getX() - floorPlaneTileX;
        float planeSampleY = camera.getTilePosMode7().getY() - floorPlaneTileY;

        int sampleColor = selectSceneryPixel(
                floorPlaneTileX,
                floorPlaneTileY,
                CellSide.TOP,
                planeSampleX,
                planeSampleY,
                planeZ);
        setPixel(x, y, sampleColor);
    }

    private void renderWall(int x, int y, TileHit hit, float ceiling, float floor, float distanceWall) {
        float wallHeight = floor - ceiling;
        float sampleY = ( (float)y - ceiling ) / wallHeight;
        int sampleColor = selectSceneryPixel(
                hit.getTilePos().getX(),
                hit.getTilePos().getY(),
                hit.getSide(),
                hit.getSampleX(),
                sampleY,
                distanceWall);
        setPixel(x, y, sampleColor);
    }

    private void renderFloor(int x, int y) {
        float planeZ = getPlaneZFloor(y, getH());
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
        setPixel(x, y, sampleColor);
    }

    /**
     * This method renders the walls, with multiple threads
     */
    private void renderWallThreads(int startX, int endX) {
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        int numThreads = executor.getMaximumPoolSize() -1;
        int sectionTotalWidth = endX - startX;
        int subsectionWidth = sectionTotalWidth / numThreads;
        for (int i = 0; i < numThreads; i++) {
            int sX = subsectionWidth * i + startX;
            int eX = subsectionWidth * (i + 1) + startX;
            executor.execute(()-> renderWallsSection(sX, eX));
        }
        executor.shutdown();
        scheduledService.restart();
    }

    /**
     * This method renders the walls. There are
     * two methods: naive and multithreading
     * The naive renders one section of screen, from 0 to the screen width (full screen)
     * The multithreading leans on the naive method, each thread renders a section of the screen
     */
    public void renderWalls() {
        int sX = 0;
        int eX = getW();
        if (screenSection != getW()) {
            int halfSectionWidth = screenSection / 2;
            sX = (getW() / 2) - halfSectionWidth;
            eX = (getW() / 2) + halfSectionWidth;
        }
        switch (renderMethod) {
            case NAIVE:
                renderWallsSection(sX, eX);
                break;
            case POOL_THREAD:
                setScheduleService();
                renderWallThreads(sX, eX);
                break;
        }
    }

    /**
     * This method renders all objects on screen
     * @param objects the objects of the world to render
     */
    private void renderObjects(HashMap<Integer, Obj> objects) {
        for (Map.Entry<Integer, Obj> e : objects.entrySet()) {
            renderObject(e.getValue(), camera);
        }
    }

    /**
     * This method render the object passed by parameter
     * @param o the object of the world to render
     * @param camera the camera
     */
    private void renderObject(Obj o, Camera camera) {
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
            float objectCeiling = (float)(getH() / 2.0) - getH() / distanceToPlayer;
            float objectFloor = getH() - objectCeiling;
            float objectHeight = objectFloor - objectCeiling;
            float objectAspectRatio = getObjectHeight(o.getId()) / getObjectWidth(o.getId());
            float objectWidth = objectHeight / objectAspectRatio;

            float middleOfObject = (0.5f * (objectAngle / (camera.getFieldOfVision() / 2.0f)) + 0.5f) * getW();

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

                    if (objectColumn >= 0 && objectColumn < getW() && y >= 0 && y < getH()) {
                        if ( depthBuffer[objectColumn] >= distanceToPlayer ) {
                            depthBuffer[objectColumn] = distanceToPlayer;
                            setPixel(objectColumn, (int) (objectCeiling + y), color);
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
    public void render(HashMap<Integer, Obj> objects) {
        long t1 = System.nanoTime();
        renderWalls();
        long t2 = System.nanoTime();
        accTimeRenderWalls += (t2 - t1);
        countRenderWalls++;
        if (accTimeRenderWalls > 1000000000L) {
            System.out.printf("(2) Time taken to render the walls: %f\n", accTimeRenderWalls / (countRenderWalls * 1000000000.0f));
            accTimeRenderWalls -= 1000000000L;
            countRenderWalls = 0;
        }
        renderObjects(objects);
        Arrays.fill(depthBuffer, 0.0f);
        Arrays.fill(doneBuffer, false);
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

    public MovingObj getCamera() {
        return camera;
    }

}
