package physics.rectangles;

import javafx.scene.input.MouseButton;
import olcPGEApproach.AbstractGame;
import olcPGEApproach.GameContainer;
import olcPGEApproach.gfx.HexColors;
import olcPGEApproach.gfx.Renderer;
import olcPGEApproach.vectors.points2d.Vec2df;
import rayCastWorld.physics.rectangles.MovingRectangle;
import rayCastWorld.physics.rectangles.Rectangle;
import rayCastWorld.physics.rectangles.RectangleMath;

import java.util.ArrayList;

public class PhysicsTestGame implements AbstractGame {

    private Vec2df rayOri;

    private Vec2df rayDir;

    private ArrayList<MovingRectangle> rectangles;

    @Override
    public void initialize(GameContainer gc) {
        rectangles = new ArrayList<>();
        rectangles.add(new MovingRectangle(new Vec2df(10, 10), new Vec2df(30, 20)));
        rectangles.add(new MovingRectangle(new Vec2df(100, 100), new Vec2df(80, 50)));

        rayOri = rectangles.get(0).getPos();
        rayDir = new Vec2df(
                (float) gc.getInput().getMouseX() - rayOri.getX(),
                (float) gc.getInput().getMouseY() - rayOri.getY());
    }

    @Override
    public void update(GameContainer gc, float elapsedTime) {
        rayOri = rectangles.get(0).getPos();
        rayDir.setX((float) gc.getInput().getMouseX() - rayOri.getX());
        rayDir.setY((float) gc.getInput().getMouseY() - rayOri.getY());

        if (gc.getInput().isButtonHeld(MouseButton.PRIMARY)) {
            rayDir.normalize();
            rectangles.get(0).getVel().addToX(rayDir.getX() * 100.0f * elapsedTime);
            rectangles.get(0).getVel().addToY(rayDir.getY() * 100.0f * elapsedTime);
        }

        for ( MovingRectangle r : rectangles ) {
            r.update(elapsedTime);
        }

        Vec2df cp;
        Vec2df cn;
        for (int i = 1; i < rectangles.size(); i++) {
            if (RectangleMath.dynamicRectVsRect(
                    rectangles.get(0),
                    rectangles.get(i),
                    cp = new Vec2df(),
                    cn = new Vec2df(),
                    elapsedTime)) {
                float ct = RectangleMath.calContactTime(
                        rectangles.get(0),
                        rectangles.get(i),
                        cp,
                        cn,
                        elapsedTime);
                rectangles.get(0).getVel().addToX(cn.getX() * Math.abs(rectangles.get(0).getVel().getX()) * (1 - ct));
                rectangles.get(0).getVel().addToY(cn.getY() * Math.abs(rectangles.get(0).getVel().getY()) * (1 - ct));
            }
        }
    }

    private void renderRectangle(Renderer r, Rectangle rect, int color) {
        r.drawRectangle(
                (int) rect.getPos().getX(),
                (int) rect.getPos().getY(),
                (int) rect.getSize().getX(),
                (int) rect.getSize().getY(), color);
    }

    private void renderFillRectangle(Renderer r, Rectangle rect, int color) {
        r.drawFillRectangle(
                (int) rect.getPos().getX(),
                (int) rect.getPos().getY(),
                (int) rect.getSize().getX(),
                (int) rect.getSize().getY(), color);
    }

    @Override
    public void render(GameContainer gc) {
        gc.getRenderer().clear(HexColors.DARK_BLUE);

        for ( Rectangle r : rectangles ) {
            renderRectangle(gc.getRenderer(), r, HexColors.WHITE);
        }

        /*gc.getRenderer().drawLine(
                (int) rayOri.getX(), (int) rayOri.getY(),
                (int) gc.getInput().getMouseX(), (int) gc.getInput().getMouseY(),
                HexColors.GREEN);

        Vec2df cp;
        Vec2df cn;
        boolean collision = RectangleMath.rayVsRect(
                rayOri,
                rayDir,
                rect1,
                cp = new Vec2df(),
                cn = new Vec2df());

        int color;
        if (collision) {
            color = HexColors.YELLOW;
            renderFillRectangle(gc.getRenderer(), rect1, color);

            gc.getRenderer().drawFillCircle((int)cp.getX(), (int)cp.getY(), 3, HexColors.RED);
            gc.getRenderer().drawLine(
                    (int)cp.getX(), (int)cp.getY(),
                    (int)(cp.getX() + cn.getX() * 10), (int)(cp.getY() + cn.getY() * 10),
                    HexColors.YELLOW);
        } else {
            color = HexColors.WHITE;
            renderRectangle(gc.getRenderer(), rect1, color);
        }*/
    }

}
