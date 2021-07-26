package orthographicViewer;

import javafx.scene.input.KeyCode;
import olcPGEApproach.AbstractGame;
import olcPGEApproach.GameContainer;
import olcPGEApproach.gfx.images.Image;
import olcPGEApproach.gfx.images.ImageTile;
import olcPGEApproach.vectors.points2d.Vec2di;
import olcPGEApproach.vectors.points3d.Vec3df;
import rayCastWorld.CellSide;

import java.util.ArrayList;
import java.util.Arrays;

public class OrthographicViewer implements AbstractGame {

    private World world;

    private Vec3df cameraPos;

    private float cameraAngle;

    private float cameraPitch;

    private float cameraZoom;

    private Vec2di cursor;

    @Override
    public void initialize(GameContainer gc) {
        ImageTile imgTile = new ImageTile("/dungeon/dg_features32.png", 32, 32);

        world = new World(64, 64);

        for ( int y = 0; y < world.getSize().getY(); y++ ) {
            for ( int x = 0; x < world.getSize().getX(); x++ ) {
                world.getCell(x, y).setWall(false);
                world.getCell(x, y).getFaces().put(CellSide.BOTTOM, imgTile.getTileImage(1, 0));
                world.getCell(x, y).getFaces().put(CellSide.TOP, imgTile.getTileImage(4, 9));
                world.getCell(x, y).getFaces().put(CellSide.NORTH, imgTile.getTileImage(3, 0));
                world.getCell(x, y).getFaces().put(CellSide.SOUTH, imgTile.getTileImage(3, 0));
                world.getCell(x, y).getFaces().put(CellSide.EAST, imgTile.getTileImage(3, 0));
                world.getCell(x, y).getFaces().put(CellSide.WEST, imgTile.getTileImage(3, 0));
            }
        }

        cameraPos = new Vec3df();
        cameraAngle = 0.0f;
        cameraPitch = 5.5f;
        cameraZoom = 16.0f;

        cursor = new Vec2di();
    }

    private Vec3df[] createCube(
            Vec2di cellPos,
            float angle,
            float pitch,
            float scale,
            Vec3df camera,
            int w,
            int h) {
        final int numVertex = 8;
        // Unit cube
        Vec3df[] unitCube, rotCube, worldCube, projCube;

        unitCube = new Vec3df[numVertex];
        unitCube[0] = new Vec3df(0.0f, 0.0f, 0.0f);
        unitCube[1] = new Vec3df(scale, 0.0f, 0.0f);
        unitCube[2] = new Vec3df(scale, -scale, 0.0f);
        unitCube[3] = new Vec3df(0.0f, -scale, 0.0f);
        unitCube[4] = new Vec3df(0.0f, 0.0f, scale);
        unitCube[5] = new Vec3df(scale, 0.0f, scale);
        unitCube[6] = new Vec3df(scale, -scale, scale);
        unitCube[7] = new Vec3df(0.0f, -scale, scale);

        // Translate the cube in X-Z Plane
        for ( int i = 0; i < numVertex; i++ ) {
            unitCube[i].addToX(cellPos.getX() * scale - camera.getX());
            unitCube[i].addToY(- camera.getX());
            unitCube[i].addToZ(cellPos.getY() * scale - camera.getZ());
        }

        // Rotate cube in Y-Axis around the origin
        rotCube = new Vec3df[numVertex];
        float s = (float)Math.sin(angle);
        float c = (float)Math.cos(angle);
        for ( int i = 0; i < numVertex; i++ ) {
            rotCube[i] = new Vec3df();
            rotCube[i].setX(unitCube[i].getX() * c + unitCube[i].getZ() * s);
            rotCube[i].setY(unitCube[i].getY());
            rotCube[i].setZ(unitCube[i].getX() * -s + unitCube[i].getZ() * c);
        }

        worldCube = new Vec3df[numVertex];
        s = (float)Math.sin(pitch);
        c = (float)Math.cos(pitch);
        for ( int i = 0; i < numVertex; i++ ) {
            worldCube[i] = new Vec3df();
            worldCube[i].setX(rotCube[i].getX());
            worldCube[i].setY(rotCube[i].getY() * c - rotCube[i].getZ() * s);
            worldCube[i].setZ(rotCube[i].getY() * s - rotCube[i].getZ() * c);
        }

        projCube = new Vec3df[numVertex];
        for ( int i = 0; i < numVertex; i++ ) {
            projCube[i] = new Vec3df();
            projCube[i].setX(worldCube[i].getX() + w * 0.5f);
            projCube[i].setY(worldCube[i].getY() + h * 0.5f);
            projCube[i].setZ(worldCube[i].getZ());
        }

        return projCube;
    }

