package hue.captains.singapura.homing.graphview.model;

import hue.captains.singapura.tao.ontology.Vector;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Vertices in order, each once: a graph's, or any set of them taken from one. The order is the producer's and is part of
 * the value, so the same vertices in another order are other Vertices. Whether a vertex is one of them is answered in
 * constant time.
 */
public final class Vertices implements Vector<Vertex> {

    private final List<Vertex> items;
    private final Set<Vertex> index;
    private final int hash;

    /**
     * The vertices given, in their order.
     *
     * @param items the vertices, none given twice
     */
    public Vertices(final List<Vertex> items) {
        this.items = List.copyOf(items);
        final Set<Vertex> seen = new HashSet<>();
        for (final Vertex vertex : this.items) {
            if (!seen.add(vertex)) {
                throw new IllegalArgumentException("Vertex " + vertex.id() + " is given twice");
            }
        }
        this.index = Set.copyOf(seen);
        this.hash = this.items.hashCode();
    }

    /** The vertices, in order; the list cannot be changed. */
    public List<Vertex> items() {
        return items;
    }

    /** Whether the vertex is one of these. */
    public boolean contains(final Vertex vertex) {
        return index.contains(vertex);
    }

    /** How many vertices there are. */
    public int size() {
        return items.size();
    }

    /** Equal to other Vertices with the same vertices in the same order. */
    @Override
    public boolean equals(final Object other) {
        return other instanceof Vertices that && hash == that.hash && items.equals(that.items);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        return "Vertices" + items;
    }
}
