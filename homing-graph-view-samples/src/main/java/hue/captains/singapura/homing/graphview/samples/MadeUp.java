package hue.captains.singapura.homing.graphview.samples;

import hue.captains.singapura.homing.graphview.model.Edge;
import hue.captains.singapura.homing.graphview.model.Edges;
import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.homing.graphview.model.Vertices;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A made-up graph with layers of the sizes asked for, the same every time. Layer 0 depends on nothing; each vertex of a
 * layer above depends on one or two vertices of the layer below it, so that is its depth, and every third also on one
 * two layers down. A vertex is named by its layer and its place in it, as L2.07.
 */
public record MadeUp() implements StatelessFunctionalObject {

    /**
     * The graph.
     *
     * @param sizes how many vertices each layer has, layer 0 first, each at least 1
     */
    public Graph of(final List<Integer> sizes) {
        if (sizes.isEmpty() || sizes.stream().anyMatch(size -> size < 1)) {
            throw new IllegalArgumentException("A made-up graph needs layers of at least one vertex each, not "
                    + sizes);
        }
        final List<List<Vertex>> layers = new ArrayList<>();
        final Set<Edge> edges = new LinkedHashSet<>();
        for (int depth = 0; depth < sizes.size(); depth++) {
            final List<Vertex> layer = new ArrayList<>();
            for (int i = 0; i < sizes.get(depth); i++) {
                final Vertex vertex = new Vertex("L" + depth + "." + (i < 10 ? "0" : "") + i);
                layer.add(vertex);
                edges.addAll(below(vertex, i, depth, layers));
            }
            layers.add(layer);
        }
        return new Graph(new Vertices(layers.stream().flatMap(List::stream).toList()),
                new Edges(List.copyOf(edges)));
    }

    /** What the vertex at this place depends on in the layers below it. */
    private List<Edge> below(final Vertex vertex, final int index, final int depth, final List<List<Vertex>> layers) {
        if (depth == 0) {
            return List.of();
        }
        final List<Vertex> under = layers.get(depth - 1);
        final List<Edge> edges = new ArrayList<>(List.of(new Edge(vertex, under.get(index % under.size()))));
        if (index % 2 == 1) {
            edges.add(new Edge(vertex, under.get((index * 7 + 3) % under.size())));
        }
        if (index % 3 == 2 && depth >= 2) {
            final List<Vertex> further = layers.get(depth - 2);
            edges.add(new Edge(vertex, further.get((index * 5 + 1) % further.size())));
        }
        return edges;
    }
}
