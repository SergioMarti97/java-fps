package game;

import javafx.scene.image.Image;

/**
 * This class contains the drawing methods
 * Works with an array of pixels
 */
public class Renderer {

    /**
     * Array of pixels
     */
    private int[] p;

    /**
     * Canvas width
     */
    private int pW;

    /**
     * Canvas height
     */
    private int pH;

    /**
     * Constructor
     * @param p array of pixels
     * @param pW canvas width
     * @param pH canvas height
     */
    public Renderer(int[] p, int pW, int pH) {
        this.p = p;
        this.pW = pW;
        this.pH = pH;
    }

    public void drawImage(Image image, int offX, int offY) {
        if ( image == null ) {
            return;
        }

        // Don't render code
        if ( offX < -pW ) {
            return;
        }
        if ( offY < -pH ) {
            return;
        }
        if ( offX >= pW ) {
            return;
        }
        if ( offY >= pH ) {
            return;
        }

        int newX = 0;
        int newY = 0;
        int newWidth = (int)image.getWidth();
        int newHeight = (int)image.getHeight();

        // Clipping Code
        if ( offX < 0 ) {
            newX -= offX;
        }
        if ( offY < 0 ) {
            newY -= offY;
        }
        if ( newWidth + offX >= pW ) {
            newWidth -= (newWidth + offX - pW);
        }
        if ( newHeight + offY >= pH ) {
            newHeight -= (newHeight + offY - pH);
        }

        for (int y = newY; y < newHeight; y++) {
            for (int x = newX; x < newWidth; x++) {
                p[(x + offX) + pW * (y + offY)] = image.getPixelReader().getArgb(x, y);
            }
        }
    }

    public void setPixel(int x, int y, int color) {
        p[y * pW + x] = color;
    }

    // Getters

    public int[] getP() {
        return p;
    }

    public int getW() {
        return pW;
    }

    public int getH() {
        return pH;
    }

}
