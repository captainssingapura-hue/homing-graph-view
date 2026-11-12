package hue.captains.singapura.homing.graphview.model;

import hue.captains.singapura.tao.ontology.Scalar;
import java.util.Objects;

/**
 * An edge is its link and nothing else: from the vertex that depends to the vertex it depends on, the way declared
 * dependencies point. Its two ends are its identity, so a pair of vertices has one edge at most in each direction. An
 * edge never goes from a vertex to itself.
 *
 * @param from the vertex that depends
 * @param to   the vertex depended on
 */
public record Edge(Vertex from, Vertex to) implements Scalar {

    /** Refuses a missing end, or an edge from a vertex to itself. */
    public Edge {
        Objects.requireNonNull(from, "An edge needs the vertex it is from");
        Objects.requireNonNull(to, "An edge needs the vertex it goes to");
        if (from.equals(to)) {
            throw new IllegalArgumentException("An edge goes from " + from.id() + " to itself");
        }
    }
}
