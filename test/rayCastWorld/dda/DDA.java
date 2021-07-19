package rayCastWorld.dda;

import points2d.Vec2df;
import points2d.Vec2di;

/**
 * This class contains the methods
 * with the DDA (Digital Differential Analysis) algorithm
 * This is for cast rays in a tile
 * world, it is very quick
 */
public class DDA {

    private static final float MAX_DISTANCE = 100.0f;

    public interface Tester {
        boolean isLocationSolid(float x, float y);
    }

    public static float castRay(Vec2df origin, Vec2df direction, Vec2df intersection, Vec2di mapCheck, Tester tester) {

        float yDivideByX = direction.getY() / direction.getX();
        float xDivideByY = direction.getX() / direction.getY();
        Vec2df rayUnitStepSize = new Vec2df(
                (float) Math.sqrt(1 + yDivideByX * yDivideByX),
                (float) Math.sqrt(1 + xDivideByY * xDivideByY)
        );

        //Vec2di mapCheck = new Vec2di((int)origin.getX(), (int)origin.getY());
        mapCheck.setX((int)origin.getX());
        mapCheck.setX((int)origin.getY());
        Vec2df rayLength1D = new Vec2df();

        Vec2di step = new Vec2di();

        if ( direction.getX() < 0 ) {
            step.setX(-1);
            rayLength1D.setX((origin.getX() - (float)mapCheck.getX()) * rayUnitStepSize.getX());
        } else {
            step.setX(1);
            rayLength1D.setX(((float)(mapCheck.getX() + 1) - origin.getX()) * rayUnitStepSize.getX());
        }

        if ( direction.getY() < 0 ) {
            step.setY(-1);
            rayLength1D.setY((origin.getY() - (float)mapCheck.getY()) * rayUnitStepSize.getY());
        } else {
            step.setY(1);
            rayLength1D.setY(((float)(mapCheck.getY() + 1) - origin.getY()) * rayUnitStepSize.getY());
        }

        boolean tileFound = false;
        float distance = 0.0f;
        while ( !tileFound && distance < MAX_DISTANCE ) {
            // Walk
            if ( rayLength1D.getX() < rayLength1D.getY() ) {
                mapCheck.addToX(step.getX());
                distance = rayLength1D.getX();
                rayLength1D.addToX(rayUnitStepSize.getX());
            } else {
                mapCheck.addToY(step.getY());
                distance = rayLength1D.getY();
                rayLength1D.addToY(rayUnitStepSize.getY());
            }

            if ( tester.isLocationSolid(mapCheck.getX(), mapCheck.getY()) ) {
                tileFound = true;
            }
        }

        if ( tileFound ) {
            intersection.setX(origin.getX() + direction.getX() * distance);
            intersection.setY(origin.getY() + direction.getY() * distance);
        } else {
            intersection = null;
        }
        return distance;
    }

}
