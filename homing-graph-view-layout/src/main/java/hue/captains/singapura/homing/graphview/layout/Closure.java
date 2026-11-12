package hue.captains.singapura.homing.graphview.layout;

import hue.captains.singapura.homing.graphview.model.Edge;
import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.homing.graphview.model.Vertices;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** The closure of a vertex: the vertex and everything it depends on, directly or not. */
public record Closure() implements StatelessFunctionalObject {

    /**
     * The root and every vertex reached from it along the edges, in the graph's order.
     *
     * @param graph the graph
     * @param root  one of the graph's vertices
     */
    public Vertices of(final Graph graph, final Vertex root) {
        if (!graph.vertices().contains(root)) {
            throw new IllegalArgumentException("Vertex " + root.id() + " is not a vertex of the graph");
        }
        final Set<Vertex> reached = new HashSet<>(List.of(root));
        final Deque<Vertex> pending = new ArrayDeque<>(List.of(root));
        while (!pending.isEmpty()) {
            for (final Edge edge : graph.edges().from(pending.pop()).items()) {
                if (reached.add(edge.to())) {
                    pending.push(edge.to());
                }
            }
        }
        return new Vertices(graph.vertices().items().stream().filter(reached::contains).toList());
    }
}
