package rayCastWorld;

import game.Renderer;
import points2d.Vec2df;
import points2d.Vec2di;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * The ray cast world engine
 */
public abstract class Engine {

    public enum CellSide {
        NORTH,
        EAST,
        SOUTH,
        WEST,
        TOP,
        BOTTOM
    }

    /**
     * The objects of the world
     */
    private HashMap<Integer, Object> mapObjects;

    private Vec2di screenSize;

    private Vec2di halfScreenSize;

    private Vec2df floatScreenSize;

    private float fieldOfView = 0.0f;

    private float[] depthBuffer;

    private Vec2df cameraPos = new Vec2df(5.0f, 5.0f);

    private float cameraHeading = 0.0f;

    public Engine(int screenWidth, int screenHeight, float fov) {
        screenSize = new Vec2di(screenWidth, screenHeight);
        halfScreenSize = new Vec2di(screenWidth / 2, screenHeight / 2);
        floatScreenSize = new Vec2df((float)screenWidth, (float)screenHeight);

        fieldOfView = fov;
        depthBuffer = new float[screenSize.getX() * screenSize.getY()];
        mapObjects = new HashMap<>();
    }

    public void setCamera(Vec2df pos, float heading) {
        cameraPos = pos;
        cameraHeading = heading;
    }

    public void update(float elapsedTime) {
        for (Map.Entry<Integer, Object> e : mapObjects.entrySet()) {
            Object object = e.getValue();

            if ( !object.isActive() ) {
                continue;
            }

            int steps = 1;
            float delta = elapsedTime;
            Vec2df temp = new Vec2df(object.getVel());
            temp.multiply(elapsedTime);
            float totalTravel = temp.mag2();
            float totalRadius = object.getRadius() * object.getRadius();

            if ( totalTravel >= totalRadius ) {
                steps = (int)Math.ceil(totalTravel / totalRadius);
                delta = elapsedTime / steps;
            }

            for ( int n = 0; n < steps; n++ ) {
                Vec2df potentialPosition = new Vec2df(object.getPos());
                potentialPosition.addToX(object.getVel().getX() * delta);
                potentialPosition.addToY(object.getVel().getY() * delta);

                if ( object.isCollideWithObject() ) {
                    for ( Map.Entry<Integer, Object> e2 : mapObjects.entrySet() ) {
                        Object target = e2.getValue();

                        if ( !target.isCollideWithObject() ) {
                            continue;
                        }

                        if ( target.equals(object) ) {
                            continue;
                        }

                        if ( doOverlap(object, target) ) {
                            float dist = dist(object, target);
                            float overlap = 1.0f * (dist - object.getRadius() - target.getRadius());

                            potentialPosition.addToX(-(object.getPos().getX() - target.getPos().getX()) / dist * overlap);
                            potentialPosition.addToY(-(object.getPos().getY() - target.getPos().getY()) / dist * overlap);

                            if ( target.isCanBeMoved() ) {
                                target.getPos().addToX((object.getPos().getX() - target.getPos().getX()) / dist * overlap);
                                target.getPos().addToY((object.getPos().getY() - target.getPos().getY()) / dist * overlap);
                            }

                            if ( object.isNotifyObjectCollision() ) {
                                handleObjectVsObject(object, target);
                            }
                        }
                    }
                }

                if ( object.isCollideWithScenery() ) {
                    Vec2di currentCell = new Vec2di((int)object.getPos().getX(), (int)object.getPos().getY());
                    Vec2di targetCell = new Vec2di((int)potentialPosition.getX(), (int)potentialPosition.getY());
                    Vec2di areaTopLeft = new Vec2di(
                            Math.min(currentCell.getX(), targetCell.getX()) - 1,
                            Math.min(currentCell.getY(), targetCell.getY()) - 1
                    );
                    Vec2di areaBottomRight = new Vec2di(
                            Math.max(currentCell.getX(), targetCell.getX()) + 1,
                            Math.max(currentCell.getY(), targetCell.getY()) + 1
                    );

                    // Iterate through each cell in test area
                    Vec2di cell = new Vec2di();
                    for ( cell.setY(areaTopLeft.getY()); cell.getY() <= areaBottomRight.getY(); cell.addToY(1) ) {
                        for ( cell.setX(areaTopLeft.getX()); cell.getX() <= areaBottomRight.getX(); cell.addToX(1) ) {
                            Vec2df cellMiddle = new Vec2df((float)cell.getX(), (float)cell.getY() + 0.5f);

                            if ( isLocationSolid(cellMiddle.getX(), cell.getY()) ) {
                                Vec2df nearestPoint = new Vec2df(
                                        Math.max((float)cell.getX(), Math.min(potentialPosition.getX(), (float)(cell.getX() + 1))),
                                        Math.max((float)cell.getY(), Math.min(potentialPosition.getY(), (float)(cell.getY() + 1)))
                                );

                                Vec2df rayToNearest = new Vec2df(
                                        nearestPoint.getX() - potentialPosition.getX(),
                                        nearestPoint.getY() - potentialPosition.getY()
                                );
                                float overlap = object.getRadius() - rayToNearest.mag();

                                // todo comprobar si overlap "isnan"

                                if ( overlap > 0 ) {
                                    Vec2df norm = (Vec2df) rayToNearest.normal();
                                    potentialPosition.addToX(-norm.getX() * overlap);
                                    potentialPosition.addToY(-norm.getY() * overlap);

                                    if ( object.isNotifyObjectCollision() ) {
                                        CellSide side = CellSide.BOTTOM;
                                        if ( nearestPoint.getX() == (float)cell.getX() ) {
                                            side = CellSide.WEST;
                                        }
                                        if ( nearestPoint.getX() == (float)(cell.getY() + 1) ) {
                                            side = CellSide.EAST;
                                        }
                                        if ( nearestPoint.getY() == (float)cell.getY() ) {
                                            side = CellSide.NORTH;
                                        }
                                        if ( nearestPoint.getY() == (float)(cell.getY() + 1) ) {
                                            side = CellSide.SOUTH;
                                        }

                                        handleObjectVsScenery(object, cell.getX(), cell.getY(), side,
                                                nearestPoint.getX() - (float)(cell.getX()),
                                                nearestPoint.getY() - (float)(cell.getY()));
                                    }
                                }
                            }
                        }
                    }
                }

                object.setPos(potentialPosition);
            }
        }
    }

