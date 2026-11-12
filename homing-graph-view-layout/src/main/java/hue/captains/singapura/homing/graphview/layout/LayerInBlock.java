package hue.captains.singapura.homing.graphview.layout;

import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.homing.graphview.model.Vertices;
import hue.captains.singapura.tao.ontology.FunctionalObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Lays one layer out flat, in 2D: its vertices in a block, as a list fills an array. Which vertices make the layer is
 * settled before it is handed over, by {@link Layers} or otherwise.
 *
 * <p>The block is as wide as the smallest size on the ladder that holds the layer, and as tall as the rows the layer
 * fills. The layer fills it in its own order, a row at a time from the top left, its rows standing on the bottom; a
 * last row that is not full is centred.
 *
 * @param ladder the sizes a block comes in
 */
public record LayerInBlock(Ladder ladder) implements FunctionalObject {

    /** Refuses a missing ladder. */
    public LayerInBlock {
        Objects.requireNonNull(ladder, "A layer in a block needs the ladder of sizes a block comes in");
    }

    /**
     * Where each vertex of the layer sits, and the block it sits in.
     *
     * @param layer the layer's vertices, in the order they fill the block
     */
    public Arrangement of(final Vertices layer) {
        final List<Vertex> vertices = layer.items();
        final Block size = ladder.holding(vertices.size());
        final Block block = new Block(size.columns(), Math.max(1, size.rowsFor(vertices.size())));
        final List<Seat> seats = new ArrayList<>();
        for (int i = 0; i < vertices.size(); i++) {
            seats.add(new Seat(vertices.get(i), new Fill().of(i, vertices.size(), block)));
        }
        return new Arrangement(block, seats);
    }
}
