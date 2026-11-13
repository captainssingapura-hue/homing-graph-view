package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.tao.ontology.Mutable;
import javafx.scene.Node;
import javafx.scene.shape.Shape3D;
import java.util.List;
import java.util.Objects;

/**
 * What a vertex is drawn as: a node centred on its own origin, facing the camera along -z and within its kind's room,
 * and the parts of it the view colours to show a selection. Those parts keep their own colour while nothing is
 * selected; a figure dimmed by a selection elsewhere has them darkened and the rest of it faded.
 */
public final class Figure implements Mutable {

    private final Node node;
    private final List<Shape3D> tinted;

    /**
     * A figure.
     *
     * @param node   the figure, centred on its origin
     * @param tinted the parts the view colours by the selection, such as a container's box or a frame's border
     */
    public Figure(final Node node, final List<Shape3D> tinted) {
        this.node = Objects.requireNonNull(node, "A figure needs its node");
        this.tinted = List.copyOf(tinted);
    }

    /** The figure's node. */
    public Node node() {
        return node;
    }

    /** The parts coloured by the selection. */
    public List<Shape3D> tinted() {
        return tinted;
    }
}
