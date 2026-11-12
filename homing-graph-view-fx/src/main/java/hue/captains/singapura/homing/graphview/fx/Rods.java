package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.homing.graphview.layout.Highlight;
import hue.captains.singapura.homing.graphview.model.Edge;
import hue.captains.singapura.homing.graphview.model.Edges;
import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.tao.ontology.Mutable;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The edges drawn: one rod for each, from the centre of the slate that depends to the centre of the one depended on.
 * Rods take no clicks, so a click on one reaches what is behind it.
 */
final class Rods implements Mutable {

    private static final double RADIUS = 1.8;
    private static final int SIDES = 8;

    private final Group node = new Group();
    private final Map<Edge, PhongMaterial> materials = new LinkedHashMap<>();

    /**
     * Rods for the edges.
     *
     * @param edges   the edges
     * @param centres where each end's slate is centred
     */
    Rods(final Edges edges, final Map<Vertex, Point3D> centres) {
        for (final Edge edge : edges.items()) {
            final PhongMaterial material = new PhongMaterial();
            materials.put(edge, material);
            node.getChildren().add(rod(centres.get(edge.from()), centres.get(edge.to()), material));
        }
        node.setMouseTransparent(true);
    }

    /** The rods' node in the scene. */
    Group node() {
        return node;
    }

    /** Draws every rod in the state the selection puts its edge in. */
    void paint(final Optional<Highlight> selection, final States states, final Palette palette) {
        materials.forEach((edge, material) -> material.setDiffuseColor(palette.edge(states.of(selection, edge))));
    }

    /** A cylinder from one point to another: JavaFX's lies along y, so it is turned onto the line between them. */
    private Cylinder rod(final Point3D from, final Point3D to, final PhongMaterial material) {
        final Point3D along = to.subtract(from);
        final Cylinder rod = new Cylinder(RADIUS, along.magnitude(), SIDES);
        final Point3D axis = Rotate.Y_AXIS.crossProduct(along);
        final double angle = Rotate.Y_AXIS.angle(along);
        final Point3D middle = from.midpoint(to);
        rod.getTransforms().setAll(new Translate(middle.getX(), middle.getY(), middle.getZ()),
                new Rotate(angle, axis.magnitude() > 0 ? axis : Rotate.X_AXIS));
        rod.setMaterial(material);
        return rod;
    }
}
