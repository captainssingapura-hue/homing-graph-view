package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.tao.ontology.Mutable;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Translate;
import java.util.ArrayList;
import java.util.List;

/**
 * A vertex's figure set in its slot: the view's hold on it, to place it, to find its vertex from a click, which reads
 * the vertex from the node's user data, and to show its state. Nothing selected, its tinted parts keep their own
 * colours; otherwise they take the state's colour, and a dimmed figure has its flat parts, its pictures and its words,
 * darkened and greyed too. Nothing is made see-through: a scene in depth draws see-through parts out of order.
 */
final class Slate implements Mutable {

    /** How a dimmed figure's flat parts are greyed and darkened. */
    private static final double GREYED = -0.7;
    private static final double DARKENED = -0.55;

    private final Group node;
    private final List<PhongMaterial> tinted;
    private final List<Color> own;
    private final List<Node> flat = new ArrayList<>();

    /**
     * The figure for the vertex, centred on the point.
     *
     * @param vertex the vertex
     * @param centre where its centre is in the scene
     * @param figure what it is drawn as
     */
    Slate(final Vertex vertex, final Point3D centre, final Figure figure) {
        node = new Group(figure.node());
        node.setUserData(vertex);
        node.getTransforms().setAll(new Translate(centre.getX(), centre.getY(), centre.getZ()));
        tinted = figure.tinted().stream().map(this::material).toList();
        own = tinted.stream().map(PhongMaterial::getDiffuseColor).toList();
        collect(figure.node());
    }

    /** The figure's node in the scene. */
    Group node() {
        return node;
    }

    /** The box the figure fills, where it is in the scene. */
    Bounds box() {
        return node.getBoundsInParent();
    }

    /** Draws the figure in a state. */
    void paint(final NodeState state, final Palette palette) {
        for (int i = 0; i < tinted.size(); i++) {
            final Color colour = state instanceof NodeState.Idle ? own.get(i) : palette.slate(state);
            tinted.get(i).setDiffuseColor(colour);
        }
        flat.forEach(part -> part.setEffect(state instanceof NodeState.Dim ? new ColorAdjust(0, GREYED, DARKENED, 0)
                : null));
    }

    /** The figure's flat parts, all but its solids and the groups that hold them. */
    private void collect(final Node part) {
        if (part instanceof Group group) {
            group.getChildren().forEach(this::collect);
        } else if (!(part instanceof Shape3D)) {
            flat.add(part);
        }
    }

    /** A part's material, given one of its own if it has none the view can colour. */
    private PhongMaterial material(final Shape3D part) {
        if (part.getMaterial() instanceof PhongMaterial phong) {
            return phong;
        }
        final PhongMaterial phong = new PhongMaterial(Color.GRAY);
        part.setMaterial(phong);
        return phong;
    }
}
