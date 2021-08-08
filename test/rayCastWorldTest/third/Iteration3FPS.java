package rayCastWorldTest.third;

import javafx.scene.input.KeyCode;
import olcPGEApproach.AbstractGame;
import olcPGEApproach.GameContainer;
import olcPGEApproach.gfx.HexColors;
import olcPGEApproach.gfx.Renderer;
import olcPGEApproach.gfx.images.Image;
import olcPGEApproach.gfx.images.ImageTile;
import olcPGEApproach.vectors.points2d.Vec2df;
import olcPGEApproach.vectors.points2d.Vec2di;
import rayCastWorld.CellSide;
import rayCastWorld.ObjectRayCastWorld;
import rayCastWorld.renderer.RayCastingWorldRender;
import rayCastWorld.renderer.RayShadowing;

import java.util.HashMap;

public class Iteration3FPS implements AbstractGame {

    private String map = "";

    private Vec2di mapSize;

    private Image imgWall;

    private Image imgFloor;

    private Image imgMario;

    private RayCastingWorldRender r;

    private HashMap<Integer, ObjectRayCastWorld> objects;

    Vec2df potentialPosition = new Vec2df();
    Vec2di currentCell = new Vec2di();
    Vec2di targetCell = new Vec2di();
    Vec2di areaTL = new Vec2di();
    Vec2di areaBR = new Vec2di();
    Vec2df nearestPoint = new Vec2df();
    Vec2df rayToNearest = new Vec2df();

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

        r = new RayCastingWorldRender(gc.getRenderer()) {
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
                return RayShadowing.shadowPixel(pixel, planeSampleZ, camera.getDepth());
            }

            @Override
            public int selectObjectPixel(int id, float sampleX, float sampleY, float distanceToObject, float niceAngle) {
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
        };
                
        r.getCamera().getPos().setX(mapSize.getX() / 2.0f);
        r.getCamera().getPos().setY(mapSize.getY() / 2.0f);

        imgMario = new Image("/mario.png");
        ImageTile imageTile = new ImageTile("dungeon/dg_features32.png", 32, 32);
        imgWall = imageTile.getTileImage(3, 0);
        imgFloor = imageTile.getTileImage(0, 5);

        objects = new HashMap<>();
        objects.put(0, new ObjectRayCastWorld(0, new Vec2df(mapSize.getX() / 2.0f, mapSize.getY() / 2.0f + 3.0f)));
    }

    @Override
    public void update(GameContainer gc, float elapsedTime) {
        final float vel = 6.25f;
        final float rotVel = 1.5f;

        if ( gc.getInput().isKeyHeld(KeyCode.D) ) {
            r.getCamera().turn(rotVel * elapsedTime);
        }

        if ( gc.getInput().isKeyHeld(KeyCode.A) ) {
            r.getCamera().turn(-rotVel * elapsedTime);
        }

        if ( gc.getInput().isKeyHeld(KeyCode.W) ) {
            r.getCamera().walk(vel * elapsedTime);
        }

        if ( gc.getInput().isKeyHeld(KeyCode.S) ) {
            r.getCamera().walk(-vel * elapsedTime);
        }

        potentialPosition.setX(r.getCamera().getPos().getX() + r.getCamera().getVel().getX() * elapsedTime);
        potentialPosition.setY(r.getCamera().getPos().getY() + r.getCamera().getVel().getY() * elapsedTime);

        currentCell.setX((int)Math.floor(r.getCamera().getPos().getX()));
        currentCell.setY((int)Math.floor(r.getCamera().getPos().getY()));
        targetCell.setX((int)potentialPosition.getX());
        targetCell.setY((int)potentialPosition.getY());
        areaTL.setX(Math.max(0, Math.min(currentCell.getX(), targetCell.getX()) - 1));
        areaTL.setY(Math.max(0, Math.min(currentCell.getY(), targetCell.getY()) - 1));
        areaBR.setX(Math.max(mapSize.getX(), Math.min(currentCell.getX(), targetCell.getX() + 1)));
        areaBR.setY(Math.max(mapSize.getY(), Math.min(currentCell.getY(), targetCell.getY() + 1)));

        for (int y = areaTL.getY(); y < areaBR.getY(); y++) {
            for (int x = areaTL.getX(); x < areaBR.getX(); x++) {
                if (map.toCharArray()[y * mapSize.getX() + x] == '#') {
                    nearestPoint.setX(Math.max((float)x, Math.min(potentialPosition.getX(), (float) x + 1)));
                    nearestPoint.setY(Math.max((float)y, Math.min(potentialPosition.getY(), (float) y + 1)));

                    rayToNearest.setX(nearestPoint.getX() - potentialPosition.getX());
                    rayToNearest.setY(nearestPoint.getY() - potentialPosition.getY());

                    float overlap = r.getCamera().getRadius() - rayToNearest.mag();
                    if ( overlap > 0 ) {
                        rayToNearest.normal();
                        potentialPosition.addToX(-rayToNearest.getX() * r.getCamera().getRadius() * overlap);
                        potentialPosition.addToY(-rayToNearest.getY() * r.getCamera().getRadius() * overlap);
                    }
                }
            }
        }
        r.getCamera().getPos().setX(potentialPosition.getX());
        r.getCamera().getPos().setY(potentialPosition.getY());
    }

    public void drawMiniMap(Renderer r, int blockSizeWidth, int blockSizeHeight, int offX, int offY) {
        for (int y = 0; y < mapSize.getY(); y++) {
            for (int x = 0; x < mapSize.getX(); x++) {
                r.drawFillRectangle(
                        x * blockSizeHeight + offX,
                        y * blockSizeHeight + offY,
                        blockSizeWidth,
                        blockSizeHeight,
                        map.toCharArray()[y * mapSize.getX() + x] == '#' ?
                                HexColors.WHITE : HexColors.BLACK
                );
            }
        }
        r.drawFillCircle(
                (int)(this.r.getCamera().getPos().getX() * blockSizeWidth) + offX,
                (int)(this.r.getCamera().getPos().getY() * blockSizeHeight) + offY,
                blockSizeWidth / 2,
                HexColors.FANCY_RED
        );
    }

    @Override
    public void render(GameContainer gc) {
        r.render(objects);
        drawMiniMap(gc.getRenderer(), 5, 5, 10, 10);
    }

}
