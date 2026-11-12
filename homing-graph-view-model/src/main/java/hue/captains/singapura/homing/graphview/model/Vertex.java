package hue.captains.singapura.homing.graphview.model;

import hue.captains.singapura.tao.ontology.Scalar;
import java.util.Objects;

/**
 * A vertex is its identity and nothing else: an ID, unique within a graph, stable across loads of the same source, and
 * never blank. Whatever else is known about it lives outside the graph.
 *
 * @param id the vertex's identity
 */
public record Vertex(String id) implements Scalar {

    /** Refuses a missing or blank ID. */
    public Vertex {
        Objects.requireNonNull(id, "A vertex needs an ID");
        if (id.isBlank()) {
            throw new IllegalArgumentException("A vertex's ID is blank");
        }
    }
}
