package rayCastWorld.first;

import game.AbstractGame;
import game.GameContainer;
import javafx.scene.input.KeyCode;
import javafx.util.Pair;
import points2d.Vec2df;
import points2d.Vec2di;

import java.util.ArrayList;

public class Iteration1FPS implements AbstractGame {

    private final Vec2df playerPos = new Vec2df(8, 6);

    private float playerAngle = 0.0f;

    private String map = "";

    private final Vec2di mapSize = new Vec2di(16, 12);

    @Override
    public void initialize(GameContainer gc) {
        map += "################";
        map += "#..........#####";
        map += "#..###.........#";
        map += "#...###........#";
        map += "#..............#";
        map += "#..............#";
        map += "#..............#";
        map += "#...........####";
        map += "###...........##";
        map += "#.........######";
        map += "#.....##########";
        map += "################";
    }

    @Override
    public void update(GameContainer gc, float elapsedTime) {

        final float vel = 1.25f;

        if ( gc.getInput().isKeyHeld(KeyCode.D) ) {
            playerAngle += vel * elapsedTime;
        }

        if ( gc.getInput().isKeyHeld(KeyCode.A) ) {
            playerAngle -= vel * elapsedTime;
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
        // For each column on screen
        float fieldOfView = (float) (Math.PI / 4.0f);
        for ( int x = 0; x < gc.getRenderer().getW(); x++ ) {
            float rayAngle = (playerAngle - fieldOfView / 2.0f) + ((float)x / (float)gc.getRenderer().getW()) * fieldOfView;
            float distanceWall = 0;
            boolean hitWall = false;
            boolean boundary = false;

            float eyeX = (float)Math.sin(rayAngle);
            float eyeY = (float)Math.cos(rayAngle);

            float depth = 16;
            while ( !hitWall && distanceWall < depth) {
                distanceWall += 0.1f;

                int testX = (int)(playerPos.getX() + eyeX * distanceWall);
                int testY = (int)(playerPos.getY() + eyeY * distanceWall);

                if ( testX < 0 || testX >= mapSize.getX() ||
                        testY < 0 || testY >= mapSize.getY() ) {
                    hitWall = true;
                    distanceWall = depth;
                } else {
                    if ( map.toCharArray()[testY * mapSize.getX() + testX] == '#' ) {
                        hitWall = true;

                        ArrayList<Pair<Float, Float>> p = new ArrayList<>(); // Distance and dot product

                        for ( int tx = 0; tx < 2; tx++ ) {
                            for ( int ty = 0; ty < 2; ty++ ) {
                                float vy = (float)testY + ty - playerPos.getY();
                                float vx = (float)testX + tx - playerPos.getX();
                                float d = (float)Math.sqrt(vx * vx + vy * vy);
                                float dot = (eyeX * vx / d) + (eyeY * vy / d);

                                p.add(new Pair<>(d, dot));
                            }
                        }

                        p.sort((o1, o2) -> Float.compare(o1.getKey(), o2.getKey()));

                        float bound = 0.001f;
                        if ( Math.acos(p.get(0).getValue()) < bound ) {
                            boundary = true;
                        }
                        if ( Math.acos(p.get(1).getValue()) < bound ) {
                            boundary = true;
                        }

                    }
                }
            }

            int ceiling = (int) ((float)(gc.getRenderer().getH() / 2.0) - gc.getRenderer().getH() / distanceWall);
            int floor = gc.getRenderer().getH() - ceiling;

            // Shading of the wall
            float value = distanceWall / depth;

            for ( int y = 0; y < gc.getRenderer().getH(); y++ ) {
                float value2 = 2 * y / (float)gc.getRenderer().getH();

                if ( y < ceiling ) { // ceiling
                    gc.getRenderer().getP()[y * gc.getRenderer().getW() + x] = (0xff << 24 | (int)(255 * (1 - value2)) << 8 | (int)(255 * (1 - value2)));
                } else if ( y > ceiling && y <= floor ) { // wall
                    if ( boundary ) {
                        gc.getRenderer().getP()[y * gc.getRenderer().getW() + x] = 0xff000000;
                    } else {
                        gc.getRenderer().getP()[y * gc.getRenderer().getW() + x] = (0xff << 24 | (int) (255 * (1 - value)) << 16);
                    }
                } else { // floor
                    gc.getRenderer().getP()[y * gc.getRenderer().getW() + x] = (0xff << 24 | (int)(255 * value2) << 8);
                }
            }
        }
    }

}