    public float dist2(Object ob1, Object ob2) {
        return (ob2.getPos().getX() - ob1.getPos().getX()) * (ob2.getPos().getX() - ob1.getPos().getX()) +
                (ob2.getPos().getY() - ob1.getPos().getY()) * (ob2.getPos().getY() - ob1.getPos().getY());
    }

    public float dist(Object ob1, Object ob2) {
        // todo hay metodos matematicos que son más optimos para raiz, seno y coseno
        return (float) Math.sqrt(dist2(ob1, ob2));
    }

    public boolean doOverlap(Object ob1, Object ob2) {
        return dist2(ob1, ob2) <= (ob1.getRadius() + ob2.getRadius()) * (ob1.getRadius() + ob2.getRadius());
    }

    private void depthDraw(int x, int y, float z, int pixel, Renderer renderer) {
        if ( z <= depthBuffer[y * screenSize.getX() + x] ) {
            renderer.getP()[y * screenSize.getX() + x] = pixel;
            depthBuffer[y * screenSize.getX() + x] = z;
        }
    }

    public void render(Renderer renderer) {
        // Clear screen and depth buffer
        Arrays.fill(depthBuffer, Integer.MAX_VALUE);
        renderWorld(renderer);
        renderObjects(renderer);
    }

    private void renderWorld(Renderer renderer) {
        // For each column on screen...
        for ( int x = 0; x < screenSize.getX(); x++ ) {
            // Create a ray eminating from player position into world
            float rayAngle = (cameraHeading - (fieldOfView / 2.90f)) + ((float)x / floatScreenSize.getX()) * fieldOfView;

            // Create unit vector for that ray
            Vec2df rayDirection = new Vec2df((float)Math.cos(rayAngle), (float)Math.sin(rayAngle));

            TileHit hit = new TileHit();

            float rayLength = Integer.MAX_VALUE;

            if ( castRayDDA(cameraPos, rayDirection, hit) ) {
                Vec2df ray = new Vec2df(
                        hit.getHitPos().getX() - cameraPos.getX(),
                        hit.getHitPos().getY() - cameraPos.getY()
                );
                rayLength = ray.mag() * (float)Math.cos(rayAngle - cameraHeading);
            }

            float ceiling = (floatScreenSize.getY() / 2.0f) - (floatScreenSize.getY() / rayLength);
            float floor = floatScreenSize.getY() - ceiling;
            float wallHeight = floor - ceiling;
            float floorHeight = floatScreenSize.getY() - floor;

            // Now draw the column from top to bottom
            for ( int y = 0; y < screenSize.getY(); y++ ) {
                if ( y < (int)(ceiling) ) {
                    float planeZ = (floatScreenSize.getY() / 2.0f) / ((floatScreenSize.getY() / 2.0f) - (float)y);

                    Vec2df planePoint = new Vec2df(
                            cameraPos.getX() + rayDirection.getX() * planeZ * 2.0f / (float)Math.cos(rayAngle - cameraHeading),
                            cameraPos.getY() + rayDirection.getY() * planeZ * 2.0f / (float)Math.cos(rayAngle - cameraHeading)
                    );

                    int planeTileX = (int)planePoint.getX();
                    int planeTileY = (int)planePoint.getY();

                    float planeSampleX = planePoint.getX() - planeTileX;
                    float planeSampleY = planePoint.getY() - planeTileY;

                    int pixel = selectSceneryPixel(planeTileX, planeTileY, CellSide.TOP, planeSampleX, planeSampleY, planeZ);

                    renderer.getP()[y * renderer.getW() + x] = pixel;
                } else if ( y > (int)ceiling && y <= (int)floor ) {
                    float sampleY = (float)(y) - ceiling / wallHeight;
                    int pixel = selectSceneryPixel(hit.getTilePos().getX(), hit.getTilePos().getY(), hit.getSide(), hit.getSampleX(), sampleY, rayLength);
                    depthDraw(x, y, rayLength, pixel, renderer);
                } else {
                    float planeZ = (floatScreenSize.getY() / 2.0f) / ((float)y - (floatScreenSize.getY() / 2.0f));

                    Vec2df planePoint = new Vec2df(
                            cameraPos.getX() + rayDirection.getX() * planeZ * 2.0f / (float)Math.cos(rayAngle - cameraHeading),
                            cameraPos.getY() + rayDirection.getY() * planeZ * 2.0f / (float)Math.cos(rayAngle - cameraHeading)
                    );

                    int planeTileX = (int)planePoint.getX();
                    int planeTileY = (int)planePoint.getY();

                    float planeSampleX = planePoint.getX() - planeTileX;
                    float planeSampleY = planePoint.getY() - planeTileY;

                    int pixel = selectSceneryPixel(planeTileX, planeTileY, CellSide.BOTTOM, planeSampleX, planeSampleY, planeZ);

                    renderer.getP()[y * renderer.getW() + y] = pixel;
                }
            }
        }
    }

