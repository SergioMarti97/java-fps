package game.gfx;

import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritablePixelFormat;

import java.nio.IntBuffer;

public class Image {

    /**
     * The image width
     */
    protected int w;

    /**
     * The image height
     */
    protected int h;

    /**
     * The image pixel array
     */
    protected int[] p;

    /**
     * The null constructor
     */
    public Image() {
        w = 0;
        h = 0;
        p = new int[w * h];
    }

    /**
     * The constructor
     *
     * @param p the pixel array what conforms the image
     * @param w width
     * @param h height
     */
    public Image(int[] p, int w, int h) {
        this.p = p;
        this.w = w;
        this.h = h;
    }

    /**
     * The constructor
     *
     * @param w width
     * @param h height
     */
    public Image(int w, int h) {
        this.w = w;
        this.h = h;
        p = new int[w * h];
    }

    /**
     * Constructor
     *
     * @param path the path where is the image
     */
    public Image(String path) {
        javafx.scene.image.Image img = new javafx.scene.image.Image(path);
        w = (int)img.getWidth();
        h = (int)img.getWidth();
        p = getPixels(img, 0, 0, w, h);
    }

    public int[] getPixels(javafx.scene.image.Image img, int x, int y, int w, int h) {
        int[] pixels = new int[w * h];

        PixelReader reader = img.getPixelReader();
        PixelFormat.Type type =  reader.getPixelFormat().getType();
        WritablePixelFormat<IntBuffer> format;

        if (type == PixelFormat.Type.INT_ARGB_PRE) {
            format = PixelFormat.getIntArgbPreInstance();
        } else {
            format = PixelFormat.getIntArgbInstance();
        }

        reader.getPixels(x, y, w, h, format, pixels, 0, w);

        return pixels;
    }

    // Get Pixel & Get Sample

    /**
     * This method returns an pixel for the image
     *
     * @param x the X position of the pixel inside the image
     * @param y the Y position of the pixel inside the image
     * @return a pixel for the image
     * @throws ArrayIndexOutOfBoundsException if the x and/or y positions are outside of the image
     */
    public int getPixel(int x, int y) throws ArrayIndexOutOfBoundsException {
        int index = x + w * y;
        assert p != null;
        if ( index < p.length ) {
            return p[x + w * y];
        } else {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    /**
     * This method returns a sample (pixel) of the image
     * @param x the X position of the pixel inside the image
     * @param y the Y position of the pixel inside the image
     * @return a pixel of the image
     * @throws ArrayIndexOutOfBoundsException if the x and/or y positions are outside of the image
     */
    public int getSample(float x, float y) {
        int color;
        int sampleX = Math.min((int)(x * (float)w), w > 0 ? w - 1 : w);
        int sampleY = Math.min((int)(y * (float)h), h > 0 ? h - 1 : h);
        try {
            color = getPixel(sampleX, sampleY);
        } catch ( ArrayIndexOutOfBoundsException e ) {
            color = 0x00000000;
            String errorMessage = "X: " + x + " Y: " + y + " outside of " + getW() + "x" + getH();
            System.out.println("Get sample Error: " + errorMessage + e.getMessage());
        }
        return color;
    }

    // Getters & Setters

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }

    public int[] getP() {
        return p;
    }

    public void setW(int w) {
        this.w = w;
    }

    public void setH(int h) {
        this.h = h;
    }

    public void setP(int[] p) {
        this.p = p;
    }

}
