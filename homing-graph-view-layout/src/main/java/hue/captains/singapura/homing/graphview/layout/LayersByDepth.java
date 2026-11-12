package hue.captains.singapura.homing.graphview.layout;

import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.homing.graphview.model.Vertices;
import hue.captains.singapura.tao.ontology.FunctionalObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Lays a graph out in layers by depth, each layer flat in a block of its own: {@link Layers} splits the graph, and
 * {@link LayerInBlock} arranges each layer, its block as wide as the ladder says and as tall as the layer fills. A
 * vertex's lane and row are its slot in its layer's block: lanes centred on 0, rows from 0 at the bottom.
 *
 * @param ladder the sizes a layer's block comes in
 */
public record LayersByDepth(Ladder ladder) implements FunctionalObject {

    /** Refuses a missing ladder. */
    public LayersByDepth {
        Objects.requireNonNull(ladder, "Layers by depth need the ladder of sizes a layer's block comes in");
    }

    /**
     * Where each vertex of the graph is placed, in the graph's order.
     *
     * @param graph the graph, cycles allowed: vertices on a cycle share a layer
     */
    public Placement of(final Graph graph) {
        final LayerInBlock inBlock = new LayerInBlock(ladder);
        final List<Vertices> layers = new Layers().of(graph);
        final Map<Vertex, Placed> placed = new HashMap<>();
        for (int depth = 0; depth < layers.size(); depth++) {
            for (final Seat seat : inBlock.of(layers.get(depth)).items()) {
                placed.put(seat.vertex(), new Placed(seat.vertex(), depth, seat.slot().lane(), seat.slot().row()));
            }
        }
        return new Placement(graph.vertices().items().stream().map(placed::get).toList());
    }
}