    private void renderObjects(Renderer renderer) {
        for ( Map.Entry<Integer, Object> entry : mapObjects.entrySet() ) {
            Object object = entry.getValue();

            if ( !object.isVisible() ) {
                continue;
            }

            Vec2df vecObject = new Vec2df(
                    object.getPos().getX() - cameraPos.getX(),
                    object.getPos().getY() - cameraPos.getY()
            );

            float distanceToObject = vecObject.mag();

            float objectAngle = (float)Math.atan2(vecObject.getY(), vecObject.getY()) - cameraHeading;
            if ( objectAngle < -3.14159f ) {
                objectAngle += 2.0f * 3.14159f;
            }
            if ( objectAngle > 3.14159f ) {
                objectAngle -= 2.0f * 3.14159f;
            }

            boolean inPlayerFov = Math.abs(objectAngle) < (fieldOfView + (1.0f / distanceToObject)) / 2.0f;

            if ( inPlayerFov && vecObject.mag() >= 0.5f ) {
                Vec2df floorPoint = new Vec2df(
                        (0.5f * ((objectAngle / (fieldOfView * 0.5f))) + 0.5f) * floatScreenSize.getX(),
                        (floatScreenSize.getY() / 2.0f) + (floatScreenSize.getY() / distanceToObject) / (float)Math.cos(objectAngle / 2.0f)
                );

                Vec2df objectSize = new Vec2df(
                        (float)(getObjectWidth(object.getId())),
                        (float)(getObjectHeight(object.getId()))
                );

                objectSize.multiply(2.0f * floatScreenSize.getY());
                objectSize.multiply(1 / distanceToObject);

                Vec2df objectTopLeft = new Vec2df(
                        floorPoint.getX() - objectSize.getX() / 2.0f,
                        floorPoint.getY() - objectSize.getY()
                );

                for ( float y = 0; y < objectSize.getY(); y++ ) {
                    for ( float x = 0; x < objectSize.getX(); x++ ) {
                        float sampleX = x / objectSize.getX();
                        float sampleY = y / objectSize.getY();

                        float niceAngle = cameraHeading - object.getHeading() + 3.14159f / 4.0f;
                        if ( niceAngle < 0 ) {
                            niceAngle += 2.0f * 3.14159f;
                        }
                        if ( niceAngle > 2.0f * 3.14159f ) {
                            niceAngle -= 2.0f * 3.14159f;
                        }
                        int pixel = selectObjectPixel(object.getId(), sampleX, sampleY, distanceToObject, niceAngle);

                        Vec2di a = new Vec2di(
                                (int)(objectTopLeft.getX() + x),
                                (int)(objectTopLeft.getY() + y)
                        );

                        if ( a.getX() >= 0 && a.getX() < screenSize.getX() &&
                                a.getY() >= 0 && a.getY() < screenSize.getY() && ((pixel >> 24) & 0xff) == 255 ) {
                            depthDraw(a.getX(), a.getY(), distanceToObject, pixel, renderer);
                        }
                    }
                }

            }
        }
    }

