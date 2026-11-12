package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.tao.ontology.Mutable;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Translate;

/** A grid on the floor under the graph, so up and down stay clear from any angle, orbiting or flying. */
final class Grid implements Mutable {

    private static final double STEP = 200;
    private static final int MAJOR_EVERY = 5;
    private static final double THICK = 2;

    private final Group node = new Group();
    private final PhongMaterial line = material("#262a31");
    private final PhongMaterial major = material("#323741");

    /** A grid over the floor of the grounds, a line every few hundred units and a stronger one every fifth. */
    Grid(final Grounds grounds) {
        int i = 0;
        for (double x = Math.floor(grounds.minX() / STEP) * STEP; x <= grounds.maxX(); x += STEP, i++) {
            add(new Box(THICK, 1, grounds.maxZ() - grounds.minZ()), x, grounds.floor(),
                    (grounds.minZ() + grounds.maxZ()) / 2, i);
        }
        i = 0;
        for (double z = Math.floor(grounds.minZ() / STEP) * STEP; z <= grounds.maxZ(); z += STEP, i++) {
            add(new Box(grounds.maxX() - grounds.minX(), 1, THICK), (grounds.minX() + grounds.maxX()) / 2,
                    grounds.floor(), z, i);
        }
        node.setMouseTransparent(true);
    }

    /** The grid's node in the scene. */
    Group node() {
        return node;
    }

    private void add(final Box box, final double x, final double y, final double z, final int index) {
        box.setMaterial(index % MAJOR_EVERY == 0 ? major : line);
        box.getTransforms().setAll(new Translate(x, y, z));
        node.getChildren().add(box);
    }

    private PhongMaterial material(final String colour) {
        final PhongMaterial material = new PhongMaterial(Color.web(colour));
        material.setSpecularColor(Color.TRANSPARENT);
        return material;
    }
}
