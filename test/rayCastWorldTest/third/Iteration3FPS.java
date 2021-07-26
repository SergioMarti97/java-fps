package rayCastWorldTest.third;

import javafx.scene.input.KeyCode;
import olcPGEApproach.GameContainer;
import olcPGEApproach.gfx.images.Image;
import olcPGEApproach.gfx.images.ImageTile;
import olcPGEApproach.vectors.points2d.Vec2df;
import olcPGEApproach.vectors.points2d.Vec2di;
import rayCastWorld.CellSide;
import rayCastWorld.Object;
import rayCastWorld.RayCastingWorldGame;

public class Iteration3FPS extends RayCastingWorldGame {

    private String map = "";

    private Vec2di mapSize;

    private Image imgWall;

    private Image imgFloor;

    private Image imgMario;

    @Override
    public void initialize(GameContainer gc) {
        super.initialize(gc);
        
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
                
        playerPos.setX(mapSize.getX() / 2.0f); 
        playerPos.setY(mapSize.getY() / 2.0f);

        imgMario = new Image("/mario.png");
        ImageTile imageTile = new ImageTile("dungeon/dg_features32.png", 32, 32);
        imgWall = imageTile.getTileImage(3, 0);
        imgFloor = imageTile.getTileImage(0, 5);

        objects.put(0, new Object(0, new Vec2df(mapSize.getX() / 2.0f, mapSize.getY() / 2.0f + 3.0f)));
    }

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

                /*switch ( side ) {
                    case SOUTH: case EAST:
                        shadow *= 0.3f;
                        break;
                }*/

                /*Vec2df marioPos = getObjects().get(0).getPos();
                Vec2df pixelPos = new Vec2df((float)planeTileX + planeSampleX, (float)planeTileY + planeSampleY);

                float marioDistance = (marioPos.getX() - pixelPos.getX()) * (marioPos.getX() - pixelPos.getX()) +
                        (marioPos.getY() - pixelPos.getY()) * (marioPos.getY() - pixelPos.getY());

                float marioLight = Math.max(0.2f, 1.0f - Math.min(marioDistance / 10.0f, 1.0f));

                shadow *= marioLight;*/

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
        return 0.5f;
    }

    @Override
    public float getObjectHeight(int id) {
        return 0.5f;
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

}
