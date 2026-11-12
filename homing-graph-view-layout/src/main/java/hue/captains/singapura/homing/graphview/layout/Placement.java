package hue.captains.singapura.homing.graphview.layout;

import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.tao.ontology.Vector;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** A graph laid out: where each of its vertices is placed, in the graph's order, each vertex once. */
public final class Placement implements Vector<Placed> {

    private final List<Placed> items;
    private final Map<Vertex, Placed> index;
    private final int maxDepth;
    private final Map<Integer, Integer> rows;
    private final int hash;

    /**
     * The places given, in their order.
     *
     * @param items the places, no vertex placed twice
     */
    public Placement(final List<Placed> items) {
        this.items = List.copyOf(items);
        final Map<Vertex, Placed> found = new HashMap<>();
        final Map<Integer, Integer> filled = new HashMap<>();
        for (final Placed placed : this.items) {
            if (found.putIfAbsent(placed.vertex(), placed) != null) {
                throw new IllegalArgumentException("Vertex " + placed.vertex().id() + " is placed twice");
            }
            filled.merge(placed.depth(), placed.row() + 1, Math::max);
        }
        this.index = Map.copyOf(found);
        this.rows = Map.copyOf(filled);
        this.maxDepth = this.items.stream().mapToInt(Placed::depth).max().orElse(0);
        this.hash = this.items.hashCode();
    }

    /** The places, in order; the list cannot be changed. */
    public List<Placed> items() {
        return items;
    }

    /** Where a vertex is placed; empty for a vertex this placement does not hold. */
    public Optional<Placed> of(final Vertex vertex) {
        return Optional.ofNullable(index.get(vertex));
    }

    /** The deepest layer, 0 when there is none. */
    public int maxDepth() {
        return maxDepth;
    }

    /** How many rows a layer fills: one more than its highest row, none for a depth with no vertices. */
    public int rowsOf(final int depth) {
        return rows.getOrDefault(depth, 0);
    }

    /** How many vertices are placed. */
    public int size() {
        return items.size();
    }

    /** Equal to another placement with the same places in the same order. */
    @Override
    public boolean equals(final Object other) {
        return other instanceof Placement that && hash == that.hash && items.equals(that.items);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        return "Placement" + items;
    }
}