    private void makeFace(
            int v1,
            int v2,
            int v3,
            int v4,
            Vec3df[] projCube,
            Cell cell,
            CellSide side,
            ArrayList<Quad> render
    ) {
        Vec3df[] points = new Vec3df[] { projCube[v1], projCube[v2], projCube[v3], projCube[v4]};
        render.add(new Quad(points, cell.getFaces().get(side)));
    }

    private void getFaceQuad(
            Vec2di cellPos,
            float angle,
            float pitch,
            float scale,
            Vec3df camera,
            ArrayList<Quad> render,
            int w, int h) {

        Vec3df[] projCube = createCube(cellPos, angle, pitch, scale, camera, w, h);
        Cell cell = world.getCell(cellPos);

        if (!cell.isWall()) {
            makeFace(4, 0, 1, 5, projCube, cell, CellSide.BOTTOM, render);
        } else {
            makeFace(3, 0, 1, 2, projCube, cell, CellSide.SOUTH, render);
            makeFace(6, 5, 4, 7, projCube, cell, CellSide.NORTH, render);
            makeFace(7, 4, 0, 3, projCube, cell, CellSide.EAST, render);
            makeFace(2, 1, 5, 6, projCube, cell, CellSide.WEST, render);
            makeFace(7, 3, 2, 6, projCube, cell, CellSide.TOP, render);
        }

    }

    @Override
    public void update(GameContainer gc, float elapsedTime) {
        if ( gc.getInput().isKeyHeld(KeyCode.W) ) {
            cameraPitch += elapsedTime;
        }
        if ( gc.getInput().isKeyHeld(KeyCode.S) ) {
            cameraPitch -= elapsedTime;
        }

        if ( gc.getInput().isKeyHeld(KeyCode.D) ) {
            cameraAngle += elapsedTime;
        }
        if ( gc.getInput().isKeyHeld(KeyCode.A) ) {
            cameraAngle -= elapsedTime;
        }

        if ( gc.getInput().isKeyHeld(KeyCode.Q) ) {
            cameraZoom += 5.0f * elapsedTime;
        }
        if ( gc.getInput().isKeyHeld(KeyCode.Z) ) {
            cameraZoom -= 5.0f * elapsedTime;
        }

        if ( gc.getInput().isKeyHeld(KeyCode.LEFT) ) {
            System.out.println("Has apretado la izquierda");
            cursor.addToX(1);
        }
        if ( gc.getInput().isKeyHeld(KeyCode.RIGHT) ) {
            System.out.println("Has apretado la derecha");
            cursor.addToX(-1);
        }
        if ( gc.getInput().isKeyHeld(KeyCode.UP) ) {
            System.out.println("Has apretado arriba");
            cursor.addToY(1);
        }
        if ( gc.getInput().isKeyHeld(KeyCode.DOWN) ) {
            System.out.println("Has apretado abajo");
            cursor.addToY(-1);
        }
        if ( cursor.getX() < 0 ) {
            cursor.setX(0);
        }
        if ( cursor.getY() < 0 ) {
            cursor.setY(0);
        }
        if ( cursor.getX() >= world.getSize().getX() ) {
            cursor.setX(world.getSize().getX() - 1);
        }
        if ( cursor.getY() >= world.getSize().getY() ) {
            cursor.setY(world.getSize().getY() - 1);
        }

        cameraPos.setX(cursor.getX() + 0.5f);
        cameraPos.setY(cursor.getY() + 0.5f);
        cameraPos.setX(cameraPos.getX() * cameraZoom);
        cameraPos.setY(cameraPos.getY() * cameraZoom);

        if ( gc.getInput().isKeyDown(KeyCode.SPACE) ) {
            world.getCell(cursor).setWall(!world.getCell(cursor).isWall());
        }
    }

    @Override
    public void render(GameContainer gc) {

        ArrayList<Quad> quads = new ArrayList<>();

        for ( int y = 0; y < world.getSize().getY(); y++ ) {
            for ( int x = 0; x < world.getSize().getX(); x++ ) {
                getFaceQuad(
                        new Vec2di(x, y),
                        cameraAngle,
                        cameraPitch,
                        cameraZoom,
                        cameraPos,
                        quads,
                        gc.getRenderer().getW(),
                        gc.getRenderer().getH());
            }
        }

        Arrays.fill(gc.getRenderer().getP(), 0xff000000);

        for ( Quad q : quads ) {
            gc.getRenderer().drawImage(q.getImg(), (int)q.getPoints()[0].getX(), (int)q.getPoints()[0].getY());
        }

        gc.getRenderer().drawImage(new Image("/dungeon/dg_features32.png"), 10, 10);

    }

}
