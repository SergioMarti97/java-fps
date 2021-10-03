package rayCastWorld.physics.rectangles;

import olcPGEApproach.vectors.points2d.Vec2df;

public class RectangleMath {

    public static boolean pointVsRect(float x, float y, Rectangle r) {
        return x >= r.getPos().getX() && y >= r.getPos().getY() &&
                x < (r.getPos().getX() + r.getSize().getX()) && y < (r.getPos().getY() + r.getSize().getY());
    }

    public static boolean pointVsRect(Vec2df p, Rectangle r) {
        return pointVsRect(p.getX(), p.getY(), r);
    }

    public static boolean rectVsRect(Rectangle r1, Rectangle r2) {
        return (r1.getPos().getX() < (r2.getPos().getX() + r2.getSize().getX()) &&
                r1.getPos().getX() + r1.getSize().getX() > r2.getPos().getX() &&
                r1.getPos().getY() < (r2.getPos().getY() + r2.getSize().getY()) &&
                r1.getPos().getY() + r1.getSize().getY() > r2.getPos().getY());
    }

    /**
     * This method detects the collision between a ray and
     * a rectangle
     * @param ori the origin of the ray
     * @param dir the direction of the ray
     * @param tar the rectangle target
     * @param contactPoint the contact point of the collision
     * @param contactNormal the contact normal
     * @return true if the ray intersects with the rectangle, false if not
     */
    public static boolean rayVsRect(
            Vec2df ori,
            Vec2df dir,
            Rectangle tar,
            Vec2df contactPoint,
            Vec2df contactNormal) {
        // Ojo! Puede darse la división por 0

        // Cache division
        Vec2df invDir = new Vec2df(1.0f / dir.getX(), 1.0f / dir.getY());

        // Calculate intersections with rectangle bounding axes
        Vec2df near = new Vec2df(
                (tar.getPos().getX() - ori.getX()) * invDir.getX(),
                (tar.getPos().getY() - ori.getY()) * invDir.getY());
        Vec2df far = new Vec2df(
                (tar.getPos().getX() + tar.getSize().getX() - ori.getX()) * invDir.getX(),
                (tar.getPos().getY() + tar.getSize().getY() - ori.getY()) * invDir.getY());

        if (far.getX() == 0 || far.getY() == 0) {
            return false;
        }
        if (near.getX() == 0 || near.getY() == 0) {
            return false;
        }

        // Sort distances, swap
        if (near.getX() > far.getX()) {
            float temp = near.getX();
            near.setX(far.getX());
            far.setX(temp);
        }
        if (near.getY() > far.getY()) {
            float temp = near.getY();
            near.setY(far.getY());
            far.setY(temp);
        }

        if (near.getX() > far.getY() || near.getY() > far.getX()) {
            return false;
        }

        float hitNear = Math.max(near.getX(), near.getY());
        float hitFar = Math.min(far.getX(), far.getY());

        if (hitNear < 1) {
            return true;
        }

        if (hitFar < 0) {
            return false;
        }

        contactPoint.setX(ori.getX() + hitNear * dir.getX());
        contactPoint.setY(ori.getY() + hitNear * dir.getY());

        if (near.getX() > near.getY()) {
            if (invDir.getX() < 0) {
                contactNormal.setX(1);
            } else {
                contactNormal.setX(-1);
            }
            contactNormal.setY(0);
        } else if (near.getX() < near.getY()) {
            if (invDir.getY() < 0) {
                contactNormal.setY(1);
            } else {
                contactNormal.setY(-1);
            }
            contactNormal.setX(0);
        }

        return true;
    }

    public static float calContactTime(
            Vec2df ori,
            Vec2df dir,
            Rectangle tar) {
        // Cache division
        Vec2df invDir = new Vec2df(1.0f / dir.getX(), 1.0f / dir.getY());

        // Calculate intersections with rectangle bounding axes
        Vec2df near = new Vec2df(
                (tar.getPos().getX() - ori.getX()) * invDir.getX(),
                (tar.getPos().getY() - ori.getY()) * invDir.getY());
        Vec2df far = new Vec2df(
                (tar.getPos().getX() + tar.getSize().getX() - ori.getX()) * invDir.getX(),
                (tar.getPos().getY() + tar.getSize().getY() - ori.getY()) * invDir.getY());

        if (far.getX() == 0 || far.getY() == 0) {
            return Float.MAX_VALUE;
        }
        if (near.getX() == 0 || near.getY() == 0) {
            return Float.MAX_VALUE;
        }

        // Sort distances, swap
        if (near.getX() > far.getX()) {
            float temp = near.getX();
            near.setX(far.getX());
            far.setX(temp);
        }
        if (near.getY() > far.getY()) {
            float temp = near.getY();
            near.setY(far.getY());
            far.setY(temp);
        }

        if (near.getX() > far.getY() || near.getY() > far.getX()) {
            return Float.MAX_VALUE;
        }

        return Math.max(near.getX(), near.getY());
    }

    /**
     * This method detects a collision between two rectangles
     * @param in the first rectangle
     * @param tar the second rectangle
     * @param cp the contact point
     * @param cn the contact normal
     * @return true if the two rectangles collide
     */
    public static boolean dynamicRectVsRect(
            MovingRectangle in,
            MovingRectangle tar,
            Vec2df cp,
            Vec2df cn,
            float elapsedTime) {
        if (in.getVel().getX() == 0 && in.getVel().getY() == 0) {
            return false;
        }

        MovingRectangle expandedRect = new MovingRectangle(
                new Vec2df(
                        tar.getPos().getX() - in.getSize().getX() / 2,
                        tar.getPos().getY() - in.getSize().getY() / 2
                ),
                new Vec2df(
                        tar.getSize().getX() + in.getSize().getX(),
                        tar.getSize().getY() + in.getSize().getY()
                ),
                new Vec2df(in.getVel())
        );

        Vec2df ori = new Vec2df(
                in.getPos().getX() + in.getSize().getX() / 2,
                in.getPos().getY() + in.getSize().getY() / 2
        );
        Vec2df dir = new Vec2df(
                in.getVel().getX() * elapsedTime,
                in.getVel().getY() * elapsedTime
        );
        boolean collide = rayVsRect(
                ori,
                dir,
                expandedRect,
                cp,
                cn);
        if (collide) {
            float contactTime = calContactTime(ori, dir, expandedRect);
            return contactTime < 1.0f;
        } else {
            return false;
        }
    }

    public static float calContactTime(
            MovingRectangle in,
            MovingRectangle tar,
            Vec2df cp,
            Vec2df cn,
            float elapsedTime) {
        MovingRectangle expandedRect = new MovingRectangle(
                new Vec2df(
                        tar.getPos().getX() - in.getSize().getX() / 2,
                        tar.getPos().getY() - in.getSize().getY() / 2
                ),
                new Vec2df(
                        tar.getSize().getX() + in.getSize().getX(),
                        tar.getSize().getY() + in.getSize().getY()
                ),
                new Vec2df(in.getVel())
        );

        Vec2df ori = new Vec2df(
                in.getPos().getX() + in.getSize().getX() / 2,
                in.getPos().getY() + in.getSize().getY() / 2
        );
        Vec2df dir = new Vec2df(
                in.getVel().getX() * elapsedTime,
                in.getVel().getY() * elapsedTime
        );
        return calContactTime(ori, dir, expandedRect);
    }

}
