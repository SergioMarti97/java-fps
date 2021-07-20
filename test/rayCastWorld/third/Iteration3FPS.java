package rayCastWorld.third;

import game.AbstractGame;
import game.GameContainer;
import game.gfx.Image;
import game.gfx.ImageTile;
import javafx.scene.input.KeyCode;
import points2d.Vec2df;
import points2d.Vec2di;
import rayCastWorld.CellSide;
import rayCastWorld.Engine2;
import rayCastWorld.Object;

public class Iteration3FPS implements AbstractGame {

    private String map = "";

    private Vec2di mapSize;
    
    private Engine2 rcw;

    private Image imgWall;

    private Image imgFloor;

    private Image imgMario;

    private ImageTile imageTile;
    
    @Override
    public void initialize(GameContainer gc) {
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
        
        rcw = new Engine2() {
            @Override
            public boolean isLocationSolid(float x, float y) {
                return map.toCharArray()[(int)y * mapSize.getX() + (int)x] == '#';
            }

            @Override
            public int selectSceneryPixel(
                    int planeTileX, 
                    int planeTileY, 
                    CellSide side, 
                    float planeSampleX, 
                    float planeSampleY, 
                    float planeSampleZ
            ) {
                int pixel;
                switch (side) {
                    default:
                        pixel = imgWall.getSample(planeSampleX, planeSampleY);
                        break;
                    case BOTTOM:
                        pixel = imgFloor.getSample(planeSampleX, planeSampleY);
                        break;
                    case TOP:
                        pixel = 0xff00ffff;
                        break;
                }

                float shadow = 1.0f - Math.min(planeSampleZ / this.getDepth(), 1.0f);

                switch ( side ) {
                    case SOUTH: case EAST:
                        shadow *= 0.3f;
                        break;
                }

                int r = pixel >> 16 & 0xff;
                int g = pixel >> 8 & 0xff;
                int b = pixel & 0xff;

                return (0xff << 24 | (int) (r * shadow) << 16 | (int) (g * shadow) << 8 | (int) (b * shadow));
            }

            @Override
            public int selectObjectPixel(
                    int id, 
                    float sampleX, 
                    float sampleY, 
                    float distanceToObject, 
                    float niceAngle
            ) {
                return imgMario.getSample(sampleX, sampleY);
            }

            @Override
            public float getObjectWidth(int id) {
                return 1;
            }

            @Override
            public float getObjectHeight(int id) {
                return 1;
            }
        };
        rcw.init(gc, new Vec2df(mapSize.getX() / 2.0f, mapSize.getY() / 2.0f), 0.0f);

        imgMario = new Image("/mario.png");
        imageTile = new ImageTile("dungeon/dg_features32.png", 32, 32);
        imgWall = imageTile.getTileImage(3, 0);
        imgFloor = imageTile.getTileImage(0, 5);
        
        rcw.getObjects().put(0, new Object(0, new Vec2df(mapSize.getX() / 2.0f, mapSize.getY() / 2.0f + 3.0f)));
    }

    @Override
    public void update(GameContainer gc, float elapsedTime) {
        final float vel = 5.25f;
        final float rotVel = 1.5f;

        if ( gc.getInput().isKeyHeld(KeyCode.D) ) {
            rcw.setPlayerAngle(rcw.getPlayerAngle() + rotVel * elapsedTime);
        }

        if ( gc.getInput().isKeyHeld(KeyCode.A) ) {
            rcw.setPlayerAngle(rcw.getPlayerAngle() - rotVel * elapsedTime);
        }

        if ( gc.getInput().isKeyHeld(KeyCode.W) ) {
            rcw.getPlayerPos().addToX((float)Math.sin(rcw.getPlayerAngle()) * vel * elapsedTime);
            rcw.getPlayerPos().addToY((float)Math.cos(rcw.getPlayerAngle()) * vel * elapsedTime);
        }

        if ( gc.getInput().isKeyHeld(KeyCode.S) ) {
            rcw.getPlayerPos().addToX(-(float)Math.sin(rcw.getPlayerAngle()) * vel * elapsedTime);
            rcw.getPlayerPos().addToY(-(float)Math.cos(rcw.getPlayerAngle()) * vel * elapsedTime);
        }

        if ( gc.getInput().isKeyHeld(KeyCode.Q) ) {
            rcw.getPlayerPos().addToX((float)Math.sin(rcw.getPlayerAngle()) * vel * elapsedTime);
            rcw.getPlayerPos().addToY(-(float)Math.cos(rcw.getPlayerAngle()) * vel * elapsedTime);
        }

        if ( gc.getInput().isKeyHeld(KeyCode.E) ) {
            rcw.getPlayerPos().addToX(-(float)Math.sin(rcw.getPlayerAngle()) * vel * elapsedTime);
            rcw.getPlayerPos().addToY((float)Math.cos(rcw.getPlayerAngle()) * vel * elapsedTime);
        }
    }

    @Override
    public void render(GameContainer gc) {
        rcw.render(gc);
    }
    
}
