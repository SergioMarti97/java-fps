package rayCastWorld;

import game.AbstractGame;
import game.GameContainer;
import game.gfx.Image;
import javafx.scene.input.KeyCode;
import points2d.Vec2df;
import points2d.Vec2di;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SecondIterationFirstPersonShooter implements AbstractGame {

    private String map = "";

    private Vec2di mapSize;

    private HashMap<Integer, Object> objects;

    private final float fieldOfView = (float) (Math.PI / 3.0f);

    private final float depth = 20.0f;

    private Vec2df playerPos;

    private float playerAngle;

    private Image imgWall;

    private Image imgMario;

    private float[] depthBuffer;

    @Override
    public void initialize(GameContainer gc) {
        depthBuffer = new float[gc.getRenderer().getW()];
        Arrays.fill(depthBuffer, 0.0f);

        map =
                "################################################################" +
                        "#.........#....................##..............................#" +
                        "#.........#....................................................#" +
                        "#.........#....................................................#" +
                        "#.........#....................................................#" +
                        "#.........#############........................................#" +
                        "#...............#..............................................#" +
                        "#...............#..............................................#" +
                        "#...............#..............................................#" +
                        "#.....#..#..#...#..............................................#" +
                        "#...............#..............................................#" +
                        "#...............#..............................................#" +
                        "#.....#..#..#...........................................####...#" +
                        "#..........................................................#...#" +
                        "#..........................................................#...#" +
                        "#.......................................................#..#...#" +
                        "#.......................................................#..#...#" +
                        "#.....................######..#......................####..#...#" +
                        "#.....................#.......#......................#.....#...#" +
                        "#....................##.###.###.........................#..#...#" +
                        "#....................##.....#........................#..#..#...#" +
                        "#....................##.#####.....................####..#..#...#" +
                        "#....................#.#.........................#......#..#...#" +
                        "#....................#..#.......................##..#####..#...#" +
                        "#..............................................##..##......#...#" +
                        "#.................................................##...........#" +
                        "#..............................................................#" +
                        "#..............................................................#" +
                        "#..............................##..............................#" +
                        "#..............................##..............................#" +
                        "#..............................##..............................#" +
                        "################################################################";

        mapSize = new Vec2di(64, 32);

        playerPos = new Vec2df(mapSize.getX() / 2.0f, mapSize.getY() / 2.0f);
        playerAngle = 90.0f;

        imgWall = new Image("/block.png");
        imgMario = new Image("/mario.png");
        objects = new HashMap<>();
        objects.put(0, new Object(0, new Vec2df(mapSize.getX() / 2.0f, mapSize.getY() / 2.0f + 3.0f)));
    }

    @Override
    public void update(GameContainer gc, float elapsedTime) {

        final float vel = 5.25f;
        final float rotVel = 1.5f;

        if ( gc.getInput().isKeyHeld(KeyCode.D) ) {
            playerAngle += rotVel * elapsedTime;
        }

        if ( gc.getInput().isKeyHeld(KeyCode.A) ) {
            playerAngle -= rotVel * elapsedTime;
        }

        if ( gc.getInput().isKeyHeld(KeyCode.W) ) {
            playerPos.addToX((float)Math.sin(playerAngle) * vel * elapsedTime);
            playerPos.addToY((float)Math.cos(playerAngle) * vel * elapsedTime);
        }

        if ( gc.getInput().isKeyHeld(KeyCode.S) ) {
            playerPos.addToX(-(float)Math.sin(playerAngle) * vel * elapsedTime);
            playerPos.addToY(-(float)Math.cos(playerAngle) * vel * elapsedTime);
        }

        if ( gc.getInput().isKeyHeld(KeyCode.Q) ) {
            playerPos.addToX((float)Math.sin(playerAngle) * vel * elapsedTime);
            playerPos.addToY(-(float)Math.cos(playerAngle) * vel * elapsedTime);
        }

        if ( gc.getInput().isKeyHeld(KeyCode.E) ) {
            playerPos.addToX(-(float)Math.sin(playerAngle) * vel * elapsedTime);
            playerPos.addToY((float)Math.cos(playerAngle) * vel * elapsedTime);
        }
    }

    @Override
    public void render(GameContainer gc) {
        renderWalls(gc);
        renderObjects(gc);
    }

    /**
     * DDA Algorithm
     */
    private boolean castRayDDA(Vec2df origin, Vec2df direction, TileHit hit) {
        // Se calcula el incremento de X y de Y
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

            if ( mapCheck.getX() < 0 || mapCheck.getX() >= mapSize.getX() ||
                    mapCheck.getY() < 0 || mapCheck.getY() >= mapSize.getY() ) {
                hitWall = true;
                distanceWall = depth;
            } else {
                if ( map.toCharArray()[mapCheck.getY() * mapSize.getX() + mapCheck.getX()] == '#' ) {
                    hitWall = true;

                    Vec2df intersection = new Vec2df();

                    float m = direction.getY() / direction.getX();

                    if ( origin.getY() <= mapCheck.getY() ) {
                        if ( origin.getX() <= mapCheck.getX() ) { // West
                            hit.setSide(Engine.CellSide.WEST);
                            intersection.setY(m * (mapCheck.getX() - origin.getX()) + origin.getY());
                            intersection.setX((float)(mapCheck.getX()));
                            sampleX = intersection.getY() - (float)Math.floor(intersection.getY());
                        } else if ( origin.getX() >= (mapCheck.getX() + 1) ) { // East
                            hit.setSide(Engine.CellSide.EAST);
                            intersection.setY(m * ((mapCheck.getX() + 1) - origin.getX()) + origin.getY());
                            intersection.setX((float)(mapCheck.getX() + 1));
                            sampleX = intersection.getY() - (float)Math.floor(intersection.getY());
                        } else { // Norte
                            hit.setSide(Engine.CellSide.NORTH);
                            intersection.setY((float)mapCheck.getY());
                            intersection.setX((mapCheck.getY() - origin.getY()) / m + origin.getX());
                            sampleX = intersection.getX() - (float)Math.floor(intersection.getX());
                        }

                        if ( intersection.getY() < mapCheck.getY() ) { // North? or South?
                            hit.setSide(Engine.CellSide.SOUTH);
                            intersection.setY((float)mapCheck.getY());
                            intersection.setX((mapCheck.getY() - origin.getY()) / m + origin.getX());
                            sampleX = intersection.getX() - (float)Math.floor(intersection.getX());
                        }
                    } else if ( origin.getY() >= mapCheck.getY() + 1 ) {
                        if ( origin.getX() <= mapCheck.getX() ) { // West
                            hit.setSide(Engine.CellSide.WEST);
                            intersection.setY(m * (mapCheck.getX() - origin.getX()) + origin.getY());
                            intersection.setX((float)mapCheck.getX());
                            sampleX = intersection.getY() - (float)Math.floor(intersection.getY());
                        } else if ( origin.getX() >= (mapCheck.getX() + 1) ) { // East
                            hit.setSide(Engine.CellSide.EAST);
                            intersection.setY(m * ((mapCheck.getX() + 1) - origin.getX()) + origin.getY());
                            intersection.setX((float)(mapCheck.getX() + 1));
                            sampleX = intersection.getY() - (float)Math.floor(intersection.getY());
                        } else { // South
                            hit.setSide(Engine.CellSide.SOUTH);
                            intersection.setY((float)(mapCheck.getY() + 1));
                            intersection.setX(((mapCheck.getY() + 1) - origin.getY()) / m + origin.getX());
                            sampleX = intersection.getX() - (float)Math.floor(intersection.getX());
                        }

                        if ( intersection.getY() > (mapCheck.getY() + 1) ) { // South? or North?
                            hit.setSide(Engine.CellSide.NORTH);
                            intersection.setY((float)(mapCheck.getY() + 1));
                            intersection.setX(((mapCheck.getY() + 1)- origin.getY()) / m + origin.getX());
                            sampleX = intersection.getX() - (float)Math.floor(intersection.getX());
                        }
                    } else {
                        if ( origin.getX() <= mapCheck.getX() ) { // West
                            hit.setSide(Engine.CellSide.WEST);
                            intersection.setY(m * (mapCheck.getX() - origin.getX()) + origin.getY());
                            intersection.setX((float)(mapCheck.getX()));
                            sampleX = intersection.getY() - (float)Math.floor(intersection.getY());
                        } else if ( origin.getX() >= (mapCheck.getX() + 1) ) { // East
                            hit.setSide(Engine.CellSide.EAST);
                            intersection.setY(m * ((mapCheck.getX() + 1) - origin.getX()) + origin.getY());
                            intersection.setX((float)(mapCheck.getX() + 1));
                            sampleX = intersection.getY() - (float)Math.floor(intersection.getY());
                        }
                    }

                    hit.setHitPos(intersection);
                    hit.setSampleX(sampleX);
                }
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
    private Vec2df getSampleMode7(Vec2df rayDirection, float rayAngle, float planeZ) {
        Vec2df planePoint = new Vec2df(
                playerPos.getX() + rayDirection.getX() * planeZ * 2.0f / (float)Math.cos(rayAngle - playerAngle),
                playerPos.getY() + rayDirection.getY() * planeZ * 2.0f / (float)Math.cos(rayAngle - playerAngle)
        );

        int planeTileX = (int)planePoint.getX();
        int planeTileY = (int)planePoint.getY();

        return new Vec2df(Math.abs(planePoint.getX() - planeTileX), Math.abs(planePoint.getY() - planeTileY));
    }

    private Vec2df getSampleCeiling(int y, int height, Vec2df rayDirection, float rayAngle) {
        return getSampleMode7(rayDirection, rayAngle, getPlaneZCeiling(y, height));
    }

    private Vec2df getSampleFloor(int y, int height, Vec2df rayDirection, float rayAngle) {
        return getSampleMode7(rayDirection, rayAngle, getPlaneZFloor(y, height));
    }

    /**
     * This method render the walls, ceiling and floor
     */
    private void renderWalls(GameContainer gc) {
        // For each column on screen
        for ( int x = 0; x < gc.getRenderer().getW(); x++ ) {

            // La dirección del rayo se podría calcular cada vez que se
            // modifica el ángulo del jugador
            float rayAngle = (playerAngle - fieldOfView / 2.0f) + ((float)x / (float)gc.getRenderer().getW()) * fieldOfView;

            Vec2df rayDirection = new Vec2df((float)Math.sin(rayAngle), (float)Math.cos(rayAngle));

            TileHit hit = new TileHit();
            float distanceWall = depth;
            if ( castRayDDA(playerPos, rayDirection, hit) ) {
                Vec2df ray = new Vec2df(
                        hit.getHitPos().getX() - playerPos.getX(),
                        hit.getHitPos().getY() - playerPos.getY()
                );
                distanceWall = ray.mag() * (float)Math.cos(rayAngle - playerAngle);
            }

            float ceiling = (gc.getRenderer().getH() / 2.0f) - (gc.getRenderer().getH() / distanceWall);
            float floor = gc.getRenderer().getH() - ceiling;
            float wallHeight = floor - ceiling;

            // depth buffer
            depthBuffer[x] = distanceWall;

            // Shading of the wall
            float value = 1 - Math.min(distanceWall / depth, 1);

            for ( int y = 0; y < gc.getRenderer().getH(); y++ ) {
                if ( y <= (int)ceiling ) { // ceiling
                    Vec2df sample = getSampleCeiling(y, gc.getRenderer().getH(), rayDirection, rayAngle);
                    int sampleColor = imgWall.getSample(sample.getX(), sample.getY());

                    int r = sampleColor >> 16 & 0xff;
                    int g = sampleColor >> 8 & 0xff;
                    int b = sampleColor & 0xff;

                    // Shading of the ceiling
                    float shadow = Math.min(1 - (2 * y / (float)gc.getRenderer().getH()), 1);

                    int shadedColor = (0xff << 24 | (int) (r * shadow) << 16 | (int) (g * shadow) << 8 | (int) (b * shadow));

                    gc.getRenderer().setPixel(x, y, shadedColor);
                } else if (y > (int)ceiling && y <= (int)floor ) { // wall
                    float sampleY = ( (float)y - ceiling ) / wallHeight;
                    int sampleColor = imgWall.getSample(hit.getSampleX(), sampleY);

                    int r = sampleColor >> 16 & 0xff;
                    int g = sampleColor >> 8 & 0xff;
                    int b = sampleColor & 0xff;

                    int shadedColor = (0xff << 24 | (int) (r * value) << 16 | (int) (g * value) << 8 | (int) (b * value));
                    gc.getRenderer().setPixel(x, y, shadedColor);
                } else { // floor
                    Vec2df sample = getSampleFloor(y, gc.getRenderer().getH(), rayDirection, rayAngle);
                    int sampleColor = imgWall.getSample(sample.getX(), sample.getY());

                    int r = sampleColor >> 16 & 0xff;
                    int g = sampleColor >> 8 & 0xff;
                    int b = sampleColor & 0xff;

                    // Shading of the floor
                    float shadow = Math.min((1 - (float)gc.getRenderer().getH() / (2 * y)), 1);

                    int shadedColor = (0xff << 24 | (int) (r * shadow) << 16 | (int) (g * shadow) << 8 | (int) (b * shadow));
                    gc.getRenderer().setPixel(x, y, shadedColor);
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
            float distanceToPlayer = (float)Math.sqrt(vecX * vecX + vecY * vecY);

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
            boolean isInPlayerFOV = Math.abs(objectAngle) < (fieldOfView / 2.0f);

            if ( isInPlayerFOV && distanceToPlayer < depth && distanceToPlayer >= 0.5f ) {
                float objectCeiling = (float)(gc.getRenderer().getH() / 2.0) - gc.getRenderer().getH() / distanceToPlayer;
                float objectFloor = gc.getRenderer().getH() - objectCeiling;
                float objectHeight = objectFloor - objectCeiling;
                float objectAspectRatio = (float)imgMario.getH() / (float)imgMario.getW();
                float objectWidth = objectHeight / objectAspectRatio;

                float middleOfObject = (0.5f * (objectAngle / (fieldOfView / 2.0f)) + 0.5f) * gc.getRenderer().getW();

                // Draw the object
                for ( float x = 0; x < objectWidth; x++ ) {
                    for ( float y = 0; y < objectHeight; y++ ) {
                        float sampleX = x / objectWidth;
                        float sampleY = y / objectHeight;
                        int color = imgMario.getSample(sampleX, sampleY);
                        if ( (color >> 24) != 0x00 ) {
                            int objectColumn = (int) (middleOfObject + x - (objectWidth / 2.0f));

                            if (objectColumn >= 0 && objectColumn < gc.getRenderer().getW() && y >= 0 && y < gc.getRenderer().getH()) {
                                if ( depthBuffer[objectColumn] >= distanceToPlayer ) {

                                    float value = 1 - Math.min(distanceToPlayer / depth, 1);
                                    int r = color >> 16 & 0xff;
                                    int g = color >> 8 & 0xff;
                                    int b = color & 0xff;

                                    int shadedColor = (0xff << 24 | (int) (r * value) << 16 | (int) (g * value) << 8 | (int) (b * value));

                                    gc.getRenderer().setPixel(objectColumn, (int) (objectCeiling + y), shadedColor);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
