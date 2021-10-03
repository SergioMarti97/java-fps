package multithreading;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;
import olcPGEApproach.AbstractGame;
import olcPGEApproach.GameContainer;
import olcPGEApproach.gfx.images.Image;
import olcPGEApproach.gfx.images.ImageTile;
import olcPGEApproach.vectors.points2d.Vec2df;
import olcPGEApproach.vectors.points2d.Vec2di;
import rayCastWorld.CellSide;
import rayCastWorld.objects.Obj;
import rayCastWorld.renderer.RayCastingWorldRender;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class MultithreadingController implements Initializable {

    /**
     * The image view from the layout of the application
     */
    public ImageView imgView;

    /**
     * Needed service to work with threads. It isn't essential
     * but in the original example what I follow is used
     */
    private ScheduledService<Boolean> scheduledService;

    /**
     * The pool of threads
     */
    private ThreadPoolExecutor executor;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setScheduleService();

        AbstractGame fps = new AbstractGame() {

            /**
             * The ray cast world renderer
             */
            private RayCastingWorldRender rcwRenderer;

            /**
             * The map of the game
             */
            private String map = "";

            /**
             * The dimensions of the map
             */
            private Vec2di mapSize;

            /**
             * the image of the walls
             */
            private Image imgWall;

            /**
             * The image for the floor
             */
            private Image imgFloor;

            /**
             * Image of mario bros
             */
            private Image imgMario;

            private HashMap<Integer, Obj> objects;

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

                rcwRenderer = new RayCastingWorldRender(gc.getRenderer()) {
                    @Override
                    public boolean isLocationSolid(float x, float y) {
                        return map.toCharArray()[(int) y * mapSize.getX() + (int) x] == '#';
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

                        float shadow = 1.0f - Math.min(planeSampleZ / camera.getDepth(), 1.0f);

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
                rcwRenderer.getCamera().getPos().setX(mapSize.getX() / 2.0f);
                rcwRenderer.getCamera().getPos().setY(mapSize.getY() / 2.0f);

                imgMario = new Image("/mario.png");
                ImageTile imageTile = new ImageTile("dungeon/dg_features32.png", 32, 32);
                imgWall = imageTile.getTileImage(3, 0);
                imgFloor = imageTile.getTileImage(0, 5);

                objects = new HashMap<>();
                objects.put(0, new Obj(0, new Vec2df(mapSize.getX() / 2.0f, mapSize.getY() / 2.0f + 3.0f)));

            }

            @Override
            public void update(GameContainer gc, float elapsedTime) {
                final float vel = 6.25f;
                final float rotVel = 1.5f;

                if (gc.getInput().isKeyHeld(KeyCode.D)) {
                    rcwRenderer.getCamera().turn(rotVel * elapsedTime);
                }

                if (gc.getInput().isKeyHeld(KeyCode.A)) {
                    rcwRenderer.getCamera().turn(-rotVel * elapsedTime);
                }

                if (gc.getInput().isKeyHeld(KeyCode.W)) {
                    rcwRenderer.getCamera().walk(vel * elapsedTime);
                }

                if (gc.getInput().isKeyHeld(KeyCode.S)) {
                    rcwRenderer.getCamera().walk(-vel * elapsedTime);
                }
            }

            @Override
            public void render(GameContainer gc) {
                executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
                executor.execute(() -> rcwRenderer.render(objects));
                executor.shutdown();
                scheduledService.restart();
            }
        };
        GameContainer gc = new GameContainer(fps, (int) imgView.getFitWidth(), (int) imgView.getFitHeight(), imgView);
        imgView.setImage(gc.getImg());
        gc.getTimer().start();
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
                            //Platform.runLater(() -> lblStatus.setText(executor.getCompletedTaskCount() + " of " + executor.getTaskCount() + " tasks finished"));
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

}
