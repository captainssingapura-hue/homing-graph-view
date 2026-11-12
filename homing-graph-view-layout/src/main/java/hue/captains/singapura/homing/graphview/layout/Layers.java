package hue.captains.singapura.homing.graphview.layout;

import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.homing.graphview.model.Vertices;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A graph's layers: its vertices grouped by depth, each layer in the graph's order. Depth is the longest path to a
 * vertex that depends on nothing, so every depth from 0 to the deepest has a layer.
 */
public record Layers() implements StatelessFunctionalObject {

    /**
     * The layers, the base's first: the layer at depth d is at index d.
     *
     * @param graph the graph, cycles allowed: vertices on a cycle share a layer
     */
    public List<Vertices> of(final Graph graph) {
        final Map<Vertex, Integer> depths = new Depths().of(graph);
        final int deepest = depths.values().stream().mapToInt(Integer::intValue).max().orElse(-1);
        final List<List<Vertex>> layers = new ArrayList<>();
        for (int depth = 0; depth <= deepest; depth++) {
            layers.add(new ArrayList<>());
        }
        for (final Vertex vertex : graph.vertices().items()) {
            layers.get(depths.get(vertex)).add(vertex);
        }
        return layers.stream().map(Vertices::new).toList();
    }
}
