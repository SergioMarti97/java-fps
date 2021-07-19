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

    private final float fieldOfView = (float) (Math.PI / 4.0f);

    private final float depth = 16.0f;

    private final Vec2df playerPos = new Vec2df(8, 6);

    private float playerAngle = 0.0f;

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
                        "#.....#..#..#..................................................#" +
                        "#..............................................................#" +
                        "#..............................................................#" +
                        "#..............................................................#" +
                        "#..............................................................#" +
                        "#.....................######..#................................#" +
                        "#.....................#.......#................................#" +
                        "#....................##.###.###.........................#......#" +
                        "#....................##.....#........................##........#" +
                        "#....................##.#####........................##.#......#" +
                        "#....................#.#.......................................#" +
                        "#....................#..#...............................#......#" +
                        "#..............................................................#" +
                        "#..............................................................#" +
                        "#..............................................................#" +
                        "#..............................................................#" +
                        "#..............................##..............................#" +
                        "#..............................##..............................#" +
                        "#..............................##..............................#" +
                        "################################################################";

        mapSize = new Vec2di(64, 32);

        imgWall = new Image("/block.png");
        imgMario = new Image("/mario.png");
        objects = new HashMap<>();
        objects.put(0, new Object(0, new Vec2df(6.5f, 9.5f)));
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

    private void renderWalls(GameContainer gc) {
        // For each column on screen
        for ( int x = 0; x < gc.getRenderer().getW(); x++ ) {

            // La dirección del rayo se podría calcular cada vez que se
            // modifica el ángulo del jugador
            float rayAngle = (playerAngle - fieldOfView / 2.0f) + ((float)x / (float)gc.getRenderer().getW()) * fieldOfView;

            // Algoritmo DDA
            Vec2df rayDirection = new Vec2df((float)Math.sin(rayAngle), (float)Math.cos(rayAngle));

            // Se calcula el incremento de X y de Y
            float yDivideByX = rayDirection.getY() / rayDirection.getX();
            float xDivideByY = rayDirection.getX() / rayDirection.getY();
            Vec2df rayDelta = new Vec2df(
                    (float)Math.sqrt(1 + (yDivideByX * yDivideByX)),
                    (float)Math.sqrt(1 + (xDivideByY * xDivideByY))
            );

            Vec2di mapCheck = new Vec2di((int)playerPos.getX(), (int)playerPos.getY());

            Vec2df rayLength1D = new Vec2df();
            Vec2di stepDistance = new Vec2di();

            if ( rayDirection.getX() < 0 ) {
                stepDistance.setX(-1);
                rayLength1D.setX((playerPos.getX() - (float)mapCheck.getX()) * rayDelta.getX());
            } else {
                stepDistance.setX(1);
                rayLength1D.setX(((float)(mapCheck.getX() + 1) - playerPos.getX()) * rayDelta.getX());
            }

            if ( rayDirection.getY() < 0 ) {
                stepDistance.setY(-1);
                rayLength1D.setY((playerPos.getY() - (float)mapCheck.getY()) * rayDelta.getY());
            } else {
                stepDistance.setY(1);
                rayLength1D.setY(((float)(mapCheck.getY() + 1) - playerPos.getY()) * rayDelta.getY());
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

                        float blockMidX = (float)mapCheck.getX() + 0.5f;
                        float blockMidY = (float)mapCheck.getY() + 0.5f;

                        float testPointX = playerPos.getX() + rayDirection.getX() * distanceWall;
                        float testPointY = playerPos.getY() + rayDirection.getY() * distanceWall;

                        float testAngle = (float)Math.atan2((testPointY - blockMidY), (testPointX - blockMidX));

                        if ( testAngle >= -3.14159f * 0.25f && testAngle < 3.14159f * 0.25f ) {
                            sampleX = testPointY - (float)mapCheck.getY();
                        }
                        if ( testAngle >= 3.14159f * 0.25f && testAngle < 3.14159f * 0.75f ) {
                            sampleX = testPointX - (float)mapCheck.getX();
                        }
                        if ( testAngle < -3.14159f * 0.25f && testAngle >= -3.14159f * 0.75f ) {
                            sampleX = testPointX - (float)mapCheck.getX();
                        }
                        if ( testAngle >= 3.14159f * 0.75f || testAngle < -3.14159f * 0.75f ) {
                            sampleX = testPointY - (float)mapCheck.getY();
                        }
                    }
                }
            }

            int ceiling = (int) ((float)(gc.getRenderer().getH() / 2.0) - gc.getRenderer().getH() / distanceWall);
            int floor = gc.getRenderer().getH() - ceiling;

            // depth buffer
            depthBuffer[x] = distanceWall;

            // Shading of the wall
            float value = 1 - Math.min(distanceWall / depth, 1);

            for ( int y = 0; y < gc.getRenderer().getH(); y++ ) {

                // Shading of the floor and ceiling
                float value2 = 2 * y / (float)gc.getRenderer().getH();

                if ( y < ceiling ) { // ceiling
                    gc.getRenderer().setPixel(x, y, (0xff << 24 | (int)(255 * (1 - value2)) << 8 | (int)(255 * (1 - value2))));
                } else if ( y > ceiling && y <= floor ) { // wall

                    float sampleY = ( (float)y - (float)ceiling ) / ( (float)floor - (float)ceiling );
                    int sampleColor = imgWall.getSample(sampleX, sampleY);

                    int r = sampleColor >> 16 & 0xff;
                    int g = sampleColor >> 8 & 0xff;
                    int b = sampleColor & 0xff;

                    int newR = (int) (r * value);
                    int newG = (int) (g * value);
                    int newB = (int) (b * value);

                    int shadedColor = (0xff << 24 | newR << 16 | newG << 8 | newB);
                    gc.getRenderer().setPixel(x, y, shadedColor); //(0xff << 24 | (int) (255 * (1 - value)) << 16);
                } else { // floor
                    gc.getRenderer().setPixel(x, y, (0xff << 24 | (int)(255 * value2) << 8));
                }
            }
        }
    }

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
            boolean isInPlayerFOV = Math.abs(objectAngle) < fieldOfView / 2.0f;

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
                                    gc.getRenderer().setPixel(objectColumn, (int) (objectCeiling + y), color);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
