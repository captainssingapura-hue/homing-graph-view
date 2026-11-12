package hue.captains.singapura.homing.graphview.layout;

import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lays a graph out in layers by depth. Without groups, every vertex is a group of its own: it has a lane to itself in
 * its layer and sits in row 0. The vertices of a layer take lanes in the graph's order, centred on 0, at
 * {@code i - (n - 1) / 2}.
 */
public record LayersByDepth() implements StatelessFunctionalObject {

    /**
     * Where each vertex of the graph is placed.
     *
     * @param graph the graph, cycles allowed: vertices on a cycle share a layer
     */
    public Placement of(final Graph graph) {
        final Map<Vertex, Placed> placed = new HashMap<>();
        final List<List<Vertex>> layers = new Layers().of(graph).stream().map(layer -> layer.items()).toList();
        for (int depth = 0; depth < layers.size(); depth++) {
            final List<Vertex> layer = layers.get(depth);
            for (int i = 0; i < layer.size(); i++) {
                placed.put(layer.get(i), new Placed(layer.get(i), depth, i - (layer.size() - 1) / 2.0, 0));
            }
        }
        return new Placement(graph.vertices().items().stream().map(placed::get).toList());
    }
}
