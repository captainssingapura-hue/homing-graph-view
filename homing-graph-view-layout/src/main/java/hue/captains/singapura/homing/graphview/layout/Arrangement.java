package hue.captains.singapura.homing.graphview.layout;

import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.tao.ontology.Vector;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A layer laid out flat: the block it is arranged in, and where in it each of its vertices sits, in the layer's order,
 * each vertex once. Nothing here is geometry: whatever draws it decides what a slot measures.
 */
public final class Arrangement implements Vector<Seat> {

    private final Block block;
    private final List<Seat> items;
    private final Map<Vertex, Seat> index;
    private final int hash;

    /**
     * The seats given, in their order, in the block.
     *
     * @param block the block
     * @param items the seats, each inside the block, no vertex seated twice
     */
    public Arrangement(final Block block, final List<Seat> items) {
        this.block = Objects.requireNonNull(block, "An arrangement needs its block");
        this.items = List.copyOf(items);
        final double edge = (block.columns() - 1) / 2.0;
        final Map<Vertex, Seat> found = new HashMap<>();
        for (final Seat seat : this.items) {
            if (seat.slot().row() >= block.rows() || Math.abs(seat.slot().lane()) > edge) {
                throw new IllegalArgumentException("Vertex " + seat.vertex().id() + " sits in lane "
                        + seat.slot().lane() + ", row " + seat.slot().row() + ", outside a block of "
                        + block.columns() + " by " + block.rows());
            }
            if (found.putIfAbsent(seat.vertex(), seat) != null) {
                throw new IllegalArgumentException("Vertex " + seat.vertex().id() + " is seated twice");
            }
        }
        this.index = Map.copyOf(found);
        this.hash = Objects.hash(block, this.items);
    }

    /** The block the layer is arranged in. */
    public Block block() {
        return block;
    }

    /** The seats, in the layer's order; the list cannot be changed. */
    public List<Seat> items() {
        return items;
    }

    /** Where a vertex sits; empty for a vertex not in this layer. */
    public Optional<Seat> of(final Vertex vertex) {
        return Optional.ofNullable(index.get(vertex));
    }

    /** How many vertices sit in the block. */
    public int size() {
        return items.size();
    }

    /** How many rows the vertices fill: none for none. */
    public int rows() {
        return items.stream().mapToInt(seat -> seat.slot().row() + 1).max().orElse(0);
    }

    /** Equal to another arrangement with the same block and the same seats in the same order. */
    @Override
    public boolean equals(final Object other) {
        return other instanceof Arrangement that && hash == that.hash && block.equals(that.block)
                && items.equals(that.items);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        return "Arrangement" + block + items;
    }
}
