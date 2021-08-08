package rayCastWorld.renderer;

/**
 * This class contains some of the code
 * needed for make shadows in the ray cast
 * world
 */
public class RayShadowing {

    /**
     * This method shadows a pixel given the percentage
     * (0 - 1) of shadow
     * @param pixel the pixel to change the color
     * @param shadow 0 from black and 1 for original color
     * @return shadowed pixel
     */
    public static int shadowPixel(int pixel, float shadow) {
        int a = pixel >> 24 & 0xff;
        int r = pixel >> 16 & 0xff;
        int g = pixel >> 8 & 0xff;
        int b = pixel & 0xff;
        return (a << 24 | (int) (r * shadow) << 16 | (int) (g * shadow) << 8 | (int) (b * shadow));
    }

    /**
     * This method calculates the shadow percentage,
     * between 0 to black and 1 to original color
     * @param depth the depth where it is
     * @param maxDepth the maximum depth, where it is full shadow
     * @return given percentage between 0 for full shadow or 1 for original color
     */
    public static float calShadow(float depth, float maxDepth) {
        return 1.0f - Math.min(depth / maxDepth, 1.0f);
    }

    /**
     * This method shadows a pixel given the depth
     * where is the pixel and the max depth
     * max depth will be black
     * @param pixel the color to change
     * @param depth the depth of the pixel
     * @param maxDepth the maximum depth, where the pixel will be black
     * @return shadowed pixel
     */
    public static int shadowPixel(int pixel, float depth, float maxDepth) {
        return shadowPixel(pixel, calShadow(depth, maxDepth));
    }

}
