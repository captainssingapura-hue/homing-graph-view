package hue.captains.singapura.homing.graphview.model;

import hue.captains.singapura.tao.ontology.Vector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Edges in order, each once: a graph's, or any set of them taken from one. An edge is its pair of ends, so two edges
 * between the same ordered pair are one edge given twice. The order is part of the value. The edges from a vertex, and
 * those to it, are found in constant time.
 */
public final class Edges implements Vector<Edge> {

    private final List<Edge> items;
    private final Set<Edge> index;
    private final Map<Vertex, List<Edge>> outgoing;
    private final Map<Vertex, List<Edge>> incoming;
    private final int hash;

    /**
     * The edges given, in their order.
     *
     * @param items the edges, no ordered pair of ends given twice
     */
    public Edges(final List<Edge> items) {
        this.items = List.copyOf(items);
        final Set<Edge> seen = new HashSet<>();
        final Map<Vertex, List<Edge>> out = new HashMap<>();
        final Map<Vertex, List<Edge>> in = new HashMap<>();
        for (final Edge edge : this.items) {
            if (!seen.add(edge)) {
                throw new IllegalArgumentException(
                        "The edge from " + edge.from().id() + " to " + edge.to().id() + " is given twice");
            }
            out.computeIfAbsent(edge.from(), vertex -> new ArrayList<>()).add(edge);
            in.computeIfAbsent(edge.to(), vertex -> new ArrayList<>()).add(edge);
        }
        this.index = Set.copyOf(seen);
        this.outgoing = frozen(out);
        this.incoming = frozen(in);
        this.hash = this.items.hashCode();
    }

    /** The edges, in order; the list cannot be changed. */
    public List<Edge> items() {
        return items;
    }

    /** Whether the edge is one of these. */
    public boolean contains(final Edge edge) {
        return index.contains(edge);
    }

    /** How many edges there are. */
    public int size() {
        return items.size();
    }

    /** The edges from a vertex - what it depends on - in order; none for a vertex with no such edge. */
    public Edges from(final Vertex vertex) {
        return new Edges(outgoing.getOrDefault(vertex, List.of()));
    }

    /** The edges to a vertex - what depends on it - in order; none for a vertex with no such edge. */
    public Edges to(final Vertex vertex) {
        return new Edges(incoming.getOrDefault(vertex, List.of()));
    }

    /** Equal to other Edges with the same edges in the same order. */
    @Override
    public boolean equals(final Object other) {
        return other instanceof Edges that && hash == that.hash && items.equals(that.items);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        return "Edges" + items;
    }

    private Map<Vertex, List<Edge>> frozen(final Map<Vertex, List<Edge>> lists) {
        return lists.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, entry -> List.copyOf(entry.getValue())));
    }
}
