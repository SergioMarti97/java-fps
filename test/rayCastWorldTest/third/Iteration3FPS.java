package rayCastWorldTest.third;

import javafx.scene.input.KeyCode;
import olcPGEApproach.AbstractGame;
import olcPGEApproach.GameContainer;
import olcPGEApproach.gfx.images.Image;
import olcPGEApproach.gfx.images.ImageTile;
import olcPGEApproach.vectors.points2d.Vec2df;
import olcPGEApproach.vectors.points2d.Vec2di;
import rayCastWorld.CellSide;
import rayCastWorld.map.RudimentaryMap;
import rayCastWorld.objects.Obj;
import rayCastWorld.objects.SourceLight;
import rayCastWorld.renderer.RayCastingWorldRender;
import rayCastWorld.renderer.RayShadowing;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class Iteration3FPS implements AbstractGame {

    private RudimentaryMap map;

    private Image imgWall;

    private Image imgFloor;

    private Image streetLight;

    private Image grass;

    private ImageTile imageTile;

    private RayCastingWorldRender r;

    private HashMap<Integer, Obj> objects;

    Vec2df potentialPosition = new Vec2df();

    Vec2di currentCell = new Vec2di();

    Vec2di targetCell = new Vec2di();

    Vec2di areaTL = new Vec2di();

    Vec2di areaBR = new Vec2di();

    Vec2df nearestPoint = new Vec2df();

    Vec2df rayToNearest = new Vec2df();

    private long accTimeUpdate = 0L;

    private int countUpdate = 0;

    private long accTimeRender = 0L;

    private int countRender = 0;

    private boolean growing = true;

    @Override
    public void initialize(GameContainer gc) {
        
        map = new RudimentaryMap(
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
                        "################################################################",
        64, 32);

        grass = new Image("/grass.png");
        streetLight = new Image("/streetlight.png");
        imageTile = new ImageTile("dungeon/dg_features32.png", 32, 32);
        setTextures(imageTile);

        objects = new HashMap<>();
        objects.put(0, new SourceLight(0, new Vec2df(map.getSize().getX() / 2.0f, map.getSize().getY() / 2.0f + 3.0f), 2, 1.0f));
        objects.put(1, new SourceLight(0, new Vec2df(map.getSize().getX() / 2.0f + 2.5f, map.getSize().getY() / 2.0f + 3.0f), 20, 1.0f));

        /*Random rnd = new Random();
        for (int i = 0; i < 100; i++) {
            int x = rnd.nextInt(map.getSize().getX() + 1) - 1;
            int y = rnd.nextInt(map.getSize().getY() + 1) - 1;
            objects.put(i + 2, new ObjectRayCastWorld(1, new Vec2df(x, y)));
        }*/

        r = new RayCastingWorldRender(gc.getRenderer()) {

            @Override
            public boolean isLocationSolid(float x, float y) {
                return map.getChar((int)x, (int)y) == '#';
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
                        return 0xff00ffff;
                }

                AtomicReference<Float> light = new AtomicReference<>(0.2f);
                objects.forEach((id, o)->{
                    if (o instanceof SourceLight) {
                        ((SourceLight)o).calLight(planeTileX, planeTileY, planeSampleX, planeSampleY);
                        if (((SourceLight)o).isNear()) {
                            light.updateAndGet(v -> v / ((SourceLight)o).getLightSourceDistance());
                            if (light.get() > 1) {
                                light.set(1f);
                            }
                        }
                    }
                });

                if ( side == CellSide.EAST || side == CellSide.NORTH ) {
                    light.updateAndGet(v -> v * 2);
                }

                return RayShadowing.shadowPixel(pixel,  (1 - RayShadowing.calShadow(planeSampleZ, camera.getDepth())) * light.get()); //
            }

            @Override
            public int selectObjectPixel(int id, float sampleX, float sampleY, float distanceToObject, float niceAngle) {
                if (id == 0) {
                    return streetLight.getSample(sampleX, sampleY);
                } else {
                    return grass.getSample(sampleX, sampleY);
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
        };
                
        r.getCamera().getPos().setX(map.getSize().getX() / 2.0f);
        r.getCamera().getPos().setY(map.getSize().getY() / 2.0f);
    }

    private void setTextures(ImageTile imageTile) {
        int[] groupWallsY = new int[] {0, 1, 6, 10};
        int[] groupFloorY = new int[] {3, 5};

        Random rnd = new Random();
        // Determine randomly the image for the Walls
        int wallsTilePosX;
        int wallsTilePosY = groupWallsY[rnd.nextInt(groupWallsY.length)];
        switch (wallsTilePosY) {
            case 0: case 1: case 10: default:
                wallsTilePosX = rnd.nextInt(3) + 3;
                break;
            case 6:
                wallsTilePosX = rnd.nextInt(3) + 6;
                break;
        }
        imgWall = imageTile.getTileImage(wallsTilePosX, wallsTilePosY);

        // Determine randomly the image for the floor
        int floorTilePosX;
        int floorTilePosY = groupFloorY[rnd.nextInt(groupFloorY.length)];
        switch (floorTilePosY) {
            case 3: default:
                floorTilePosX = rnd.nextInt(6) + 3;
                break;
            case 5:
                floorTilePosX = rnd.nextInt(3);
                break;
        }
        imgFloor = imageTile.getTileImage(floorTilePosX, floorTilePosY);
    }

    @Override
    public void update(GameContainer gc, float elapsedTime) {

        long t1 = System.nanoTime();

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

        if ( gc.getInput().isKeyHeld(KeyCode.SPACE) ) {
            setTextures(imageTile);
        }


        if (growing) {
            objects.get(1).getPos().addToX(0.05f);
        } else {
            objects.get(1).getPos().addToX(-0.05f);
        }
        float min = map.getSize().getX() / 2.0f + 2.5f;
        float max = map.getSize().getX() / 2.0f + 10.5f;
        if (objects.get(1).getPos().getX() <= min) {
            growing = true;
        }
        if (objects.get(1).getPos().getX() >= max) {
            growing = false;
        }

        potentialPosition.setX(r.getCamera().getPos().getX() + r.getCamera().getVel().getX() * elapsedTime);
        potentialPosition.setY(r.getCamera().getPos().getY() + r.getCamera().getVel().getY() * elapsedTime);

        currentCell.setX((int)Math.floor(r.getCamera().getPos().getX()));
        currentCell.setY((int)Math.floor(r.getCamera().getPos().getY()));
        targetCell.setX((int)potentialPosition.getX());
        targetCell.setY((int)potentialPosition.getY());
        areaTL.setX(Math.max(0, Math.min(currentCell.getX(), targetCell.getX()) - 1));
        areaTL.setY(Math.max(0, Math.min(currentCell.getY(), targetCell.getY()) - 1));
        areaBR.setX(Math.max(map.getSize().getX(), Math.min(currentCell.getX(), targetCell.getX() + 1)));
        areaBR.setY(Math.max(map.getSize().getY(), Math.min(currentCell.getY(), targetCell.getY() + 1)));

        for (int y = areaTL.getY(); y < areaBR.getY(); y++) {
            for (int x = areaTL.getX(); x < areaBR.getX(); x++) {
                if (map.getChar(x, y) == '#') {
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

        long t2 = System.nanoTime();
        long time = (t2 - t1);
        accTimeUpdate += time;
        countUpdate++;
        if (accTimeUpdate > 1000000000.0f) {
            System.out.printf("Avg elapsed time updates: %.6f\n", accTimeUpdate / (1000000000.0f * countUpdate));
            accTimeUpdate -= 1000000000.0f;
            countUpdate = 0;
        }
    }

    @Override
    public void render(GameContainer gc) {
        long t1 = System.nanoTime();
        r.render(objects);
        long t2 = System.nanoTime();
        long time = (t2 - t1);
        accTimeRender += time;
        countRender++;
        if (accTimeRender > 1000000000L) {
            System.out.printf("(5) Avg elapsed time render: %.6f\n", accTimeRender / (1000000000.0f * countRender));
            accTimeRender -= 1000000000L;
            countRender = 0;
        }
    }

}
