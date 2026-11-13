package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.homing.graphview.model.Edge;
import hue.captains.singapura.homing.graphview.model.Edges;
import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.tao.ontology.Mutable;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import java.util.Map;

/**
 * Where each edge is drawn, from where its line leaves the figure that depends to where it enters the one depended on,
 * so neither end is lost inside a figure. A pair of edges each way between two vertices shares one line, split in the
 * middle: each edge draws the half on the side of the vertex it depends on, so its head lands there, and in its own
 * state. Figures too close to leave a line between them are joined centre to centre.
 */
final class Spans implements Mutable {

    private final Edges edges;
    private final Map<Vertex, Point3D> centres;
    private final Map<Vertex, Bounds> boxes;

    /**
     * Spans for the edges.
     *
     * @param edges   the edges
     * @param centres where each vertex's figure is centred
     * @param boxes   the box each vertex's figure fills, where it is
     */
    Spans(final Edges edges, final Map<Vertex, Point3D> centres, final Map<Vertex, Bounds> boxes) {
        this.edges = edges;
        this.centres = Map.copyOf(centres);
        this.boxes = Map.copyOf(boxes);
    }

    /** Where an edge's drawing starts. */
    Point3D start(final Edge edge) {
        final Point3D leaving = leaving(edge);
        return edges.contains(new Edge(edge.to(), edge.from())) ? leaving.midpoint(entering(edge)) : leaving;
    }

    /** Where an edge's drawing ends, at the vertex it depends on. */
    Point3D end(final Edge edge) {
        return entering(edge);
    }

    /** Where the line leaves the figure that depends, or its centre if the figures leave no line between them. */
    private Point3D leaving(final Edge edge) {
        return apart(edge) ? exit(centre(edge.from()), centre(edge.to()), box(edge.from())) : centre(edge.from());
    }

    /** Where the line enters the figure depended on, or its centre if the figures leave no line between them. */
    private Point3D entering(final Edge edge) {
        return apart(edge) ? exit(centre(edge.to()), centre(edge.from()), box(edge.to())) : centre(edge.to());
    }

    /** Whether the figures leave some line between them. */
    private boolean apart(final Edge edge) {
        final Point3D from = centre(edge.from());
        final Point3D to = centre(edge.to());
        return from.distance(exit(from, to, box(edge.from()))) + to.distance(exit(to, from, box(edge.to())))
                < from.distance(to);
    }

    /** Where a line from a point inside a box toward another point leaves the box, or the point if it is outside. */
    private Point3D exit(final Point3D from, final Point3D toward, final Bounds box) {
        final double length = from.distance(toward);
        if (!(length > 0)) {
            return from;
        }
        final Point3D along = toward.subtract(from).multiply(1 / length);
        final double out = Math.min(Math.min(
                way(from.getX(), along.getX(), box.getMinX(), box.getMaxX()),
                way(from.getY(), along.getY(), box.getMinY(), box.getMaxY())),
                way(from.getZ(), along.getZ(), box.getMinZ(), box.getMaxZ()));
        return from.add(along.multiply(Math.clamp(out, 0, length)));
    }

    /** How far along one axis a line goes before it passes a side of the box that way. */
    private double way(final double at, final double along, final double least, final double most) {
        if (along > 0) {
            return (most - at) / along;
        }
        return along < 0 ? (least - at) / along : Double.POSITIVE_INFINITY;
    }

    private Point3D centre(final Vertex vertex) {
        return centres.get(vertex);
    }

    private Bounds box(final Vertex vertex) {
        return boxes.get(vertex);
    }
}
