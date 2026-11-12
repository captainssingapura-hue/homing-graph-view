package hue.captains.singapura.homing.graphview.model;

import hue.captains.singapura.tao.ontology.Scalar;
import java.util.Objects;

/**
 * A graph is its vertices and its edges, and nothing else: the product of two vectors, valid whenever it exists. Both
 * ends of every edge are among its vertices. What a vertex or an edge means, and how it is drawn, lives outside it.
 *
 * @param vertices the graph's vertices, in the producer's order
 * @param edges    the graph's edges, in the producer's order
 */
public record Graph(Vertices vertices, Edges edges) implements Scalar {

    /** Refuses an edge with an end that is not one of the vertices. */
    public Graph {
        Objects.requireNonNull(vertices, "A graph needs its vertices");
        Objects.requireNonNull(edges, "A graph needs its edges");
        for (final Edge edge : edges.items()) {
            if (!vertices.contains(edge.from()) || !vertices.contains(edge.to())) {
                final Vertex unknown = vertices.contains(edge.from()) ? edge.to() : edge.from();
                throw new IllegalArgumentException("The edge from " + edge.from().id() + " to " + edge.to().id()
                        + " names " + unknown.id() + ", which is not a vertex of the graph");
            }
        }
    }
}
