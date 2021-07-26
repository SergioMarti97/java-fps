package orthographicViewer;

import olcPGEApproach.gfx.images.Image;
import olcPGEApproach.vectors.points3d.Vec3df;

public class Quad {

    private final int NUM_POINTS = 4;

    private Vec3df[] points;

    private Image img;

    public Quad(Vec3df[] points, Image img) {
        this.points = points;
        this.img = img;
    }

    public Vec3df[] getPoints() {
        return points;
    }

    public void setPoints(Vec3df[] points) {
        this.points = points;
    }

    public Image getImg() {
        return img;
    }

    public void setImg(Image img) {
        this.img = img;
    }

}
