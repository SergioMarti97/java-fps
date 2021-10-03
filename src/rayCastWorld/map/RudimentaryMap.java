package rayCastWorld.map;

import olcPGEApproach.gfx.HexColors;
import olcPGEApproach.gfx.Renderer;
import olcPGEApproach.vectors.points2d.Vec2df;
import olcPGEApproach.vectors.points2d.Vec2di;

/**
 * This class represents the most
 * primitive version of the map used
 * in the ray cast world.
 *
 * It is basically a String of characters
 * where each character represents something
 * and two dimensions: the width and the height
 */
public class RudimentaryMap {

    private String map;

    private Vec2di size;

    public RudimentaryMap(String map, int width, int height) {
        this.map = map;
        size = new Vec2di(width, height);
    }

    public RudimentaryMap(String map, Vec2di size) {
        this.map = map;
        this.size = size;
    }

    public void drawMiniMap(Renderer r, Vec2df playerPos, int blockSizeWidth, int blockSizeHeight, int offX, int offY) {
        for (int y = 0; y < size.getY(); y++) {
            for (int x = 0; x < size.getX(); x++) {
                r.drawFillRectangle(
                        x * blockSizeHeight + offX,
                        y * blockSizeHeight + offY,
                        blockSizeWidth,
                        blockSizeHeight,
                        getChar(x, y) == '#' ? HexColors.WHITE : HexColors.BLACK
                );
            }
        }
        r.drawFillCircle(
                (int)(playerPos.getX() * blockSizeWidth) + offX,
                (int)(playerPos.getY() * blockSizeHeight) + offY,
                blockSizeWidth / 2,
                HexColors.FANCY_RED
        );
    }

    // Getters and Setters

    public char getChar(int x, int y) {
        return map.toCharArray()[y * size.getX() + x];
    }

    public String getMap() {
        return map;
    }

    public Vec2di getSize() {
        return size;
    }

}