    public boolean castRayDDA(Vec2df origin, Vec2df direction, TileHit hit) {
        Vec2df rayDelta = new Vec2df(
                (float)Math.sqrt(1 + (direction.getY() / direction.getX()) * (direction.getY() / direction.getX())),
                (float)Math.sqrt(1 + (direction.getX() / direction.getY()) * (direction.getX() / direction.getY()))
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

        Vec2df intersection = new Vec2df();
        Vec2di hitTile;
        float maxDistance = 100.0f;
        float distance = 0.0f;
        boolean tileFound = false;
        while ( !tileFound && distance < maxDistance ) {
            if ( rayLength1D.getX() < rayLength1D.getY() ) {
                mapCheck.addToX(stepDistance.getX());
                distance = rayLength1D.getX();
                rayLength1D.addToX(rayDelta.getX());
            } else {
                mapCheck.addToY(stepDistance.getY());
                distance = rayLength1D.getY();
                rayLength1D.addToY(rayDelta.getY());
            }

            /*Vec2df rayDist = new Vec2df(
                    mapCheck.getX() - origin.getX(),
                    mapCheck.getY() - origin.getY()
            );
            distance = rayDist.mag();*/

            if ( isLocationSolid((float)mapCheck.getX(), (float)mapCheck.getY()) ) {
                hitTile = new Vec2di(mapCheck);
                tileFound = true;
                hit.setTilePos(mapCheck);
                hit.setLength(distance);

                // Find accurate Hit Location
                float m = direction.getY() / direction.getX();

                if ( origin.getY() <= mapCheck.getY() ) {
                    if ( origin.getX() <= mapCheck.getX() ) {
                        hit.setSide(CellSide.WEST);
                        intersection.setY(m * (mapCheck.getX() - origin.getX()) + origin.getY());
                        intersection.setX((float)(mapCheck.getX()));
                        hit.setSampleX(intersection.getY() - (float)Math.floor(intersection.getY()));
                    } else if ( origin.getX() >= (mapCheck.getX() + 1) ) {
                        hit.setSide(CellSide.EAST);
                        intersection.setY(m * ((mapCheck.getX() + 1) - origin.getX()) + origin.getY());
                        intersection.setX((float)(mapCheck.getX() + 1));
                        hit.setSampleX(intersection.getY() - (float)Math.floor(intersection.getY()));
                    } else {
                        hit.setSide(CellSide.NORTH);
                        intersection.setY((float)mapCheck.getY());
                        intersection.setX((mapCheck.getY() - origin.getY()) / m + origin.getX());
                        hit.setSampleX(intersection.getX() - (float)Math.floor(intersection.getX()));
                    }

                    if ( intersection.getY() < mapCheck.getY() ) {
                        hit.setSide(CellSide.SOUTH); //CellSide.NORTH
                        intersection.setY((float)mapCheck.getY());
                        intersection.setX((mapCheck.getY() - origin.getY()) / m + origin.getX());
                        hit.setSampleX(intersection.getX() - (float)Math.floor(intersection.getX()));
                    }
                } else if ( origin.getY() >= mapCheck.getY() + 1 ) {
                    if ( origin.getX() <= mapCheck.getX() ) {
                        hit.setSide(CellSide.WEST);
                        intersection.setY(m * (mapCheck.getX() - origin.getX()) + origin.getY());
                        intersection.setX((float)mapCheck.getX());
                        hit.setSampleX(intersection.getY() - (float)Math.floor(intersection.getY()));
                    } else if ( origin.getX() >= (mapCheck.getX() + 1) ) {
                        hit.setSide(CellSide.EAST);
                        intersection.setY(m * ((mapCheck.getX() + 1) - origin.getX()) + origin.getY());
                        intersection.setX((float)(mapCheck.getX() + 1));
                        hit.setSampleX(intersection.getY() - (float)Math.floor(intersection.getY()));
                    } else {
                        hit.setSide(CellSide.SOUTH);
                        intersection.setY((float)(mapCheck.getY() + 1));
                        intersection.setX(((mapCheck.getY() + 1) - origin.getY()) / m + origin.getX());
                        hit.setSampleX(intersection.getX() - (float)Math.floor(intersection.getX()));
                    }

                    if ( intersection.getY() > (mapCheck.getY() + 1) ) {
                        hit.setSide(CellSide.NORTH); //CellSide.SOUTH
                        intersection.setY((float)(mapCheck.getY() + 1));
                        intersection.setX(((mapCheck.getY() + 1)- origin.getY()) / m + origin.getX());
                        hit.setSampleX(intersection.getX() - (float)Math.floor(intersection.getX()));
                    }
                } else {
                    if ( origin.getX() <= mapCheck.getX() ) {
                        hit.setSide(CellSide.WEST);
                        intersection.setY(m * (mapCheck.getX() + origin.getX()) + origin.getY());
                        intersection.setX((float)(mapCheck.getX()));
                        hit.setSampleX(intersection.getY() - (float)Math.floor(intersection.getY()));
                    } else if ( origin.getX() >= (mapCheck.getX() + 1) ) {
                        hit.setSide(CellSide.EAST);
                        intersection.setY(m * ((mapCheck.getX() + 1) - origin.getX()) + origin.getY());
                        intersection.setX((float)(mapCheck.getX() + 1));
                        hit.setSampleX(intersection.getY() - (float)Math.floor(intersection.getY()));
                    }
                }

                hit.setHitPos(intersection);
            }
        }
        return tileFound;
    }

    public abstract int selectSceneryPixel(int tileX, int tileY, CellSide side, float sampleX, float sampleY, float distance);

    public abstract boolean isLocationSolid(float tileX, float tileY);

    public abstract float getObjectWidth(int id);

    public abstract float getObjectHeight(int id);

    public abstract int selectObjectPixel(int id, float sampleX, float sampleY, float distance, float angle);

    public abstract void handleObjectVsScenery(Object object, int tileX, int tileY, CellSide side, float offsetX, float offsetY);

    public abstract void handleObjectVsObject(Object obj1, Object obj2);

}
