package fps;

import game.AbstractGame;
import game.GameContainer;
import javafx.scene.image.Image;
import points2d.Vec2df;
import points2d.Vec2di;
import rayCastWorld.CellSide;
import rayCastWorld.Engine;
import rayCastWorld.Object;

import java.util.Random;

public class FirstPersonShooterGame implements AbstractGame {

    private Random rnd;

    private Image img;

    private Vec2df pos;

    private Vec2df vel;

    private Engine engine;

    private String map;

    private Vec2di worldSize;

    @Override
    public void initialize(GameContainer gc) {
        rnd = new Random();
        img = new Image("block.png");
        pos = new Vec2df();
        vel = new Vec2df(150f, 150f);

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

        worldSize = new Vec2di(64, 32);

        engine = new Engine(gc.getRenderer().getW(), gc.getRenderer().getH(), 50.0f) {
            @Override
            public int selectSceneryPixel(int tileX, int tileY, CellSide side, float sampleX, float sampleY, float distance) {
                int p = 0;
                switch ( side ) {
                    case TOP:
                        p = 0xff00ffff;
                        break;
                    case BOTTOM:
                        p = 0xff006400;
                    default:
                        p = 0xffffffff;
                        if ( sampleX < 0.05f || sampleX > 0.95f || sampleY < 0.05f || sampleY > 0.95f ) {
                            p = 0xff000000;
                        }
                        break;
                }
                float shadow = 1.0f;
                switch (side) {
                    case SOUTH: case EAST:
                        shadow = 0.3f;
                        break;
                }
                float fdistance = 1.0f - Math.min(distance / 32.0f, 1.0f);

                int r = (p >> 16) & 0xff;
                int g = (p >> 8) & 0xff;
                int b = (p) & 0xff;
                p = 0xff << 24 | (int)(r * fdistance) << 16 | (int)(g * fdistance) << 8 | (int)(b * fdistance);

                return p;
            }

            @Override
            public boolean isLocationSolid(float tileX, float tileY) {
                if ( (int)tileX >= 0 && (int)tileX < worldSize.getX() &&
                        (int)tileY >= 0 && (int)tileY < worldSize.getY()) {
                    return map.charAt((int)tileY * worldSize.getX() + (int)tileX) == 'P';
                } else {
                    return true;
                }
            }

            @Override
            public float getObjectWidth(int id) {
                return 1;
            }

            @Override
            public float getObjectHeight(int id) {
                return 1;
            }

            @Override
            public int selectObjectPixel(int id, float sampleX, float sampleY, float distance, float angle) {
                return 0xff000000;
            }

            @Override
            public void handleObjectVsScenery(Object object, int tileX, int tileY, CellSide side, float offsetX, float offsetY) {

            }

            @Override
            public void handleObjectVsObject(Object obj1, Object obj2) {

            }
        };
    }

    @Override
    public void update(GameContainer gc, float elapsedTime) {
        /*pos.addToX(vel.getX() * elapsedTime);
        pos.addToY(vel.getY() * elapsedTime);
        if (pos.getX() > gc.getRenderer().getW() - img.getWidth() || pos.getX() < 0) {
            vel.setX(-vel.getX());
        }
        if (pos.getY() > gc.getRenderer().getH() - img.getHeight() || pos.getY() < 0) {
            vel.setY(-vel.getY());
        }*/

        engine.update(elapsedTime);
    }

    @Override
    public void render(GameContainer gc) {
        engine.render(gc.getRenderer());
    }

}
